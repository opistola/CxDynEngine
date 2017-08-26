package com.checkmarx.engine.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
public class QueueMonitorTests {
	
	private static final Logger log = LoggerFactory.getLogger(QueueMonitorTests.class);


	@Autowired
	private QueueMonitor monitor;
	
	@Autowired
	private CxRestClient cxClient;
	
	@Before
	public void setUp() throws Exception {
		log.trace("setup()");

		assertThat(monitor, is(notNullValue()));
	}
	
	@Test
	public void test() throws Exception {
		log.trace("test()");
		
		cxClient.login();
		
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(monitor, 0L, 5, TimeUnit.SECONDS);
		
		Thread.sleep(5*60*1000);
	}
	
}
