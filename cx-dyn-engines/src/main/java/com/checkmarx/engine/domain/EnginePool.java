package com.checkmarx.engine.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.checkmarx.engine.domain.DynamicEngine.State;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EnginePool {

	private static final Logger log = LoggerFactory.getLogger(EnginePool.class);
	
	//immutable after initialization
	private final Map<ScanSize, List<DynamicEngine>> allEngines = Maps.newLinkedHashMap();
	private final Map<ScanSize, List<DynamicEngine>> activeEngines = Maps.newConcurrentMap();
	private final Map<ScanSize, List<DynamicEngine>> idleEngines = Maps.newConcurrentMap();
	private final Map<ScanSize, List<DynamicEngine>> expiringEngines = Maps.newConcurrentMap();
	private final Map<ScanSize, List<DynamicEngine>> unprovisionedEngines = Maps.newConcurrentMap();
	
	private final Map<DynamicEngine.State, Map<ScanSize, List<DynamicEngine>>> engineMaps 
		= Maps.newEnumMap(DynamicEngine.State.class);

	//immutable after initialization
	private final Map<ScanSize, AtomicLong> engineSizes = Maps.newLinkedHashMap();
	
	protected EnginePool() {
		engineMaps.put(DynamicEngine.State.ALL, allEngines);
		engineMaps.put(DynamicEngine.State.ACTIVE, activeEngines);
		engineMaps.put(DynamicEngine.State.EXPIRING, expiringEngines);
		engineMaps.put(DynamicEngine.State.IDLE, idleEngines);
		engineMaps.put(DynamicEngine.State.UNPROVISIONED, unprovisionedEngines);
	}
	
	public EnginePool(List<DynamicEngine> engines) {
		this();
		engines.forEach(engine->addEngine(engine));
	}
	
	public Map<ScanSize, List<DynamicEngine>> getAllEngines() {
		return allEngines;
	}

	public Map<ScanSize, List<DynamicEngine>> getActiveEngines() {
		return activeEngines;
	}

	public Map<ScanSize, List<DynamicEngine>> getIdleEngines() {
		return idleEngines;
	}

	public Map<ScanSize, List<DynamicEngine>> getExpiringEngines() {
		return expiringEngines;
	}

	public Map<ScanSize, List<DynamicEngine>> getUnprovisionedEngines() {
		return unprovisionedEngines;
	}

	private void initEngineMaps(ScanSize size, Map<ScanSize, List<DynamicEngine>> map) {
		if (map.containsKey(size)) return;
		map.put(size, Lists.newArrayList());
	}
	
	private void initEngineSizes(ScanSize size) {
		if (engineSizes.get(size) == null) 
			engineSizes.put(size, new AtomicLong(0));
		engineSizes.get(size).incrementAndGet();
	}
	
	private void addEngine(DynamicEngine engine) {
		final ScanSize size = engine.getSize();
		final DynamicEngine.State state = engine.getState();
		
		initEngineSizes(size);
		engineMaps.forEach((k, map)->initEngineMaps(size, map));
		allEngines.get(size).add(engine);
		engineMaps.get(state).get(size).add(engine);
	}
	
	// default scope for unit testing
	void changeState(DynamicEngine engine, DynamicEngine.State toState) {
		
		if (toState.equals(DynamicEngine.State.ALL)) 
			throw new IllegalArgumentException("Cannot set Engine state to ALL");
		
		DynamicEngine.State curState = engine.getState();
		ScanSize size = engine.getSize();
		
		if (curState.equals(toState)) return;
		
		engineMaps.get(curState).get(size).remove(engine);
		engineMaps.get(toState).get(size).add(engine);

		engine.setState(toState);
	}

	public ScanSize calcEngineSize(long loc) {
		log.trace("calcEngineSize() : loc={}", loc);
		
		for (ScanSize size : engineSizes.keySet()) {
			if (size.isMatch(loc)) return size;
		}
		return null;
	}
	
	public DynamicEngine allocateEngine(ScanSize size, State fromState) {
		log.trace("allocateEngine() : size={}; state={}", size.getName(), fromState);
		
		
		final Map<ScanSize, List<DynamicEngine>> engineMap = engineMaps.get(fromState);
		if (engineMap == null) return null;
		final List<DynamicEngine> engineList = engineMap.get(size);
		
		synchronized(this) {
			if (engineList == null || engineList.size() == 0) return null;
			
			final DynamicEngine engine = engineList.get(0);
			changeState(engine, DynamicEngine.State.ACTIVE);
			log.debug("Engine allocated: pool={}", this);
			return engine;
		}
		
	}
	
	public void deallocateEngine(DynamicEngine engine) {
		log.trace("unallocateEngine() : {}", engine);
		changeState(engine, State.IDLE);
		log.debug("Engine unallocated: pool={}", this);
	}

	public void logEngines()	{
		allEngines.forEach((size,engines)->logEngines(engines));
	}
	
	public void logEngines(DynamicEngine.State state) {
		engineMaps.get(state).forEach((size,engines)->logEngines(engines));
	}
	
	private void logEngines(List<DynamicEngine> engines) {
		engines.forEach(engine->log.debug("{}", engine));
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		allEngines.forEach((size,engines)->
				engines.forEach(engine->sb.append(String.format("%s; ", engine))));
		final StringBuilder sbSizes = new StringBuilder();
		engineSizes.forEach((size,count)->
				sbSizes.append(String.format("%s:%d, ", size.getName(), count.get())));
		return MoreObjects.toStringHelper(this)
				.add("engineSizes", "[" + sbSizes.toString().replaceAll(", $", "") + "]")
				.add("engines", "[" + sb.toString().replaceAll("; $", "") + "]")
				.toString();
	}

}
