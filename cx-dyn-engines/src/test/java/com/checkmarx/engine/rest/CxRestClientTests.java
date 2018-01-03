/*******************************************************************************
 * Copyright (c) 2017 Checkmarx
 * 
 * This software is licensed for customer's internal use only.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.checkmarx.engine.rest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.junit.Assert.fail;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;

import com.checkmarx.engine.SpringUnitTest;
import com.checkmarx.engine.rest.model.EngineServer;
import com.checkmarx.engine.rest.model.Login;
import com.checkmarx.engine.rest.model.ScanRequest;

@TestPropertySource("/application-test.properties")
public class CxRestClientTests extends SpringUnitTest {
	
	private static final Logger log = LoggerFactory.getLogger(CxRestClientTests.class);

	@Autowired
	private CxRestClient cxClient;
	
	@Value("${cx.test-engine-url}")
	private String engineUrl;
	
	private final List<Long> engineIds = Lists.newArrayList();

	@Before
	public void setUp() throws Exception {
		
		Assume.assumeTrue(super.runIntegrationTests());
		
		assertThat(cxClient, notNullValue());
		assertThat(engineUrl, is(not(isEmptyOrNullString())));
		log.debug("engineUrl={}", engineUrl);

		assertThat(login(), is(true));
	}
	
	@After
	public void tearDown() throws Exception {
		for (Long id : engineIds) {
			cxClient.unregisterEngine(id);
		}
	}

	@Test
	public void testBadLogin() {
		log.trace("testBadLogin()");

		final boolean success = cxClient.login(new Login("bogus", "bogus"));
		assertThat(success, is(false));
	}
	
	@Test
	public void testGetEngineServers() {
		log.trace("testGetEngineServers()");
		
		final List<EngineServer> engines = cxClient.getEngines();
		
		assertThat(engines, is(notNullValue()));
		assertThat(engines, hasSize(greaterThan(0)));
		
		for (EngineServer eng : engines) {
			log.debug("{}", eng);
		}

		final EngineServer engine = engines.get(0);
		assertThat(engine.getName().toUpperCase(), is("Localhost".toUpperCase()));
		//assertThat(engine.isAlive(), is(true));
		//assertThat(engine.isBlocked(), is(false));
		
	}
	
	@Test
	public void testGetEngine() {
		log.trace("testGetEngine()");
		
		final EngineServer engine = cxClient.getEngine(1);
		assertThat(engine, is(notNullValue()));
		assertThat(engine.getId(), is(equalTo(1L)));
	}
	
	@Test(expected=HttpClientErrorException.class)
	public void testGetBadEngine() {
		log.trace("testGetBadEngine()");
		
		cxClient.getEngine(0);
		fail();
	}
	
	@Test
	public void testRegisterEngine() {
		log.trace("testRegisterEngine()");
		
		final EngineServer engine1 = createEngine();
		final EngineServer engine2 = registerEngine(engine1);
		
		log.debug("{}", engine2);

		assertThat(engine2, is(notNullValue()));
		this.engineIds.add(engine2.getId());
		
		assertThat(engine2.getId(), is(greaterThan(1L)));
		assertThat(engine2.getMinLoc(), is(equalTo(engine1.getMinLoc())));
		assertThat(engine2.getMaxLoc(), is(equalTo(engine1.getMaxLoc())));
		assertThat(engine2.getMaxScans(), is(equalTo(engine1.getMaxScans())));
		assertThat(engine2.getUri(), is(equalTo(engine1.getUri())));
		assertThat(engine2.isBlocked(), is(equalTo(engine1.isBlocked())));
		assertThat(engine2.isAlive(), is(equalTo(false)));
	}
	
	@Test
	public void testUpdateEngine() {
		log.trace("testUpdateEngine()");
		
		final EngineServer engine1 = registerEngine(createEngine());

		log.debug("{}", engine1);

		engine1.setBlocked(!engine1.isBlocked());
		engine1.setMinLoc(10);
		engine1.setMaxLoc(10);
		final EngineServer engine2 = cxClient.updateEngine(engine1);
		
		log.debug("{}", engine2);
		this.engineIds.add(engine2.getId());

		assertThat(engine2.isBlocked(), is(equalTo(engine1.isBlocked())));
		assertThat(engine2.getMinLoc(), is(equalTo(engine1.getMinLoc())));
		assertThat(engine2.getMaxLoc(), is(equalTo(engine1.getMaxLoc())));
	}
	
	@Test
	public void testGetScansQueue() {
		log.trace("testGetScansQueue()");

		final List<ScanRequest> scans = cxClient.getScansQueue();
		assertThat(scans, is(notNullValue()));
		for (ScanRequest scan : scans) {
			log.debug("{}", scan);
		}
	}
	
	@Test
	public void testPingEngine() {
		log.trace("testPingEngine()");

		assertThat(cxClient.pingEngine(engineUrl), is(true));
	}
	

	private EngineServer registerEngine(EngineServer engine) {
		return cxClient.registerEngine(engine);
	}
	
	private static final String ENGINE_URI = "http://engine/";
	private EngineServer createEngine() {
		return new EngineServer("name", ENGINE_URI, 1, 1, 1, true);
	}
	
	private boolean login() {
		return cxClient.login(new Login("admin@cx", "Im@hom3y!!"));
	}

}
