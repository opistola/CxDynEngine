package com.checkmarx.engine.aws;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.common.base.MoreObjects;

@Component
@ConfigurationProperties(prefix="cx-aws")
public class AwsConfig {

	private boolean assignPublicIP;
	private String cxVersion;
	private String iamProfile;
	private String imageId;
	private String keyName;
	private int pollingIntervalSecs = 10;
	private String securityGroup;
	private String subnetId;
	private boolean terminateOnStop;
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
		this.iamProfile = iamProfile.trim();
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId.trim();
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName.trim();
	}

	public String getSecurityGroup() {
		return securityGroup;
	}

	public void setSecurityGroup(String securityGroup) {
		this.securityGroup = securityGroup.trim();
	}

	public String getSubnetId() {
		return subnetId;
	}

	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId.trim();
	}

	public boolean isTerminateOnStop() {
		return terminateOnStop;
	}

	public void setTerminateOnStop(boolean terminateOnStop) {
		this.terminateOnStop = terminateOnStop;
	}

	public Map<String, String> getEngineTypeMap() {
		return engineSizeMap;
	}

	public void setEngineSizeMap(Map<String, String> engineSizeMap) {
		this.engineSizeMap = engineSizeMap;
	}

	public int getPollingIntervalSecs() {
		return pollingIntervalSecs;
	}

	public void setPollingIntervalSecs(int pollingIntervalSecs) {
		this.pollingIntervalSecs = pollingIntervalSecs;
	}

	// TODO: print out engineSizeMap
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("assignPublicIP", assignPublicIP)
				.add("cxVersion", cxVersion)
				.add("iamProfile", iamProfile)
				.add("imageId", imageId)
				.add("keyName", keyName)
				.add("pollingIntervalSecs", pollingIntervalSecs)
				.add("securityGroup", securityGroup)
				.add("subnetId", subnetId)
				.add("terminateOnStop", terminateOnStop)
				.toString();
	}

}
