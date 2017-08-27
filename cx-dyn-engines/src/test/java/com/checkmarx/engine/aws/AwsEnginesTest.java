package com.checkmarx.engine.aws;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.amazonaws.services.ec2.model.Instance;
import com.checkmarx.engine.aws.AwsEngines;
import com.checkmarx.engine.domain.DynamicEngine;
import com.checkmarx.engine.domain.DynamicEngine.State;
import com.checkmarx.engine.domain.EngineSize;
import com.google.common.collect.Lists;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AwsEnginesTest {
	
	private static final Logger log = LoggerFactory.getLogger(AwsEnginesTest.class);

	private final boolean runTest = false;  //uncomment next line to run this test
	//private final boolean runTest = true;
	
	@Autowired
	private AwsEngines awsEngines;
	
	private final List<DynamicEngine> runningEngines = Lists.newArrayList();
	
	@Before
	public void setUp() throws Exception {
		log.trace("setUp()");

		assertThat(awsEngines, is(notNullValue()));
	}
	
	@After
	public void tearDown() throws Exception {
		runningEngines.forEach((engine) -> {
			awsEngines.stop(engine, true);
			log.debug("Stopped: {}", engine);
			assertThat(engine.getState(), is(State.UNPROVISIONED));
		});
	}
	
	@Test
	public void testFindEngines() {
		log.trace("testFindEngines()");
		
		final Map<String, Instance> engines = awsEngines.findEngines();
		assertThat(engines, is(notNullValue()));

		engines.forEach((name,instance) -> log.debug("{}", Ec2.print(instance)));
	}

	@Test
	public void testListEngines() {
		log.trace("testListEngines()");
		
		final List<DynamicEngine> engines = awsEngines.listEngines();
		assertThat(engines, is(notNullValue()));
		
		engines.forEach((engine) -> log.debug("{}", engine));
	}

	@Test
	public void testLaunchAndStop() throws Exception {
		log.trace("testLaunchAndStop()");
		
		//Assume.assumeTrue(runTest);

		final String NAME = "cx-engine-test-01";
		
		final EngineSize size = new EngineSize("S", 1, 50000);
		final DynamicEngine engine = new DynamicEngine(NAME, size.getName(), AwsConstants.BILLING_INTERVAL_SECS);
		log.debug("Pre-launch: {}", engine);
		assertThat(engine.getState(), is(State.UNPROVISIONED));
		
		awsEngines.launch(engine, size, true);
		runningEngines.add(engine);
		
		log.debug("Launched: {}", engine);
		assertThat(engine.getState(), is(State.IDLE));
		assertThat(engine.getName(), is(NAME));
		assertThat(engine.getHost(), is(notNullValue()));
		assertThat(engine.getHost().getName(), is(NAME));
		assertThat(engine.getHost().getIp(), is(notNullValue()));
		assertThat(engine.getHost().getCxManagerUrl(), is(notNullValue()));
	
		Thread.sleep(3000);

	}

}
