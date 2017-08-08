package com.checkmarx.engine.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {
	
	private Integer messageCode;
	private String messageDetails;
	
	public ErrorResponse() {
		// default .ctor for unmarshalling
	}

	public Integer getMessageCode() {
		return messageCode;
	}

	public String getMessageDetails() {
		return messageDetails;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("messageCode", messageCode)
				.add("messageDetails", messageDetails)
				.toString();
	}

}
