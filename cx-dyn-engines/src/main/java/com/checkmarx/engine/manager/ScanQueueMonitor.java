/*******************************************************************************
 * Copyright (c) 2017 Checkmarx
 * 
 * This software is licensed for customer's internal use only.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.checkmarx.engine.manager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.checkmarx.engine.rest.CxRestClient;
import com.checkmarx.engine.rest.model.ScanRequest;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ScanQueueMonitor implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(ScanQueueMonitor.class);

	private final BlockingQueue<ScanRequest> scanQueued;
	//private final BlockingQueue<ScanRequest> scanWorking;
	private final BlockingQueue<ScanRequest> scanFinished;
	
	/**
	 * Map of active scan requests by Scan.Id
	 */
	private final Map<Long,ScanRequest> activeScans = Maps.newHashMap();
	private final List<Long> workingScans = Lists.newArrayList();
	private final CxRestClient cxClient;
	
	public ScanQueueMonitor(
			BlockingQueue<ScanRequest> scanQueued, 
			//BlockingQueue<ScanRequest> scanWorking,
			BlockingQueue<ScanRequest> scanFinished,
			CxRestClient cxClient) {
		this.scanQueued = scanQueued;
		//this.scanWorking = scanWorking;
		this.scanFinished = scanFinished;
		this.cxClient = cxClient;
	}

	@Override
	public void run() {
		log.debug("run()");
		
		final List<ScanRequest> queue = cxClient.getScansQueue();
		queue.forEach((scan) -> processScan(scan)); 
		
		//TODO: check for missing scans and treat as finished
	}

	private void processScan(ScanRequest scan) {
		log.debug("processScan(): {}", scan);
		
		final long scanId = scan.getId();
		switch (scan.getStatus()) {
			case Queued:
				onQueued(scanId, scan);
				break;
			case Scanning :
				onScanning(scanId, scan);
				break;
			case Canceled :
			case Deleted :
			case Failed :
			case Finished :
				onCompleted(scanId, scan);
				break;
			default:
				onOther(scan);
				break;
		}
	}

	private void onQueued(final long scanId, ScanRequest scan) {
		log.trace("onQueued(): {}", scan);

		if (!activeScans.containsKey(scanId)) {
			log.debug("scan queued, adding to scanQueued queue; id={}", scanId);
			scanQueued.add(scan);
			log.trace("scan added to scanQueued; count={}; id={}", scanQueued.size(), scanId);
			activeScans.put(scanId, scan);
			log.info("Scan queued: {}; queuedCount={}", scan, scanQueued.size());
		}
	}

	private void onScanning(final long scanId, ScanRequest scan) {
		log.trace("onScanning(): {}", scan);

		// only process working scans once, so we add to workingScans after processing
		if (activeScans.containsKey(scanId) && !workingScans.contains(scanId)) {
			// update active scan
			activeScans.put(scanId, scan);

			//scanWorking.add(scan);
			//FIXME: move to EngineManager
			final long engineId = scan.getEngineId();
			log.info("Scan is working, blocking engine; scanId={}; engineId={}", scanId, engineId);
			cxClient.blockEngine(engineId);
			workingScans.add(scanId);
		}
	}

	private void onCompleted(final long scanId, ScanRequest scan) {
		log.trace("onCompleted(): {}", scan);
		
		if (activeScans.remove(scanId) != null ) {
			log.debug("scan complete, adding to scanFinished queue; id={}", scanId);
			workingScans.remove(scanId);
			scanFinished.add(scan);
			log.info("Scan finished: {}; queuedCount={}", scan, scanQueued.size());
		}
	}

	private void onOther(ScanRequest scan) {
		log.trace("onOther(): {}", scan);
		// do nothing
	}

}
