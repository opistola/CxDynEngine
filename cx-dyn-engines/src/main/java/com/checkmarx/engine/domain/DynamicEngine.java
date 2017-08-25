/**
 * 
 */
package com.checkmarx.engine.domain;

import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

/**
 * @author rjgeyer
 *
 */
public class DynamicEngine {
	
	private static final Logger log = LoggerFactory.getLogger(DynamicEngine.class);

	public enum State {
		ALL,
		ACTIVE,
		EXPIRING,
		IDLE,
		UNPROVISIONED;
	}
	
	private final String name;
	private final ScanSize size;
	private State state = State.UNPROVISIONED;
	private DateTime currentStateTime = DateTime.now();
	private DateTime timeToExpire;
	private Host host;
	private Map<State, Duration> elapsedTimes = Maps.newConcurrentMap();
	private final int expireDurationSecs;
	private DateTime launchTime;

	public DynamicEngine(String name, ScanSize size, int expireDurationSecs) {
		this.name = name;
		this.size = size;
		this.expireDurationSecs = expireDurationSecs;
		initElapsedTimes();
	}

	private void initElapsedTimes() {
		final Duration zero = new Duration(0);
		for (State state : State.values()) {
			elapsedTimes.put(state, zero);
		}
	}

	public String getName() {
		return name;
	}

	public ScanSize getSize() {
		return size;
	}

	public State getState() {
		return state;
	}

	public Host getHost() {
		return host;
	}
	
	public String getUrl() {
		return host != null ? host.getUrl() : null;
	}
	
	public DateTime getCurrentStateTime() {
		return currentStateTime;
	}
	
	public DateTime getLaunchTime() {
		return launchTime;
	}

	public DateTime getTimeToExpire() {
		return timeToExpire;
	}
	
	public void setState(State state) {
		log.debug("setState(): currentState={}; newState={}", this.state, state);
		
		//sanity check
		if (this.state.equals(state)) {
			final String msg = "Attempting to set DynamicEngine state to current state; state=" + state;
			log.warn(msg);
			throw new IllegalArgumentException(msg);
		}
		
		// before changing state, update current state elapsed time
		final Duration currentDuration = elapsedTimes.get(this.state); 
		elapsedTimes.put(this.state, currentDuration.plus(getElapsedTime()));

		// if current state is UNPROVISIONED, set launch time
		if (this.state.equals(State.UNPROVISIONED)) {
			launchTime = DateTime.now();
			timeToExpire = launchTime.plusSeconds(this.expireDurationSecs);
		}

		// update state
		this.state = state;
		currentStateTime = DateTime.now();
		
		
		// if new state is UNPROVISIONED, clear applicable items
		switch (state) {
			case UNPROVISIONED :
				host = null;
				launchTime = null;
				timeToExpire = null;
				break;
			case IDLE : 
				timeToExpire = calcExpirationTime();
				break;
			case ACTIVE :
				timeToExpire = null;
				break;
			default:
				break;
		}
	}

	DateTime calcExpirationTime() {
		final Duration runTime = getRunTime();
		Long factor = Math.floorMod(runTime.getStandardSeconds(), expireDurationSecs) + 1;
		return launchTime.plusSeconds(factor.intValue() * expireDurationSecs);
	}

	/**
	 * Gets the elapsed time (duration) in the current state
	 * @return Duration since last state transition
	 */
	public Duration getElapsedTime() {
		return new Duration(currentStateTime, DateTime.now());  
	}
	
	/**
	 * Gets the elapsed time (duration) since the engine was launched.
	 * @return Duration since launched
	 */
	public Duration getRunTime() {
		if (launchTime == null) return Duration.ZERO;
		return new Duration(launchTime, DateTime.now());  
	}

	public void setHost(Host server) {
		this.host = server;
	}
	
	public String printElapsedTimes() {
		final StringBuilder sb = new StringBuilder();
		elapsedTimes.forEach((state,duration) -> {
			if (state.equals(State.ALL)) return;
			if (state.equals(this.state)) duration = duration.plus(getElapsedTime()); 
			sb.append(String.format("%s:%ss, ", state, duration.getStandardSeconds()));
		});
		return sb.toString().replaceAll(", $", "");
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("size", size.getName())
				.add("state", state)
				.add("elapsedTime", getElapsedTime().getStandardSeconds())
				.add("launchTime", launchTime)
				.add("runTime", getRunTime().getStandardSeconds())
				.add("currentStateTime", currentStateTime)
				.add("expireDurationSecs", expireDurationSecs)
				.add("timeToExpire", timeToExpire)
				.add("host", host)
				.add("elapsedTimes", "[" + printElapsedTimes() + "]")
				//.omitNullValues()
				.toString();
	}
}
