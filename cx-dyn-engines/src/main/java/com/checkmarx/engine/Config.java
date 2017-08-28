package com.checkmarx.engine;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.common.base.MoreObjects;

@Component
@ConfigurationProperties(prefix="cx")
public class Config {
	
	private final String cxEngineUrlPath = "/CxSourceAnalyzerEngineWCF/CxEngineWebServices.svc";
	
	private String userName;
	private String password;
	private String cxEnginePrefix = "**";
	private String enginePoolPrefix = "cx-engine";
	private int idleMonitorSecs = 15;
	private int queueCapacity = 100;
	private int queueIntervalSecs = 20;
	private long queueingEngineId=1L;
	private String restUrl;
	private int timeoutSecs = 20;
	private String userAgent = "CxDynamicEngineManager";
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCxEnginePrefix() {
		return cxEnginePrefix;
	}

	public void setCxEnginePrefix(String cxEnginePrefix) {
		this.cxEnginePrefix = cxEnginePrefix;
	}

	public String getEnginePoolPrefix() {
		return enginePoolPrefix;
	}

	public void setEnginePoolPrefix(String enginePoolPrefix) {
		this.enginePoolPrefix = enginePoolPrefix;
	}

	public int getIdleMonitorSecs() {
		return idleMonitorSecs;
	}

	public void setIdleMonitorSecs(int idleMonitorSecs) {
		this.idleMonitorSecs = idleMonitorSecs;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public int getQueueIntervalSecs() {
		return queueIntervalSecs;
	}
	
	public void setQueueIntervalSecs(int queueIntervalSecs) {
		this.queueIntervalSecs = queueIntervalSecs;
	}

	public long getQueueingEngineId() {
		return queueingEngineId;
	}

	public void setQueueingEngineId(long queueingEngineId) {
		this.queueingEngineId = queueingEngineId;
	}

	public String getRestUrl() {
		return restUrl;
	}

	public void setRestUrl(String url) {
		this.restUrl = url;
	}

	public int getTimeoutSecs() {
		return timeoutSecs;
	}

	public void setTimeoutSecs(int timeoutSecs) {
		this.timeoutSecs = timeoutSecs;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getCxEngineUrlPath() {
		return cxEngineUrlPath;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("userName", userName)
				.add("cxEnginePrefix", cxEnginePrefix)
				.add("cxEngineUrlPath", cxEngineUrlPath)
				.add("enginePoolPrefix", enginePoolPrefix)
				.add("idleMonitorSecs", idleMonitorSecs)
				.add("queueCapacity", queueCapacity)
				.add("queueIntervalSecs", queueIntervalSecs)
				.add("queueingEngineId", queueingEngineId)
				.add("restUrl", restUrl)
				.add("timeoutSecs", timeoutSecs)
				.add("userAgent", userAgent)
				.toString();
	}

}
