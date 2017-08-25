/**
 * 
 */
package com.checkmarx.engine.domain;

import com.google.common.base.MoreObjects;

/**
 * @author rjgey
 *
 */
public class Host {
	
	private final String name;
	private final String ip;
	private final String url;
	
	public Host(String name, String ip, String url) {
		super();
		this.name = name;
		this.ip = ip;
		this.url = url;
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

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("ip", ip)
				.add("url", url)
				.toString();
	}

}
