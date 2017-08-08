package com.checkmarx.engine.rest.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class EngineServer {
	
	private Long id;
	private String name;
	private String uri;
	private int minLoc;
	private int maxLoc;
	@JsonProperty(value="isAlive")
	private Boolean isAlive; 
	private int maxScans;
	@JsonProperty(value="isBlocked")
	private boolean isBlocked;
	private String cxVersion;
	
	public EngineServer() {
		// default .ctor for unmarshalling
	}
	
	public EngineServer(String name, String uri, int minLoc, int maxLoc, int maxScans, boolean isBlocked) {
		this.name = name;
		this.uri = uri;
		this.minLoc = minLoc;
		this.maxLoc = maxLoc;
		this.maxScans = maxScans;
		this.isBlocked = isBlocked;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getUri() {
		return uri;
	}

	public int getMinLoc() {
		return minLoc;
	}

	public int getMaxLoc() {
		return maxLoc;
	}

	@JsonIgnore
	public Boolean isAlive() {
		return isAlive;
	}

	public int getMaxScans() {
		return maxScans;
	}

	@JsonIgnore
	public boolean isBlocked() {
		return isBlocked;
	}

	public String getCxVersion() {
		return cxVersion;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("name", name)
				.add("uri", uri)
				.add("minLOC", minLoc)
				.add("maxLOC", maxLoc)
				.add("isAlive", isAlive)
				.add("maxScans", maxScans)
				.add("isBlocked", isBlocked)
				.add("cxVersion", cxVersion)
				.omitNullValues()
				.toString();
	}
}
