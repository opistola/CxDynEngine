package com.checkmarx.engine.domain;

import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

//@Component
public class DefaultEnginePoolBuilder implements EnginePoolBuilder {
	
	private static final Logger log = LoggerFactory.getLogger(DefaultEnginePoolBuilder.class);

	private final Map<ScanSize, Integer> sizeMap = Maps.newHashMap();
	private final List<DynamicEngine> engines = Lists.newArrayList();
	
	private final String engineNamePrefix;
	private final int expirationIntervalSecs;
	
	public DefaultEnginePoolBuilder(String engineNamePrefix, int expirationIntervalSecs) {
		this.engineNamePrefix = engineNamePrefix;
		this.expirationIntervalSecs = expirationIntervalSecs;
	}

	@Override
	public EnginePoolBuilder addSize(ScanSize size, int count) {
		log.trace("addSize(): {}; count={}", size, count);
		
		sizeMap.put(size, count);
		return this;
	}

	@Override
	public EnginePool build() {
		log.trace("build()");
		engines.clear();
		sizeMap.forEach((size,count) -> addEngines(size, count));
		return new EnginePool(engines);
	}

	private void addEngines(ScanSize size, int count) {
		log.trace("addEngines(): {}, count={}", size, count);
		for(int i = 1; i <= count; i++) {
			final String name = String.format("%s-%s-%02d", engineNamePrefix, size.getName(), i);
			engines.add(new DynamicEngine(name, size, expirationIntervalSecs));
			log.info("Adding engine to pool; name={}", name);
		}
	}
}
