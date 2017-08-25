package com.checkmarx.engine.aws;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.ec2.model.Instance;

public interface AwsComputeClient {

	Instance launch(String name, String instanceType, Map<String, String> tags);

	void start(String instanceId);

	void stop(String instanceId);

	void terminate(String instanceId);
	
	List<Instance> list(String tag, List<String> values);

	Instance describe(String instanceId);
	
	boolean isProvisioned(String instanceId);

	boolean isRunning(String instanceId);

	AwsConfig getConfig();

}