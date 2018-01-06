package com.checkmarx.engine.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptRunnerTests {
	
	private static final Logger log = LoggerFactory.getLogger(ScriptRunnerTests.class);

	@Test
	public void test() {
		log.trace("test()");
		
		final ScriptRunner<String> runner = new ScriptRunner<String>();
		final boolean isValid = runner.loadScript("scripts/terminate.js");
		assertThat(isValid, is(true));
		
		runner.bindData("engine", "Testing...1,2,3...");
		runner.run();
	}

}
