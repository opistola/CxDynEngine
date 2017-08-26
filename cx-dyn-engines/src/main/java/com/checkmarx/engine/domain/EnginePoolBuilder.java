package com.checkmarx.engine.domain;

import com.checkmarx.engine.domain.EnginePool.EnginePoolEntry;

public interface EnginePoolBuilder {
	
	EnginePoolBuilder addEntry(EnginePoolEntry entry);
	
	@Deprecated
	EnginePoolBuilder addEntry(ScanSize size, int count);
	
	EnginePool build();
	
}
