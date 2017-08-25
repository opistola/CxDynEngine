package com.checkmarx.engine.domain;

public interface EnginePoolBuilder {
	
	EnginePoolBuilder addSize(ScanSize size, int count);
	
	EnginePool build();
	
}
