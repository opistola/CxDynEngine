package com.checkmarx.engine.servers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.checkmarx.engine.domain.DynamicEngine;
import com.checkmarx.engine.domain.EngineSize;
import com.google.common.collect.Lists;

/**
 * {@code CxEngines} that does nothing.  
 * Useful for unit testing and spring context building.
 *  
 * @author rjgey
 * @see CxEngines
 *
 */
@Profile("noop")
@Component
public class NoopEngines implements CxEngines {
	
	private static final Logger log = LoggerFactory.getLogger(NoopEngines.class);


	@Override
	public List<DynamicEngine> listEngines() {
		log.debug("listEngines()");
		return Lists.newArrayList();
	}

	@Override
	public void launch(DynamicEngine engine, EngineSize size, boolean waitForSpinup) {
		log.debug("launch() : {}; {}; waitForSpinup={}", engine, size, waitForSpinup);
		// noop
	}

	@Override
	public void stop(DynamicEngine engine) {
		log.debug("stop() : {}", engine);
		// noop
	}

	@Override
	public void stop(DynamicEngine engine, boolean forceTerminate) {
		log.debug("stop() : {}, force={}", engine, forceTerminate);
		// noop
	}

}
