package com.checkmarx.engine;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;

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
public class ConfigTests {
	
	private static final Logger log = LoggerFactory.getLogger(ConfigTests.class);
	
	@Autowired
	private Config config;
	
	@Before
	public void setUp() throws Exception {
		assertThat(config, notNullValue());
	}

	@Test
	public void test() {
		log.trace("test()");
		
		log.info("{}", config);
		
		assertThat(config.getCxEngineUrlPath(), is(not(isEmptyOrNullString())));
		assertThat(config.getPassword(), is(not(isEmptyOrNullString())));
		assertThat(config.getQueueCapacity(), is(greaterThan(0)));
		assertThat(config.getQueueIntervalSecs(), is(greaterThan(0)));
		assertThat(config.getRestUrl(), is(not(isEmptyOrNullString())));
		assertThat(config.getUserName(), is(not(isEmptyOrNullString())));
		assertThat(config.getTimeoutSecs(), is(greaterThan(0)));
	}
	
}
