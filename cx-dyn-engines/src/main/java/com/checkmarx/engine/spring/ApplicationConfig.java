package com.checkmarx.engine.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.checkmarx.engine.Config;
import com.checkmarx.engine.aws.AwsConstants;
import com.checkmarx.engine.domain.DefaultEnginePoolBuilder;
import com.checkmarx.engine.domain.EnginePool;
import com.checkmarx.engine.domain.ScanQueue;
import com.checkmarx.engine.domain.EngineSize;
import com.checkmarx.engine.domain.EnginePool.EnginePoolEntry;
import com.checkmarx.engine.manager.EngineMonitor;
import com.checkmarx.engine.manager.EngineProvisioner;
import com.checkmarx.engine.manager.QueueMonitor;
import com.checkmarx.engine.rest.CxRestClient;
import com.fasterxml.jackson.datatype.joda.JodaModule;

@Configuration
public class ApplicationConfig {
	
	public static final EngineSize SMALL = new EngineSize("S", 0, 99999);
	public static final EngineSize MEDIUM = new EngineSize("M", 100000, 499999);
	public static final EngineSize LARGE = new EngineSize("L", 500000, 999999999);
	
	@Bean
	public JodaModule jacksonJodaModule() {
		return new JodaModule();
	}
	
	@Bean
	public ScanQueue scansQueued(Config config) {
		return new ScanQueue(config.getQueueCapacity());
	}
	
	@Bean
	public ScanQueue scansFinished(Config config) {
		return new ScanQueue(config.getQueueCapacity());
	}
	
	@Bean
	public EnginePool enginePool(Config config) {
		//TODO: add configurable engine pool entries
		return new DefaultEnginePoolBuilder(config.getEnginePoolPrefix(), 
				AwsConstants.BILLING_INTERVAL_SECS)
			.addEntry(new EnginePoolEntry(SMALL, 3))
			.addEntry(new EnginePoolEntry(MEDIUM, 3))
			.addEntry(new EnginePoolEntry(LARGE, 3))
			.build();
	}
	
	@Bean
	public EngineMonitor engineMonitor(EnginePool enginePool,
			CxRestClient cxClient,
			EngineProvisioner engineProvisioner, Config config,
			ScanQueue scansQueued, ScanQueue scansFinished) {
		
		return new EngineMonitor(enginePool, cxClient, engineProvisioner, 
						scansQueued.getQueue(), scansFinished.getQueue());
	}
	
	@Bean
	public QueueMonitor queueMonitor(CxRestClient cxClient, Config config,
			ScanQueue scansQueued, ScanQueue scansFinished) {
		//final ScanQueue scansQueued = new ScanQueue(config.getQueueCapacity());
		//final ScanQueue scansFinished = new ScanQueue(config.getQueueCapacity());
		
		return new QueueMonitor(scansQueued.getQueue(), scansFinished.getQueue(), cxClient);
	}
	
	/*
	@Bean
	public EnginePool enginePool() {
		final ScanSize small = new ScanSize("S", 0, 99999);
		final List<DynamicEngine> engines = Lists.newArrayList();
		engines.add(new DynamicEngine("aws-cxengine-small-1", small));
		engines.add(new DynamicEngine("aws-cxengine-small-2", small));
		return new EnginePool(engines);
	}
	
	@Bean
	public BlockingQueue<ScanEvent> scanQueue() {
		return new ArrayBlockingQueue<ScanEvent>(1000, true);
	}
	*/
}
