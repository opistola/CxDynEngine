package com.checkmarx.engine.manager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.checkmarx.engine.Config;
import com.checkmarx.engine.domain.DynamicEngine;
import com.checkmarx.engine.domain.EnginePool;
import com.checkmarx.engine.rest.CxRestClient;

@Component
public class EngineService implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(EngineService.class);

	private final CxRestClient cxClient;
	private final Config config;
	private final EnginePool enginePool;
	private final EngineProvisioner engineProvisioner;
	private final QueueMonitor queueMonitor;
	private final EngineMonitor engineMonitor;

	public EngineService(CxRestClient cxClient, EngineProvisioner engineProvisioner, Config config,
			QueueMonitor queueMonitor, EngineMonitor engineMonitor, EnginePool enginePool) {
		this.cxClient = cxClient;
		this.config = config;
		this.engineProvisioner = engineProvisioner;
		this.queueMonitor = queueMonitor;
		this.engineMonitor = engineMonitor;
		this.enginePool = enginePool;
		
		log.info("ctor(): {}; {}; {}", this.enginePool, this.cxClient, this.config);
	}
	
	@Override
	public void run() {
		log.info("run()");
	
		cxClient.login();

		updateEngines();
		startEngineMonitor();
		startQueueMonitor();
	}
	
	private void startEngineMonitor() {
		log.debug("startEngineMonitor()");
		
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(engineMonitor);
	}

	private void startQueueMonitor() {
		log.debug("startQueueMonitor()");
		
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(queueMonitor, 0L, config.getQueueIntervalSecs(), TimeUnit.SECONDS);
	}

	private void updateEngines() {
		log.debug("updateEngines()");

		final List<DynamicEngine> provisionedEngines = engineProvisioner.listEngines();
		provisionedEngines.forEach((engine) -> {
			final String name = engine.getName();
			final DynamicEngine existingEngine = enginePool.getEngineByName(name);
			if (existingEngine != null) {
				enginePool.replaceEngine(engine);
			} else {
				// terminate unknown engines
				engineProvisioner.stop(engine, true);
			}
		});
		
	}
	
}
