package com.checkmarx.engine.aws;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.checkmarx.engine.aws.AwsEngines;
import com.checkmarx.engine.domain.DynamicEngine;
import com.checkmarx.engine.domain.ScanSize;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AwsEnginesTest {
	
	private static final Logger log = LoggerFactory.getLogger(AwsEnginesTest.class);

	//private final boolean runTest = false;  //uncomment next line to run this test
	private final boolean runTest = true;
	
	@Autowired
	private AwsEngines awsEngines;
	
	@Before
	public void setUp() throws Exception {
		log.trace("setUp()");

		Assume.assumeTrue(runTest);
		
		assertThat(awsEngines, is(notNullValue()));
	}

	@Test
	public void testLaunch() {
		log.trace("testLaunch()");
		
		final ScanSize size = new ScanSize("S", 1, 50000);
		final DynamicEngine engine = new DynamicEngine("cx-engine-test-01", size, AwsConstants.BILLING_INTERVAL_SECS);
		
		awsEngines.launch(engine, size, false);
		assertThat(engine.getHost(), is(notNullValue()));
	}

	@Test
	public void testStop() {
		fail("Not yet implemented");
	}

}
