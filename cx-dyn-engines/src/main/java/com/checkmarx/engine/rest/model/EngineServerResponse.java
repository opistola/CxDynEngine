package com.checkmarx.engine.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EngineServerResponse extends BaseResponse {
	
	private Long id;
	private String self;
	
	public Long getId() {
		return id;
	}
	public String getSelf() {
		return self;
	}

	@Override
	public String toString() {
		return super.toStringHelper()
				.add("id", id)
				.add("self", self)
				.toString();
	}

}
