package com.checkmarx.engine.domain;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.checkmarx.engine.rest.model.ScanRequest;
import com.google.common.base.MoreObjects;

public class ScanQueue {
	
	private final int capacity;
	private final BlockingQueue<ScanRequest> queue;
	
	public ScanQueue(int capacity) {
		this.capacity = capacity;
		this.queue = new ArrayBlockingQueue<ScanRequest>(capacity);
	}

	public BlockingQueue<ScanRequest> getQueue() {
		return queue;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("capacity", capacity)
				.add("queuedCount", queue.size())
				.toString();
	}

}
