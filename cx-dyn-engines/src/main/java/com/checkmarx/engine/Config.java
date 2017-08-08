package com.checkmarx.engine;

import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.google.common.base.MoreObjects;

@ConfigurationProperties(prefix="cx")
public class Config {
	
	private String userName;
	
	private String password;
	
	private String cxUrl;
	
	private String callbackUrl;
	
	private String callbackSecret = UUID.randomUUID().toString();
	
	private int timeoutSecs = 20;
	
	private String cxEngineUrlPath = "/CxSourceAnalyzerEngineWCF/CxEngineWebServices.svc";
		
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

	public String getCxUrl() {
		return cxUrl;
	}

	public void setUrl(String url) {
		this.cxUrl = url;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public String getCallbackSecret() {
		return callbackSecret;
	}

	public void setCallbackSecret(String callbackSecret) {
		this.callbackSecret = callbackSecret;
	}

	public int getTimeoutSecs() {
		return timeoutSecs;
	}

	public void setTimeoutSecs(int timeoutSecs) {
		this.timeoutSecs = timeoutSecs;
	}

	public String getCxEngineUrlPath() {
		return cxEngineUrlPath;
	}

	public void setCxEngineUrlPath(String cxEngineUrlPath) {
		this.cxEngineUrlPath = cxEngineUrlPath;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("userName", userName)
				.add("cxUrl", cxUrl)
				.add("callbackUrl", callbackUrl)
				.add("timeoutSecs", timeoutSecs)
				.add("cxEngineUrlPath", getCxEngineUrlPath())
				.toString();
	}

}
