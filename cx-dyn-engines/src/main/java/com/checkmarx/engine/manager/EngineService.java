package com.checkmarx.engine.manager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.checkmarx.engine.Config;
import com.checkmarx.engine.domain.DynamicEngine;
import com.checkmarx.engine.domain.EnginePool;
import com.checkmarx.engine.rest.CxRestClient;
import com.checkmarx.engine.utils.ExecutorServiceUtils;
import com.google.common.collect.Lists;

@Component
public class EngineService implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(EngineService.class);

	private final CxRestClient cxClient;
	private final Config config;
	private final EnginePool enginePool;
	private final EngineProvisioner engineProvisioner;
	private final QueueMonitor queueMonitor;
	private final EngineManager engineController;

	private final List<Future<?>> tasks = Lists.newArrayList();
	private final ExecutorService monitorExecutor;
	private final ScheduledExecutorService queueExecutor;

	public EngineService(CxRestClient cxClient, EngineProvisioner engineProvisioner, Config config,
			QueueMonitor queueMonitor, EngineManager engineMonitor, EnginePool enginePool) {
		this.cxClient = cxClient;
		this.config = config;
		this.engineProvisioner = engineProvisioner;
		this.queueMonitor = queueMonitor;
		this.engineController = engineMonitor;
		this.enginePool = enginePool;
		this.monitorExecutor = ExecutorServiceUtils.buildSingleThreadExecutorService("eng-service-%d", true);
		this.queueExecutor = ExecutorServiceUtils.buildScheduledExecutorService("queue-mon-%d", true);
		
		log.info("ctor(): {}; {}; {}", this.enginePool, this.cxClient, this.config);
	}
	
	@Override
	public void run() {
		log.info("run()");
	
		try {
		
			cxClient.login();
			updateEngines();

			tasks.add(monitorExecutor.submit(engineController));
			tasks.add(queueExecutor.scheduleAtFixedRate(queueMonitor, 0L, config.getQueueIntervalSecs(), TimeUnit.SECONDS));

		} catch (Throwable t) {
			log.error("Error occurred while launching Engine services, shutting down; cause={}; message={}", 
					t, t.getMessage(), t); 
			shutdown();
		}
	}
	
	public void stop() {
		log.info("stop()");
		shutdown();
	}
	
	private void shutdown() {
		log.info("shutdown()");

		engineController.stop();
		
		tasks.forEach((task) -> {
			task.cancel(true);
		});
		monitorExecutor.shutdown();
		queueExecutor.shutdown();
		try {
			if (!monitorExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
				monitorExecutor.shutdownNow();
			}
			if (!queueExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
				queueExecutor.shutdownNow();
			}
		} catch (InterruptedException e) {
			monitorExecutor.shutdownNow();
			queueExecutor.shutdownNow();
		}
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
