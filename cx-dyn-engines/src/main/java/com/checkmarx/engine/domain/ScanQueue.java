package com.checkmarx.engine.domain;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.checkmarx.engine.rest.model.ScanRequest;
import com.google.common.base.MoreObjects;

public class ScanQueue {
	
	private final int capacity;
	private final ScanSize size;
	private final BlockingQueue<ScanRequest> queue;
	
	public ScanQueue(int capacity, ScanSize size) {
		this.capacity = capacity;
		this.size = size;
		this.queue = new ArrayBlockingQueue<ScanRequest>(capacity);
	}

	public BlockingQueue<ScanRequest> getQueue() {
		return queue;
	}

	public ScanSize getSize() {
		return size;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("capcity", capacity)
				.add("scanSize", size)
				.add("queuedCount", queue.size())
				.toString();
	}

}
