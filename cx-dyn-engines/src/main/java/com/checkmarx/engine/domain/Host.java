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
	private final String ip;
	private final String url;
	private final String externalUrl;
	private final DateTime launchTime;
	
	public Host(String name, String ip, String url, String externalUrl, DateTime launchTime) {
		super();
		this.name = name;
		this.ip = ip;
		this.url = url;
		this.externalUrl = externalUrl;
		this.launchTime = launchTime;
	}

	public String getName() {
		return name;
	}

	public String getIp() {
		return ip;
	}

	public String getUrl() {
		return url;
	}
	
	public String getExternalUrl() {
		return externalUrl;
	}

	public DateTime getLaunchTime() {
		return launchTime;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, ip, url);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Host other = (Host) obj;
		return Objects.equals(this.name, other.name)
				&& Objects.equals(this.ip, other.ip)
				&& Objects.equals(this.url, other.url);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("ip", ip)
				.add("url", url)
				.add("externalUrl", externalUrl)
				.add("launchTime", launchTime)
				.toString();
	}

}
