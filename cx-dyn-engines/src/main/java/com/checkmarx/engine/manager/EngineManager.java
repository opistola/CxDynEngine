package com.checkmarx.engine.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.checkmarx.engine.Config;
import com.checkmarx.engine.rest.CxRestClient;

@Component
public class EngineManager implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(EngineManager.class);

	private final CxRestClient cxClient;
	private final Config config;

	public EngineManager(CxRestClient cxClient, Config config) {
		log.debug("ctor(): {}; {}", cxClient, config);
		
		this.cxClient = cxClient;
		this.config = config;
	}


	@Override
	public void run() {
		log.info("run()");
		
		
		
	}

}
