package com.checkmarx.engine.manager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.checkmarx.engine.rest.CxRestClient;
import com.checkmarx.engine.rest.model.ScanRequest;
import com.checkmarx.engine.rest.model.ScanRequest.ScanStatus;
import com.google.common.collect.Maps;

public class QueueMonitor implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(QueueMonitor.class);

	private final BlockingQueue<ScanRequest> scanQueued;
	private final BlockingQueue<ScanRequest> scanFinished;
	private final Map<Long,ScanRequest> scans = Maps.newHashMap();
	private final CxRestClient cxClient;
	
	public QueueMonitor(BlockingQueue<ScanRequest> scanQueued, BlockingQueue<ScanRequest> scanFinished,
			CxRestClient cxClient) {
		this.scanQueued = scanQueued;
		this.scanFinished = scanFinished;
		this.cxClient = cxClient;
	}

	@Override
	public void run() {
		log.debug("run()");
		
		final List<ScanRequest> queue = cxClient.getScansQueue();
		queue.forEach((scan) -> checkScanStatus(scan)); 
	}

	private void checkScanStatus(ScanRequest scan) {
		log.trace("checkScanStatus(): {}", scan);
		
		if (isNewScan(scan)) {
			scanQueued.add(scan);
			scans.put(scan.getId(), scan);
			log.info("Scan queued: {}; queuedCount={}", scan, scanQueued.size());
		} else if (isFinishedScan(scan)) {
			scanFinished.add(scan);
			scans.remove(scan.getId());
			log.info("Scan finished: {}; queuedCount={}", scan, scanQueued.size());
		} else {
			updateScan(scan);
		}
	}

	private boolean isNewScan(ScanRequest scan) {
		log.trace("isNewScan(): {}", scan);
		return !scans.containsKey(scan.getId()) && isQueued(scan.getStatus());
	}
	
	private boolean isFinishedScan(ScanRequest scan) {
		log.trace("isFinishedScan(): {}", scan);
		return scans.containsKey(scan.getId()) && isDone(scan.getStatus());
	}

	private boolean isDone(ScanStatus status) {
		switch (status) {
			case Canceled :
			case Deleted :
			case Failed :
			case Finished :
				
				return true;
	
			default:
				return false;
		}
	}

	private boolean isQueued(ScanStatus status) {
		return status.equals(ScanStatus.Queued);
	}

	private void updateScan(ScanRequest scan) {
		log.trace("updateScan(): {}", scan);
		long id = scan.getId();
		if (scans.containsKey(id)) {
			scans.put(id, scan);
		}
	}

}