package com.checkmarx.engine.rest.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

public abstract class BaseResponse {

	protected String error;

	public String getError() {
		return error;
	}
	
	protected ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper(this)
				.add("error", error)
				.omitNullValues();
	}
}
