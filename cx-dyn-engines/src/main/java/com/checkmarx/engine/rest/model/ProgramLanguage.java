package com.checkmarx.engine.rest.model;

import com.google.common.base.MoreObjects;

public class ProgramLanguage {

	private long id;
	private String name;
	
	public ProgramLanguage() {
		// default .ctor for unmarshalling
	}
	
	public long getId() {
		return id;
	}
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("name", name)
				.toString();
	}
}
