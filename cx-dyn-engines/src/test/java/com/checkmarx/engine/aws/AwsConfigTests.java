package com.checkmarx.engine.aws;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;

import java.util.Map;

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
public class AwsConfigTests {
	
	private static final Logger log = LoggerFactory.getLogger(AwsConfigTests.class);

	@Autowired
	private AwsConfig config;
	
	@Before
	public void setUp() throws Exception {
		assertThat(config, notNullValue());
	}

	@Test
	public void test() {
		log.trace("test()");
		
		log.info("{}", config);
		
		assertThat(config.getIamProfile(), is(not(isEmptyOrNullString())));
		assertThat(config.getImageId(), is(not(isEmptyOrNullString())));
		assertThat(config.getKeyName(), is(not(isEmptyOrNullString())));
		assertThat(config.getSecurityGroup(), is(not(isEmptyOrNullString())));
		assertThat(config.getSubnetId(), is(not(isEmptyOrNullString())));

		final Map<String, String> sizeMap = config.getEngineTypeMap();
		assertThat(sizeMap, notNullValue());
		assertThat(sizeMap.values(), hasSize(greaterThan(0)));
	}

}
