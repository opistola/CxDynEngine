package com.checkmarx.engine.manager;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.checkmarx.engine.domain.DynamicEngine;
import com.checkmarx.engine.domain.DynamicEngine.State;
import com.checkmarx.engine.domain.EnginePool;
import com.checkmarx.engine.domain.EngineSize;
import com.checkmarx.engine.rest.CxRestClient;
import com.checkmarx.engine.rest.model.EngineServer;
import com.checkmarx.engine.rest.model.ScanRequest;
import com.google.common.collect.Maps;

public class EngineMonitor implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(EngineMonitor.class);

	private final CxRestClient cxClient;
	private final EnginePool pool;
	private final EngineProvisioner engineProvisioner;
	private final BlockingQueue<ScanRequest> scansQueued;
	private final BlockingQueue<ScanRequest> scansFinished;
	private final BlockingQueue<DynamicEngine> enginesExpiring;
	//private final EngineLauncher engineLauncher;

	/**
	 * map of scans assigned to engines; key=Scan.RunId, value=cxEngineId
	 */
	private final Map<String, Long> engineScans = Maps.newConcurrentMap();
	
	/**
	 * map of registered cx engine servers, key=cxEngineId
	 */
	private Map<Long, DynamicEngine> cxEngines = Maps.newConcurrentMap();
	
	/**
	 * map of active (scanning) cx engine servers, key=engineId
	 */
	private Map<Long, EngineServer> activeEngines = Maps.newConcurrentMap();

	public EngineMonitor(EnginePool pool, 
			CxRestClient cxClient,
			EngineProvisioner engineProvisioner,
			BlockingQueue<ScanRequest> scansQueued,
			BlockingQueue<ScanRequest> scansFinished) {
		this.pool = pool;
		this.cxClient = cxClient;
		this.engineProvisioner = engineProvisioner;
		this.scansQueued = scansQueued;
		this.scansFinished = scansFinished;
		this.enginesExpiring =  new ArrayBlockingQueue<DynamicEngine>(pool.getEngineCount());
		//this.engineLauncher = new EngineLauncher();
	}

	@Override
	public void run() {
		log.info("run()");
		
		final ExecutorService launcher = Executors.newSingleThreadExecutor();
		launcher.execute(new EngineLauncher());
		
		final ExecutorService terminator = Executors.newSingleThreadExecutor();
		terminator.execute(new EngineTerminator());

		//TODO: implement background task to expire engines 
	}	
	public class EngineLauncher implements Runnable {

		@Override
		public void run() {
			log.info("EngineLauncher.run()");
			
			try {
				registerQueuingEngine();
				while (true) {
					// blocks until scan available
					log.debug("EngineLauncher: waiting for scan");
					ScanRequest scan = scansQueued.take();
					Runnable task = new Runnable() {
						@Override
						public void run() {
							onScanQueued(scan);
						}
					};
					
					final ExecutorService executor = Executors.newSingleThreadExecutor();
					executor.execute(task);
				}
			} catch (InterruptedException e) {
				log.info("EngineLauncher interrupted: message={}", e.getMessage());
			}
		}
		
		public void registerQueuingEngine() {
			log.debug("registerQueueEngine()");
			
			EngineServer engine = cxClient.getEngine(1L);
			engine.setBlocked(true);
			engine = cxClient.updateEngine(engine);
			
			log.info("Queueing engine registered: {}", engine);
		}
		
		public void onScanQueued(ScanRequest scan) {
			log.debug("onScanQueued() : {}", scan);

			final EngineSize size = calcScanSize(scan);
			if (size == null) {
				log.error("Invalid scan size; {}", scan); 
				return;
			}
			
			if (allocateIdleEngine(scan, size)) return;
			
			if (checkActiveEngines(scan, size)) return;
			
			if (allocateNewEngine(scan, size)) return;

			log.debug("No engine available for scan: scan={}", scan);

		}

		private EngineSize calcScanSize(ScanRequest scan) {
			return pool.calcEngineSize(scan.getLoc());
		}

		private boolean allocateIdleEngine(ScanRequest scan, EngineSize size) {
			log.trace("allocateIdleEngine() : {}; size={}", scan, size);

			final State state = State.IDLE;
			final DynamicEngine engine = pool.allocateEngine(size, state);
			
			if (engine == null) return false;
			
			registerEngine(scan, engine, state);
			return true;
		}
		
		private boolean allocateNewEngine(ScanRequest scan, EngineSize size) {
			log.trace("allocateNewEngine() : {}; size={}", scan, size);

			final State state = DynamicEngine.State.UNPROVISIONED;
			final DynamicEngine engine = pool.allocateEngine(size, state);
			
			if (engine == null) return false;

			// blocks while engine is spinning up
			engineProvisioner.launch(engine, size, true);
			
			registerEngine(scan, engine, state);
			return true;
		}
		
		private void registerEngine(ScanRequest scan, DynamicEngine engine, State fromState) {
			log.trace("registerEngine() : {}; {}; fromState={}", scan, engine, fromState);
			
			final String id = scan.getRunId();
			EngineServer cxEngine = createEngine(engine.getName(), scan, engine.getUrl());  
			cxEngine = registerCxEngine(id, cxEngine);
			cxEngines.put(cxEngine.getId(), engine);

			engine.setScanRunId(id);
			log.info("Engine allocated for scan: fromState={}; engine={}; scan={}", fromState, engine, scan);
		}
		

		private EngineServer registerCxEngine(final String scanId, final EngineServer cxServer) {
			log.trace("registerCxEngine()");
			
			final EngineServer cxEngine = cxClient.registerEngine(cxServer);
			log.info("Engine registered: scanId={}; engine={}", scanId, cxEngine);
			
			long engineId = cxEngine.getId();
			engineScans.put(scanId, engineId);
			activeEngines.put(engineId, cxEngine);
			return cxEngine;
		}

		private EngineServer createEngine(String name, ScanRequest scan, String url) {
			//FIXME: pull from config
			final String prefix = "**";
			final String engineName = String.format("%s%s", prefix, name);
			final int size = scan.getLoc(); 
			return new EngineServer(engineName, url, size, size, 1, false);
		}

		private boolean checkActiveEngines(ScanRequest scan, EngineSize size) {
			log.trace("checkActiveEngines() : {}; size={}", scan, size);

			//final State state = State.SCANNING;
			//final DynamicEngine engine = pool.allocateEngine(size, state);
			
			//TODO: implement checkActiveEngines
			return false;
		}

	}

	public class EngineTerminator implements Runnable {

		@Override
		public void run() {
			log.info("EngineTerminator.run()");
			
			try {
				while (true) {
					// blocks until scan available
					log.debug("EngineTerminator: waiting for scan");
					ScanRequest scan = scansFinished.take();
					Runnable task = new Runnable() {
						@Override
						public void run() {
							onScanFinished(scan);
						}

					};
					Thread t = new Thread(task);
					t.start();
				}
			} catch (InterruptedException e) {
				log.info("EngineTerminator interrupted: message={}", e.getMessage());
			}

		}

		private void onScanFinished(ScanRequest scan) {
			log.debug("onScanFinished() : {}", scan);
			
			final String runId = scan.getRunId(); 
			final Long engineId = engineScans.get(runId);
			final DynamicEngine engine = cxEngines.get(engineId);
			cxClient.unregisterEngine(engineId);
			pool.deallocateEngine(engine);
			engineProvisioner.stop(engine);
			engineScans.remove(runId);
			cxEngines.remove(engineId);
			
			log.info("Engine deallocated for scan: engine={}; scan={}", engine, scan);
		}
		
	}

}
