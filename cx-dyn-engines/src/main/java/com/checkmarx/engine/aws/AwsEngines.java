package com.checkmarx.engine.aws;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.services.ec2.model.Instance;
import com.checkmarx.engine.domain.DynamicEngine;
import com.checkmarx.engine.domain.ScanSize;
import com.checkmarx.engine.domain.Host;
import com.checkmarx.engine.manager.EngineProvisioner;
import com.checkmarx.engine.rest.CxRestClient;
import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;

/**
 * @author rjgey
 *
 */
@Component
public class AwsEngines implements EngineProvisioner {

	private static final Logger log = LoggerFactory.getLogger(AwsEngines.class);
	
	private final AwsConfig config;
	private final AwsComputeClient ec2;
	private final CxRestClient cxClient;
	private final int pollingMillis;
	// key=engine name
	private final Map<String, Instance> activeEngines = Maps.newConcurrentMap();
	private final Map<String, String> engineSizeMap;
	
	public AwsEngines(
			AwsComputeClient awsClient, 
			CxRestClient engineClient) {
		
		this.ec2 = awsClient;
		this.config = awsClient.getConfig(); 
		this.cxClient = engineClient;
		this.engineSizeMap = config.getEngineSizeMap();
		this.pollingMillis = config.getPollingIntervalSecs() * 1000;
		
		log.info("ctor(): {}", this);
	}
	
	public static Map<String, String> createCxTags(CxRoles role, String version) {
		log.trace("createCxTags(): role={}; version={}", role, version);
		
		final Map<String, String> tags = Maps.newHashMap();
		tags.put(CX_ROLE_TAG, role.toString());
		tags.put(CX_VERSION_TAG, version);
		return tags;
	}

	@Override
	public void launch(DynamicEngine engine, ScanSize size, boolean waitForSpinup) {
		log.trace("launch() : {}; size={}", engine, size);
		
		final String name = engine.getName();
		final String type = engineSizeMap.get(size.getName());
		
		Instance instance = activeEngines.get(name);
		String instanceId = null;
		
		final Stopwatch timer = Stopwatch.createStarted(); 
		try {
		
			if (instance == null) {
				instance = launchEngine(engine, name, type);
			}

			instanceId = instance.getInstanceId();
			
			if (!ec2.isProvisioned(instanceId)) {
				instance = launchEngine(engine, name, type);
				instanceId = instance.getInstanceId();
			} else if (!ec2.isRunning(instanceId)) {
				ec2.start(instanceId);
			}
			
			if (waitForSpinup) {
				waitForSpinup(instance);
			}

		} finally {
			log.debug("Launched instance: name={}; id={}; elapsedTime={}s; {}", 
					name, instanceId, timer.elapsed(TimeUnit.SECONDS), instance); 
		}
	}

	private Instance launchEngine(final DynamicEngine engine, final String name, final String type) {
		log.debug("launchEngine(): name={}; type={}", name, type);
		
		final Instance instance = ec2.launch(name, type, 
				createCxTags(CxRoles.ENGINE, config.getCxVersion()));
		final String ip = getIpAddress(instance);
		final Host host = new Host(name, ip, buildUrl(ip));
		engine.setHost(host);
		activeEngines.put(name, instance);
		return instance;
	}

	private void waitForSpinup(Instance instance) {
		log.trace("waitForSpinup() : instance={}", instance);
		
		final String host = "http://" + getIpAddress(instance);
		while (!cxClient.pingEngine(host)) {
			try {
				Thread.sleep(pollingMillis);
			} catch (InterruptedException e) {
				final String msg = String.format("Interrupted while waiting for AWS instanceId=%1s", 
						instance.getInstanceId());
				throw new RuntimeException(msg, e);
			}
		}
	}
	
	private String getIpAddress(Instance instance)  {
		String ip = instance.getPublicIpAddress();
		final String instanceId = instance.getInstanceId();
		while (ip == null) {
			try {
				Thread.sleep(pollingMillis);
				Instance newInstance = ec2.describe(instanceId);
				ip = newInstance.getPublicIpAddress();
			} catch (InterruptedException e) {
				final String msg = String.format("Interrupted while waiting for AWS instanceId=%1s", 
						instance.getInstanceId());
				throw new RuntimeException(msg, e);
			}
		}
		return ip;
	}

	private String buildUrl(String ip) {
		final String host = "http://" + ip;
		return cxClient.buildEngineServerUrl(host);
	}

	@Override
	public void stop(DynamicEngine engine) {
		log.trace("stop() : {}", engine);

		final String name = engine.getName();
		final Instance instance = activeEngines.get(name);
		
		if (instance == null) {
			throw new RuntimeException("Cannot stop engine, no instance found");
		}
		
		final String instanceId = instance.getInstanceId();
		ec2.stop(instanceId);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("config", config)
				.toString();
	}
}
