package com.checkmarx.engine.aws;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.checkmarx.engine.domain.EngineSize;
import com.google.common.base.MoreObjects;

@Component
@ConfigurationProperties(prefix="cx-aws-engine")
public class AwsEngineConfig {

	private boolean assignPublicIP;
	private String cxVersion;
	private String iamProfile;
	private String imageId;
	private String keyName;
	private int monitorPollingIntervalSecs = 10;
	private int monitorTimeoutSec=300;
	private String securityGroup;
	private int statusTimeoutSec=60;
	private String subnetId;
	private boolean terminateOnStop;
	private boolean usePublicUrlForCx = false;
	private boolean usePublicUrlForMonitor = false;
	
	
	/**
	 * Maps EngineSize to EC2 instanceType; 
	 * key=size (name), 
	 * value=ec2 instance type (e.g. m4.large)
	 */
	private Map<String, String> engineSizeMap;
	
	public boolean isAssignPublicIP() {
		return assignPublicIP;
	}

	public void setAssignPublicIP(boolean assignPublicIP) {
		this.assignPublicIP = assignPublicIP;
	}

	public String getCxVersion() {
		return cxVersion;
	}

	public void setCxVersion(String cxVersion) {
		this.cxVersion = cxVersion;
	}

	public String getIamProfile() {
		return iamProfile;
	}

	public void setIamProfile(String iamProfile) {
		this.iamProfile = iamProfile;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public int getMonitorPollingIntervalSecs() {
		return monitorPollingIntervalSecs;
	}

	public int getMonitorTimeoutSec() {
		return monitorTimeoutSec;
	}

	public void setMonitorTimeoutSec(int monitorTimeoutSec) {
		this.monitorTimeoutSec = monitorTimeoutSec;
	}

	public void setMonitorPollingIntervalSecs(int monitorPollingIntervalSecs) {
		this.monitorPollingIntervalSecs = monitorPollingIntervalSecs;
	}

	public String getSecurityGroup() {
		return securityGroup;
	}

	public void setSecurityGroup(String securityGroup) {
		this.securityGroup = securityGroup;
	}

	public int getStatusTimeoutSec() {
		return statusTimeoutSec;
	}

	public void setStatusTimeoutSec(int statusTimeoutSec) {
		this.statusTimeoutSec = statusTimeoutSec;
	}

	public String getSubnetId() {
		return subnetId;
	}

	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId;
	}

	public boolean isTerminateOnStop() {
		return terminateOnStop;
	}

	public void setTerminateOnStop(boolean terminateOnStop) {
		this.terminateOnStop = terminateOnStop;
	}

	public boolean isUsePublicUrlForCx() {
		return usePublicUrlForCx;
	}

	public void setUsePublicUrlForCx(boolean usePublicUrlForCx) {
		this.usePublicUrlForCx = usePublicUrlForCx;
	}

	public boolean isUsePublicUrlForMonitor() {
		return usePublicUrlForMonitor;
	}

	public void setUsePublicUrlForMonitor(boolean usePublicUrlForMonitor) {
		this.usePublicUrlForMonitor = usePublicUrlForMonitor;
	}

	/**
	 * Map of EngineSize to EC2 instanceType; 
	 * key=size (name), 
	 * value=ec2 instance type (e.g. m4.large)
	 * @see EngineSize
	 */
	public Map<String, String> getEngineSizeMap() {
		return engineSizeMap;
	}

	public void setEngineSizeMap(Map<String, String> engineSizeMap) {
		this.engineSizeMap = engineSizeMap;
	}

	public String printEngineSizeMap() {
		final StringBuilder sb = new StringBuilder();
		engineSizeMap.forEach((size,instanceType) ->
			sb.append(String.format("%s->%s, ", size,instanceType)) );
		return sb.toString().replaceAll(", $", ""); 
	}

	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("assignPublicIP", assignPublicIP)
				.add("cxVersion", cxVersion)
				.add("iamProfile", iamProfile)
				.add("imageId", imageId)
				.add("keyName", keyName)
				.add("monitorPollingIntervalSecs", monitorPollingIntervalSecs)
				.add("monitorTimeoutSec", monitorTimeoutSec)
				.add("securityGroup", securityGroup)
				.add("statusTimeoutSec", statusTimeoutSec)
				.add("subnetId", subnetId)
				.add("terminateOnStop", terminateOnStop)
				.add("usePublicUrlForCx", usePublicUrlForCx)
				.add("usePublicUrlForMonitor", usePublicUrlForMonitor)
				.add("engineSizeMap", "[" + printEngineSizeMap() +"]")
				.toString();
	}

}
