package com.checkmarx.engine.scans;

public class ScanEvent {
	
	public enum EventType {
		
		QUEUED,
		FINISHED
	}
	
	private EventType eventType; 

}
