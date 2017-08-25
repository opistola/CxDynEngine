/**
 * 
 */
package com.checkmarx.engine.domain;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import com.checkmarx.engine.aws.AwsConstants;

/**
 * @author rjgey
 *
 */
@RunWith(SpringRunner.class)
public class EnginePoolTests {

	private static final Logger log = LoggerFactory.getLogger(EnginePoolTests.class);

	private EnginePool pool;
	
	public static final ScanSize SMALL = new ScanSize("S", 0, 99999);
	public static final ScanSize MEDIUM = new ScanSize("M", 100000, 499999);
	public static final ScanSize LARGE = new ScanSize("L", 500000, 999999999);
	
	@Before
	public void setUp() throws Exception {
		log.trace("setup()");

		pool = new DefaultEnginePoolBuilder("cx-engine", AwsConstants.BILLING_INTERVAL_SECS)
			.addSize(SMALL, 3)
			.addSize(MEDIUM, 3)
			.addSize(LARGE, 3)
			.build();
	}

	@Test
	public void testInit() {
		log.trace("testInit()");
		
		pool.logEngines();
		log.debug("{}", pool);

		assertEquals(3, pool.getAllEngines().size());
		assertEquals(3, pool.getAllEngines().get(SMALL.getName()).size());
		assertEquals(3, pool.getAllEngines().get(MEDIUM.getName()).size());
		assertEquals(3, pool.getAllEngines().get(LARGE.getName()).size());

		assertEquals(3, pool.getUnprovisionedEngines().size());
		assertEquals(3, pool.getUnprovisionedEngines().get(SMALL.getName()).size());
		assertEquals(3, pool.getUnprovisionedEngines().get(MEDIUM.getName()).size());
		assertEquals(3, pool.getUnprovisionedEngines().get(LARGE.getName()).size());
		
		assertEquals(3, pool.getActiveEngines().size());
		assertEquals(0, pool.getActiveEngines().get(SMALL.getName()).size());
		assertEquals(0, pool.getActiveEngines().get(MEDIUM.getName()).size());
		assertEquals(0, pool.getActiveEngines().get(LARGE.getName()).size());

		assertEquals(3, pool.getExpiringEngines().size());
		assertEquals(0, pool.getExpiringEngines().get(SMALL.getName()).size());
		assertEquals(0, pool.getExpiringEngines().get(MEDIUM.getName()).size());
		assertEquals(0, pool.getExpiringEngines().get(LARGE.getName()).size());

		assertEquals(3, pool.getIdleEngines().size());
		assertEquals(0, pool.getIdleEngines().get(SMALL.getName()).size());
		assertEquals(0, pool.getIdleEngines().get(MEDIUM.getName()).size());
		assertEquals(0, pool.getIdleEngines().get(LARGE.getName()).size());
	}
	
	
	@Test
	public void testChangeState() throws InterruptedException {
		log.trace("testChangeState()");
		
		DynamicEngine engine = pool.getUnprovisionedEngines().get(SMALL.getName()).get(0);
		String size = engine.getSize();
		
		pool.changeState(engine, DynamicEngine.State.SCANNING);
		assertEquals(1, pool.getActiveEngines().get(size).size());
		assertEquals(2, pool.getUnprovisionedEngines().get(size).size());
		assertEquals(DynamicEngine.State.SCANNING, engine.getState());
		
		Thread.sleep(100);
		assertTrue(engine.getElapsedTime().getMillis() >= 100);

		pool.logEngines();
	}

	@Test
	public void testCalcSize() {
		log.trace("testCalcSize()");
		
		assertEquals(SMALL, pool.calcEngineSize(0));
		assertEquals(SMALL, pool.calcEngineSize(1));
		assertEquals(SMALL, pool.calcEngineSize(99999));
		assertEquals(MEDIUM, pool.calcEngineSize(100000));
		assertEquals(MEDIUM, pool.calcEngineSize(499999));
		assertEquals(LARGE, pool.calcEngineSize(500000));
		assertEquals(LARGE, pool.calcEngineSize(999999999));
		assertNull(pool.calcEngineSize(100000000000L));
	}

}
