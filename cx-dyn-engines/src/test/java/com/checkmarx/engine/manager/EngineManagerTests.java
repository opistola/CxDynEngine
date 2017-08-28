package com.checkmarx.engine.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.checkmarx.engine.rest.CxRestClient;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EngineManagerTests {
	
	private static final Logger log = LoggerFactory.getLogger(EngineManagerTests.class);
	
	@Autowired
	private EngineManager engineManager;

	@Autowired
	private CxRestClient cxClient;
	
	@Before
	public void setUp() throws Exception {
		assertThat(engineManager, notNullValue());
		assertThat(cxClient, notNullValue());
		assertThat(cxClient.login(), is(true));
	}
	
	@Test
	public void testShutdown() throws InterruptedException {
		log.trace("testShutdown()");
		
		
		engineManager.run();
		TimeUnit.SECONDS.sleep(10);
		engineManager.stop();
	}	

}
