package com.checkmarx.engine.rest;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

public class CxRestExceptionHandler extends DefaultResponseErrorHandler {
	
	private static final Logger log = LoggerFactory.getLogger(CxRestExceptionHandler.class);

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		log.trace("handleError()");
		
		// TODO Auto-generated method stub
		super.handleError(response);
	}

}
