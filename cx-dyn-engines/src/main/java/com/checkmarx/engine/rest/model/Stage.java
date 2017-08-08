package com.checkmarx.engine.rest.model;

import com.google.common.base.MoreObjects;

public class Stage {

	private long id;
	private String value;
	
	public Stage() {
		// default .ctor for unmarshalling
	}
	
	public long getId() {
		return id;
	}
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("value", value)
				.toString();
	}
}
