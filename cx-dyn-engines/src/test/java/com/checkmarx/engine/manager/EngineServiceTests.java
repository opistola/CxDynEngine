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
package com.checkmarx.engine.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

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

	//private final boolean runTest = false;  //uncomment next line to run this test
	private final boolean runTest = true;

	@Autowired
	private EngineService service;
	
	@Before
	public void setUp() throws Exception {
		log.trace("setup()");

		assertThat(service, is(notNullValue()));
	}
	
	@Test
	public void testRun() throws Exception {
		log.trace("testRun()");
		
		Assume.assumeTrue(runTest);

		service.run();
		TimeUnit.MINUTES.sleep(60);
		service.stop();
	}

	@Test
	public void testShutdown() throws Exception {
		log.trace("testShutdown()");
		
		service.run();
		TimeUnit.SECONDS.sleep(5);
		service.stop();
	}

}
