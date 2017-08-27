package com.checkmarx.engine.domain;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.checkmarx.engine.domain.EnginePool.EnginePoolEntry;
import com.google.common.collect.Sets;

//@Component
public class DefaultEnginePoolBuilder implements EnginePoolBuilder {
	
	private static final Logger log = LoggerFactory.getLogger(DefaultEnginePoolBuilder.class);

	private final Set<EnginePoolEntry> entries = Sets.newHashSet();
	private final Set<DynamicEngine> engines = Sets.newHashSet();
	
	private final String engineNamePrefix;
	private final int expirationIntervalSecs;
	
	public DefaultEnginePoolBuilder(String engineNamePrefix, int expirationIntervalSecs) {
		this.engineNamePrefix = engineNamePrefix;
		this.expirationIntervalSecs = expirationIntervalSecs;
	}

	@Override
	public EnginePoolBuilder addEntry(EnginePoolEntry entry) {
		log.trace("addEntry(): {}", entry);
		entries.add(entry);
		return this;
	}
	
	@Override
	public EnginePoolBuilder addEntry(EngineSize size, int count) {
		entries.add(new EnginePoolEntry(size, count));
		return this;
	}

	@Override
	public EnginePool build() {
		log.trace("build()");
		engines.clear();
		entries.forEach((entry) -> addEngines(entry.getScanSize(), entry.getCount()));
		return new EnginePool(entries, engines);
	}

	private void addEngines(EngineSize size, int count) {
		log.trace("addEngines(): {}, count={}", size, count);
		for(int i = 1; i <= count; i++) {
			final String name = String.format("%s-%s-%02d", engineNamePrefix, size.getName(), i);
			engines.add(new DynamicEngine(name, size.getName(), expirationIntervalSecs));
			log.info("Adding engine to pool; name={}", name);
		}
	}

}
