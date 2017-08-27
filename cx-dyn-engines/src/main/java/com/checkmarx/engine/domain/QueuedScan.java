package com.checkmarx.engine.domain;

import com.checkmarx.engine.rest.model.ScanRequest;
import com.google.common.base.MoreObjects;

public class QueuedScan {

	private final EngineSize size;
	private final ScanRequest scanRequest;
	
	public QueuedScan(EngineSize size, ScanRequest scanJob) {
		this.size = size;
		this.scanRequest = scanJob;
	}

	public EngineSize getSize() {
		return size;
	}

	public ScanRequest getScanJob() {
		return scanRequest;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("size", size.getName())
				.add("scanRequest", scanRequest)
				.toString();
	}
	
	
	
}
