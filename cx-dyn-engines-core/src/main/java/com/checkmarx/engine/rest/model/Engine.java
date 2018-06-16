package com.checkmarx.engine.rest.model;

import com.google.common.base.MoreObjects;

public class Engine {

	private long id;
	
	public Engine() {
		// default .ctor for unmarshalling
	}

	public Engine(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.toString();
	}
}
