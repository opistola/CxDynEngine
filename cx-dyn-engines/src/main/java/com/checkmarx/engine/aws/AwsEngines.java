package com.checkmarx.engine.aws;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.util.StringUtils;
import com.checkmarx.engine.aws.Ec2.InstanceState;
import com.checkmarx.engine.domain.DynamicEngine;
import com.checkmarx.engine.domain.DynamicEngine.State;
import com.checkmarx.engine.domain.ScanSize;
import com.checkmarx.engine.domain.Host;
import com.checkmarx.engine.manager.EngineProvisioner;
import com.checkmarx.engine.rest.CxRestClient;
import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author rjgey
 *
 */
@Component
public class AwsEngines implements EngineProvisioner {

	private static final Logger log = LoggerFactory.getLogger(AwsEngines.class);
	
	private final AwsConfig config;
	private final AwsComputeClient ec2Client;
	private final CxRestClient cxClient;
	private final int pollingMillis;
	// key=engine name
	private final Map<String, Instance> provisionedEngines = Maps.newConcurrentMap();
	// key=size name, value=instanceType
	private final Map<String, String> engineTypeMap;
	
	public AwsEngines(
			AwsComputeClient awsClient, 
			CxRestClient engineClient) {
		
		this.ec2Client = awsClient;
		this.config = awsClient.getConfig(); 
		this.cxClient = engineClient;
		this.engineTypeMap = config.getEngineTypeMap();
		this.pollingMillis = config.getPollingIntervalSecs() * 1000;
		
		log.info("ctor(): {}", this);
	}
	
	public static Map<String, String> createCxTags(CxServerRole role, String version) {
		log.trace("createCxTags(): role={}; version={}", role, version);
		
		final Map<String, String> tags = Maps.newHashMap();
		tags.put(CX_ROLE_TAG, role.toString());
		tags.put(CX_VERSION_TAG, version);
		return tags;
	}
	
	private Map<String, String> createEngineTags(String size) {
		log.trace("createEngineTags(): size={}", size);
		final Map<String, String> tags = createCxTags(CxServerRole.ENGINE, config.getCxVersion());
		tags.put(CX_SIZE_TAG, size);
		return tags;
	}

	Map<String, Instance> findEngines() {
		log.trace("findEngines()");
		
		final Stopwatch timer = Stopwatch.createStarted(); 
		try {
			final List<Instance> engines = ec2Client.find(CX_ROLE_TAG, CxServerRole.ENGINE.toString());
			engines.forEach((instance) -> {
				if (Ec2.isTerminated(instance)) {
					log.info("Terminated engine found: {}", Ec2.print(instance));
					return;
				}

				final String name = Ec2.getName(instance);
				if (!provisionedEngines.containsKey(name)) {
					provisionedEngines.put(name, instance);
					log.info("Provisioned engine found: {}", Ec2.print(instance));
				}
			});
			
		} finally {
			log.debug("Find Engines: elapsedTime={}ms; count={}", 
					timer.elapsed(TimeUnit.MILLISECONDS), provisionedEngines.size()); 
		}
		return provisionedEngines;
	}
	
	@Override
	public List<DynamicEngine> listEngines() {
		final Map<String, Instance> engines = findEngines();
		final List<DynamicEngine> dynEngines = Lists.newArrayList();
		engines.forEach((name, instance) -> {
			final DynamicEngine engine = buildDynamicEngine(name, instance);
			dynEngines.add(engine);
		});
		return dynEngines;
	}

	DynamicEngine buildDynamicEngine(String name, Instance instance) {
		final String size = lookupEngineSize(instance);
		final DateTime launchTime = new DateTime(instance.getLaunchTime());
		final boolean isRunning = 
				InstanceState.from(instance.getState().getCode()).equals(InstanceState.RUNNING);
		final DynamicEngine engine = DynamicEngine.fromProvisionedInstance(
				name, size, AwsConstants.BILLING_INTERVAL_SECS,
				launchTime, isRunning);
		if (isRunning) {
			engine.setHost(createHost(name, instance));
		}
		return engine;
	}

	private String lookupEngineSize(Instance instance) {
		String sizeTag = Ec2.getTag(instance, CX_SIZE_TAG);
		if (!StringUtils.isNullOrEmpty(sizeTag)) return sizeTag;
		
		final Map<String, String> sizeMap = config.getEngineTypeMap();
		
		for (Entry<String,String> entry : sizeMap.entrySet()) {
			String instanceType = entry.getValue();
			String size = entry.getKey();
			if (instance.getInstanceType().equals(instanceType))
				return size;
		}
		// if not found, return first size in map
		return Iterables.getFirst(sizeMap.values(), "S"); 
	}

	@Override
	public void launch(DynamicEngine engine, ScanSize size, boolean waitForSpinup) {
		log.trace("launch() : {}; size={}", engine, size);
		
		findEngines();
		
		final String name = engine.getName();
		final String type = engineTypeMap.get(size.getName());
		final Map<String, String> tags = createEngineTags(size.getName());
		
		Instance instance = provisionedEngines.get(name);
		String instanceId = null;
		
		final Stopwatch timer = Stopwatch.createStarted(); 
		try {
			if (instance == null) {
				instance = launchEngine(engine, name, type, tags);
			}

			instanceId = instance.getInstanceId();
			
			if (Ec2.isTerminated(instance)) {
				instance = launchEngine(engine, name, type, tags);
				instanceId = instance.getInstanceId();
			} else if (!ec2Client.isRunning(instanceId)) {
				ec2Client.start(instanceId);
			} else {
			}
			final Host host = createHost(name, instance);
			engine.setHost(host);
			
			if (waitForSpinup) {
				waitForSpinup(host);
			}
			//engine.setHost(createHost(name, instance));
			engine.setState(State.IDLE);
			
		} finally {
			log.debug("Launched instance: name={}; id={}; elapsedTime={}s; {}", 
					name, instanceId, timer.elapsed(TimeUnit.SECONDS), Ec2.print(instance)); 
		}
	}

	@Override
	public void stop(DynamicEngine engine) {
		stop(engine, false);
	}
	
	@Override
	public void stop(DynamicEngine engine, boolean forceTerminate) {
		log.trace("stop() : {}", engine);

		final String name = engine.getName();
		final Instance instance = provisionedEngines.get(name);
		
		if (instance == null) {
			throw new RuntimeException("Cannot stop engine, no instance found");
		}
		
		final String instanceId = instance.getInstanceId();
		if (config.isTerminateOnStop() || forceTerminate) {
			ec2Client.terminate(instanceId);
			engine.setState(State.UNPROVISIONED);
		} else {
			ec2Client.stop(instanceId);
			engine.setState(State.IDLE);
		}
		
	}

	private Instance launchEngine(final DynamicEngine engine, final String name, 
			final String type, final Map<String, String> tags) {
		log.debug("launchEngine(): name={}; type={}", name, type);
		
		final Instance instance = ec2Client.launch(name, type, tags);
		//engine.setState(State.SCANNING);
		provisionedEngines.put(name, instance);
		return instance;
	}

	private Host createHost(final String name, final Instance instance) {
		final String ip = getIpAddress(instance);
		final String extIp = instance.getPublicIpAddress();
		final DateTime launchTime = new DateTime(instance.getLaunchTime());
		return new Host(name, ip, buildUrl(ip), buildUrl(extIp), launchTime);
	}

	private void waitForSpinup(Host host) {
		log.trace("waitForSpinup() : host={}", host);
		
		String server = host.getExternalUrl();
		if (StringUtils.isNullOrEmpty(server))
			server = host.getUrl();
		while (!cxClient.pingEngine(server)) {
			try {
				Thread.sleep(pollingMillis);
			} catch (InterruptedException e) {
				final String msg = String.format("Interrupted while waiting for AWS; instance=%1s", 
						host.getName());
				throw new RuntimeException(msg, e);
			}
		}
	}
	
	private String getIpAddress(Instance instance)  {
		String ip = instance.getPrivateIpAddress();
		final String instanceId = instance.getInstanceId();
		while (ip == null) {
			try {
				Thread.sleep(pollingMillis);
				Instance newInstance = ec2Client.describe(instanceId);
				ip = newInstance.getPrivateIpAddress();
			} catch (InterruptedException e) {
				final String msg = String.format("Interrupted while waiting for AWS instanceId=%1s", 
						instance.getInstanceId());
				throw new RuntimeException(msg, e);
			}
		}
		return ip;
	}

	private String buildUrl(String ip) {
		if (StringUtils.isNullOrEmpty(ip)) return null;
		final String host = "http://" + ip;
		return cxClient.buildEngineServerUrl(host);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("config", config)
				.toString();
	}

}
