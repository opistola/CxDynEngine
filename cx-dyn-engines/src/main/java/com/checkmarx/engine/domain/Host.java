/**
 * 
 */
package com.checkmarx.engine.domain;

import java.util.Objects;

import org.joda.time.DateTime;

import com.google.common.base.MoreObjects;

/**
 * @author rjgey
 *
 */
public class Host {
	
	private final String name;
	private final String privateIp;
	private final String publicIp;
	private final String cxManagerUrl;
	private final String monitorUrl;
	private final DateTime launchTime;
	
	public Host(String name, String privateIp, String managerUrl, DateTime launchTime) {
		this(name, privateIp, null, managerUrl, null, launchTime);
	}
	
	public Host(String name, String privateIp, String publicIp, 
			String managerUrl, String monitorUrl, DateTime launchTime) {
		super();
		this.name = name;
		this.privateIp = privateIp;
		this.publicIp = publicIp;
		this.cxManagerUrl = managerUrl;
		this.monitorUrl = monitorUrl;
		this.launchTime = launchTime;
	}

	public String getName() {
		return name;
	}

	public String getIp() {
		return privateIp;
	}

	/**
	 * Returns the url for the CxManager 
	 */
	public String getCxManagerUrl() {
		return cxManagerUrl;
	}
	
	/**
	 * Returns the url for monitoring the host during spinup 
	 */
	public String getMonitorUrl() {
		return monitorUrl;
	}

	public DateTime getLaunchTime() {
		return launchTime;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, privateIp, cxManagerUrl);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Host other = (Host) obj;
		return Objects.equals(this.name, other.name)
				&& Objects.equals(this.privateIp, other.privateIp)
				&& Objects.equals(this.publicIp, other.publicIp);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("privateIp", privateIp)
				.add("publicIp", publicIp)
				.add("cxManagerUrl", cxManagerUrl)
				.add("monitorUrl", monitorUrl)
				.add("launchTime", launchTime)
				.toString();
	}

}
