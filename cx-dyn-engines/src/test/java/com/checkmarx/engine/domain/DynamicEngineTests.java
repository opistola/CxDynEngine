package com.checkmarx.engine.domain;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.checkmarx.engine.domain.DynamicEngine.State;

public class DynamicEngineTests {
	
	private static final Logger log = LoggerFactory.getLogger(DynamicEngineTests.class);

	
	@Test
	public void testTimeToExpire() throws Exception {
		log.trace("testTimeToExpire()");
		
		final int EXPIRE_DURATION = 3;
		
		final DynamicEngine engine = new DynamicEngine("name", new ScanSize("S", 0, 1), EXPIRE_DURATION);
		log.debug("unprovisioned: {}", engine);
		assertThat(engine.getState(), is(State.UNPROVISIONED));
		assertThat(engine.getRunTime(), is(Duration.ZERO));
		assertThat(engine.getTimeToExpire(), is(nullValue()));
		Thread.sleep(1000);
		
		engine.setState(State.IDLE);
		Thread.sleep(1000);
		log.debug("idle: {}", engine);

		assertThat(engine.getState(), is(State.IDLE));
		assertThat(engine.getRunTime().getStandardSeconds(), is(1L));
		assertThat(engine.getTimeToExpire(), is(notNullValue()));
		assertThat(engine.getTimeToExpire().isAfterNow(), is(true));
		final DateTime firstTimeToExpire = engine.getTimeToExpire();
		final Duration firstExpireDuration = new Duration(engine.getLaunchTime(), firstTimeToExpire);
		assertThat(firstExpireDuration.getMillis(), is(EXPIRE_DURATION * 1000L));
		
		engine.setState(State.ACTIVE);
		log.debug("active: {}", engine);
		assertThat(engine.getState(), is(State.ACTIVE));
		assertThat(engine.getTimeToExpire(), is(nullValue()));
		assertThat(engine.getRunTime().getStandardSeconds(), is(1L));

		Thread.sleep(EXPIRE_DURATION * 1000);
		engine.setState(State.IDLE);
		log.debug("idle: {}", engine);
		assertThat(engine.getState(), is(State.IDLE));
		final DateTime nextTimeToExpire = engine.getTimeToExpire();
		assertThat(nextTimeToExpire.isAfterNow(), is(true));
		assertThat(nextTimeToExpire.isAfter(firstTimeToExpire), is(true));

		engine.setState(State.UNPROVISIONED);
		log.debug("unprovisioned: {}", engine);
		assertThat(engine.getState(), is(State.UNPROVISIONED));
		assertThat(engine.getRunTime(), is(Duration.ZERO));
		assertThat(engine.getTimeToExpire(), is(nullValue()));

	}

}
