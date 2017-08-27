package com.checkmarx.engine.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EngineServiceTests {
	
	private static final Logger log = LoggerFactory.getLogger(EngineServiceTests.class);

	private final boolean runTest = false;  //uncomment next line to run this test
	//private final boolean runTest = true;

	@Autowired
	private EngineService service;
	
	@Before
	public void setUp() throws Exception {
		log.trace("setup()");

		Assume.assumeTrue(runTest);

		assertThat(service, is(notNullValue()));
	}
	
	@Test
	public void test() throws Exception {
		log.trace("test()");
		
		Assume.assumeTrue(runTest);

		service.run();
		
		Thread.sleep(10*60*1000);  // 10 mins
	}

}
