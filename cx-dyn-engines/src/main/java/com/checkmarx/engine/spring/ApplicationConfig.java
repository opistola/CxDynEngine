package com.checkmarx.engine.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.datatype.joda.JodaModule;

@Configuration
public class ApplicationConfig {

	/*
	@Bean
	public Config config() {
		return new Config();
	}
	*/
	
	@Bean
	public JodaModule jacksonJodaModule() {
		return new JodaModule();
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
