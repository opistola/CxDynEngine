package com.checkmarx.engine.aws;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceNetworkInterfaceSpecification;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagSpecification;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.checkmarx.engine.aws.Ec2.InstanceState;
import com.checkmarx.engine.utils.TimedTask;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Launches and terminates EC2 instances from a specified AMI image.
 *  
 * @author randy@checkmarx.com
 *
 */
@Component
public class AwsEc2Client implements AwsComputeClient {
	
	private static final Logger log = LoggerFactory.getLogger(AwsEc2Client.class);
	
	private final AmazonEC2 client;
	private final AwsEngineConfig config;

	public AwsEc2Client(AwsEngineConfig config) {
		this.client = AmazonEC2ClientBuilder.defaultClient();
		this.config = config;

		log.info("ctor(): {}", this);
	}
	
	@Override
	public AwsEngineConfig getConfig() {
		return config;
	}
	
	@Override
	public Instance launch(String name, String instanceType, Map<String,String> tags) {
		log.trace("launch(): name={}; instanceType={}", name, instanceType);
		
		try {
			final TagSpecification tagSpec = createTagSpec(name, tags);
			
			final IamInstanceProfileSpecification profile = new IamInstanceProfileSpecification();
			profile.withName(config.getIamProfile());
			
			final InstanceNetworkInterfaceSpecification nic = new InstanceNetworkInterfaceSpecification();
			nic.withDeviceIndex(0)
				.withSubnetId(config.getSubnetId())
				.withGroups(config.getSecurityGroup())
				.withAssociatePublicIpAddress(config.isAssignPublicIP());

			final RunInstancesRequest runRequest = new RunInstancesRequest();
			runRequest.withImageId(config.getImageId())
				.withInstanceType(instanceType)
				.withKeyName(config.getKeyName())
				.withMinCount(1)
				.withMaxCount(1)
				//.withSecurityGroupIds(config.getSecurityGroup())
				//.withSubnetId(config.getSubnetId())
				.withNetworkInterfaces(nic)
				.withIamInstanceProfile(profile)
				.withTagSpecifications(tagSpec)
				;
			
			RunInstancesResult result = client.runInstances(runRequest);
			
			Instance instance = validateRunResult(result);
			
			final String instanceId = instance.getInstanceId();
			// wait until instance is running to populate IP addresses
			instance = waitForState(instanceId, null);
			
			final String requestId = result.getSdkResponseMetadata().getRequestId();
			final int statusCode = result.getSdkHttpMetadata().getHttpStatusCode();
			
			log.info("AWS EC2; action=launch; {}; requestId={}; status={}", 
					Ec2.print(instance), requestId, statusCode);
			
			return instance;
			
		} catch (AmazonClientException e) {
			log.warn("Failed to launch EC2 instance; name={}; cause={}", name, e.getMessage());
			throw new RuntimeException("Failed to launch EC2 instance", e);
		}
	}

	private TagSpecification createTagSpec(String name, Map<String, String> tags) {
		final TagSpecification tagSpec = new TagSpecification();
		tagSpec.getTags().add(createTag("Name", name));
		for (Entry<String,String> tag: tags.entrySet()) {
			tagSpec.getTags().add(createTag(tag.getKey(), tag.getValue()));
		}
		//tags.getTags().add(createTag(CX_ROLE_TAG, role));
		//tags.getTags().add(createTag(CX_VERSION_TAG, version));
		tagSpec.setResourceType("instance");
		return tagSpec;
	}
	
	@Override
	public Instance start(String instanceId) {
		log.trace("start(): instanceId={}", instanceId);
		
		try {
			final StartInstancesRequest request = new StartInstancesRequest();
			request.withInstanceIds(instanceId);
			
			final StartInstancesResult result = client.startInstances(request);
			final String requestId = result.getSdkResponseMetadata().getRequestId();
			final int statusCode = result.getSdkHttpMetadata().getHttpStatusCode();
		
			log.info("AWS EC2; action=start; instanceId={}; requestId={}; status={}", 
						instanceId, requestId, statusCode);
			
			return waitForState(instanceId, null);
			
		} catch (AmazonClientException e) {
			log.warn("Failed to start EC2 instance; instanceId={}; cause={}", instanceId, e.getMessage());
			throw new RuntimeException("Failed to start EC2 instance", e);
		}
	}
	
	@Override
	public void stop(String instanceId) {
		log.trace("stop(): instanceId={}", instanceId);
		
		try {
			final StopInstancesRequest request = new StopInstancesRequest();
			request.withInstanceIds(instanceId);
			
			final StopInstancesResult result = client.stopInstances(request);
			final String requestId = result.getSdkResponseMetadata().getRequestId();
			final int statusCode = result.getSdkHttpMetadata().getHttpStatusCode();
		
			log.info("AWS EC2; action=stop; instanceId={}; requestId={}; status={}", 
						instanceId, requestId, statusCode);
		} catch (AmazonClientException e) {
			log.warn("Failed to stop EC2 instance; instanceId={}; cause={}", instanceId, e.getMessage());
			throw new RuntimeException("Failed to stop EC2 instance", e);
		}
	}
	
	@Override
	public void terminate(String instanceId) {
		log.trace("terminate(): instanceId={}", instanceId);
		
		try {
			final TerminateInstancesRequest request = new TerminateInstancesRequest();
			request.withInstanceIds(instanceId);
			
			final TerminateInstancesResult result = client.terminateInstances(request);
			final String requestId = result.getSdkResponseMetadata().getRequestId();
			final int statusCode = result.getSdkHttpMetadata().getHttpStatusCode();
		
			log.info("AWS EC2; action=terminate; instanceId={}; requestId={}; status={}", 
						instanceId, requestId, statusCode);
		} catch (AmazonClientException e) {
			log.warn("Failed to terminate EC2 instance; instanceId={}; cause={}", instanceId, e.getMessage());
			throw new RuntimeException("Failed to terminate EC2 instance", e);
		}
	}
	
	@Override
	public List<Instance> find(String tag, String... values) {
		log.trace("list(): tag={}; values={}", tag, values);
		
		try {
			final List<Instance> allInstances = Lists.newArrayList();
			final DescribeInstancesRequest request = new DescribeInstancesRequest();
			
			if (!Strings.isNullOrEmpty(tag)) {
				final List<String> tagValues = Lists.newArrayList(values);
				final String filterKey = String.format("tag:%s", tag);
				final Filter filter = new Filter(filterKey, tagValues);
				request.withFilters(filter);
			}
	
			final DescribeInstancesResult result = client.describeInstances(request);
			for (Reservation reservation : result.getReservations()) {
				allInstances.addAll(reservation.getInstances());
			}
			return allInstances;
		} catch (AmazonClientException e) {
			log.warn("Failed to find EC2 instances; cause={}", e.getMessage());
			throw new RuntimeException("Failed to find EC2 instances", e);
		}
	}

	@Override
	public Instance describe(String instanceId) {
		log.trace("describe(): instanceId={}", instanceId);
		
		try {
			final DescribeInstancesRequest request = new DescribeInstancesRequest();
			request.withInstanceIds(instanceId);
			
			final DescribeInstancesResult result = client.describeInstances(request);
			final Instance instance = validateDescribeResult(result);
			if (instance == null) return null;
			
			final String requestId = result.getSdkResponseMetadata().getRequestId();
			final int statusCode = result.getSdkHttpMetadata().getHttpStatusCode();
			
			log.info("AWS EC2: action=describe; {}; requestId={}; status={}", 
					Ec2.print(instance), requestId, statusCode);
			
			return instance;
		} catch (AmazonClientException e) {
			log.warn("Failed to describe EC2 instance; instanceId={}; cause={}", instanceId, e.getMessage());
			throw new RuntimeException("Failed to describe EC2 instance", e);
		}
	}
	
	/**
	 * Calls describe until instance state is not pending, and optionally is not the state
	 * supplied.  Times out after <code>config.getStatusTimeoutSec()</code>.
	 * 
	 * @param instanceId
	 * @param skipState state to avoid, can be null
	 * @return instance or <null/> if not valid
	 * @throws RuntimeException if unable to determine status in the alloted timeout
	 */
	private Instance waitForState(String instanceId, InstanceState skipState) {
		log.trace("waitForState() : instanceId={}; skipState={}", instanceId, skipState);
		
		long sleepMs = config.getMonitorPollingIntervalSecs() * 1000;
		
		final TimedTask<Instance> task = 
				new TimedTask<>("waitForState", config.getStatusTimeoutSec(), TimeUnit.SECONDS);
		try {
			return task.execute(() -> {
				Instance instance = describe(instanceId);
				if (instance == null) return null;
				
				InstanceState state = Ec2.getState(instance);
				while (state.equals(InstanceState.PENDING) || state.equals(skipState)) {
					log.trace("Instance Pending, waiting to refresh state; instanceId={}; sleep={}", instanceId, sleepMs); 
					Thread.sleep(sleepMs);
					instance = describe(instanceId);
					state = Ec2.getState(instance); 
				}
				return instance;
			});
		} catch (TimeoutException e) {
			log.warn("Failed to determine instance state due to timeout; instanceId={}; message={}", 
					instanceId, e.getMessage());
			throw new RuntimeException("Timed out waiting for instance state", e);
		} catch (Exception e) {
			log.warn("Failed to determine instance state; instanceId={}; cause={}; message={}", 
					instanceId, e.getCause(), e.getMessage());
			throw new RuntimeException("Failed to determine instance state", e);
		}
	}
	
	@Override
	public boolean isProvisioned(String instanceId) {
		final Instance instance = waitForState(instanceId, null);
		return Ec2.isProvisioned(instance);
	}

	@Override
	public boolean isRunning(String instanceId) {
		final Instance instance = waitForState(instanceId, null);
		return Ec2.isRunning(instance);
	}

	private Instance validateRunResult(RunInstancesResult result) {
		if (result == null) return null;
		
		final Reservation reservation = result.getReservation();
		if (reservation == null) return null;
		
		final List<Instance> instances = reservation.getInstances();
		if (instances == null || instances.size() == 0) return null;
		
		return instances.get(0);
	}
		
	private Instance validateDescribeResult(DescribeInstancesResult result) {
		if (result == null) return null;
		
		final List<Reservation> reservations = result.getReservations();
		if (reservations == null || reservations.size() == 0) return null;
		
		final Reservation reservation = reservations.get(0);
		final List<Instance> instances = reservation.getInstances();
		if (instances == null || instances.size() == 0) return null;
		
		return instances.get(0);
	}
		
	private Tag createTag(String key, String value) {
		Tag tag = new Tag();
		tag.setKey(key);
		tag.setValue(value);
		return tag;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("config", config)
				.toString();
	}

}
