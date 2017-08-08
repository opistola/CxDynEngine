package com.checkmarx.engine.rest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import com.checkmarx.engine.rest.model.EngineServer;
import com.checkmarx.engine.rest.model.EngineServerResponse;
import com.checkmarx.engine.rest.model.Login;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CxRestClientTests {
	
	private static final Logger log = LoggerFactory.getLogger(CxRestClientTests.class);

	@Autowired
	private CxRestClient restClient;

	@Before
	public void setUp() throws Exception {
		assertThat(restClient, notNullValue());
	}

	@Test
	public void testLogin() {
		log.trace("testLogin()");

		assertThat(login(), is(true));
	}
	
	@Test
	public void testBadLogin() {
		log.trace("testBadLogin()");

		final boolean success = restClient.login(new Login("bogus", "bogus"));
		assertThat(success, is(false));
		
	}
	
	@Test
	public void testGetEngineServers() {
		log.trace("testGetEngineServers()");
		login();
		
		final List<EngineServer> engines = restClient.getEngines();
		
		assertThat(engines, is(notNullValue()));
		assertThat(engines, hasSize(greaterThan(0)));
		
		for (EngineServer eng : engines) {
			log.debug("{}", eng);
		}

		final EngineServer engine = engines.get(0); 
		assertThat(engine.isAlive(), is(true));
		assertThat(engine.isBlocked(), is(false));
		
	}
	
	@Test
	public void testGetEngine() {
		log.trace("testGetEngine()");
		login();
		
		final EngineServer engine = restClient.getEngine(1);
		assertThat(engine, is(notNullValue()));
		assertThat(engine.getId(), is(equalTo(1L)));
	}
	
	@Test(expected=HttpClientErrorException.class)
	public void testGetBadEngine() {
		log.trace("testGetBadEngine()");
		login();
		
		restClient.getEngine(0);
		fail();
	}
	
	@Test
	public void testRegisterEngine() {
		log.trace("testRegisterEngine()");
		login();
		
		final EngineServer engine = new EngineServer("name", "http://engine", 1, 1, 1, true);		
		final EngineServerResponse response = restClient.registerEngine(engine);
		assertThat(response, is(notNullValue()));
		
		log.debug("{}", response);
		
		restClient.unregisterEngine(response.getId());
	}
	
	private boolean login() {
		return restClient.login(new Login("admin@cx", "Im@hom3y!!"));
	}

}
