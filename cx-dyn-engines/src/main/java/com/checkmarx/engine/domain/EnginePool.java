package com.checkmarx.engine.domain;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.checkmarx.engine.domain.DynamicEngine.State;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EnginePool {

	private static final Logger log = LoggerFactory.getLogger(EnginePool.class);
	
	/**
	 * map of all engines by name; key = engine name
	 */
	private final Map<String, DynamicEngine> allNamedEngines = Maps.newConcurrentMap();

	/**
	 * map of all engines by size; key = engine size (string)
	 */
	private final Map<String, Set<DynamicEngine>> allSizedEngines = Maps.newConcurrentMap();
	private final Map<String, Set<DynamicEngine>> activeEngines = Maps.newConcurrentMap();
	private final Map<String, Set<DynamicEngine>> idleEngines = Maps.newConcurrentMap();
	private final Map<String, Set<DynamicEngine>> expiringEngines = Maps.newConcurrentMap();
	private final Map<String, Set<DynamicEngine>> unprovisionedEngines = Maps.newConcurrentMap();

	// 1st key=engine state, 2nd key = engine size
	/**
	 * map of engine maps by State, then by size name
	 */
	private final Map<State, Map<String, Set<DynamicEngine>>> engineMaps 
		= Maps.newEnumMap(State.class);

	//immutable after initialization
	/**
	 * map of engine counts by size; key=ScanSize
	 */
	private final Map<EngineSize, AtomicLong> engineSizes = Maps.newLinkedHashMap();
	
	/**
	 * map of scan sizes; key=size name (string)
	 */
	private final Map<String, EngineSize> scanSizes = Maps.newConcurrentMap();
	
	public EnginePool(Set<EnginePoolEntry> entries, Set<DynamicEngine> engines) {
		this(entries);
		engines.forEach(engine->addEngine(engine));
	}
	
	protected EnginePool(Set<EnginePoolEntry> entries) {
		engineMaps.put(DynamicEngine.State.ALL, allSizedEngines);
		engineMaps.put(DynamicEngine.State.SCANNING, activeEngines);
		engineMaps.put(DynamicEngine.State.EXPIRING, expiringEngines);
		engineMaps.put(DynamicEngine.State.IDLE, idleEngines);
		engineMaps.put(DynamicEngine.State.UNPROVISIONED, unprovisionedEngines);
		initSizeMaps(entries);
	}
	
	private void initSizeMaps(Set<EnginePoolEntry> entries) {
		entries.forEach((entry) -> {
			final EngineSize scanSize = entry.getScanSize();
			final String size = scanSize.getName();
			scanSizes.put(scanSize.getName(), scanSize);
			engineSizes.put(scanSize, new AtomicLong(0));
			engineMaps.forEach((k, map)->initEngineMaps(size, map));
		});
	}

	private void initEngineMaps(String size, Map<String, Set<DynamicEngine>> map) {
		if (map.containsKey(size)) return;
		map.put(size, Sets.newConcurrentHashSet());
	}
	
	private void addEngine(DynamicEngine engine) {
		final String size = engine.getSize();
		final EngineSize scanSize = scanSizes.get(size);
		final State state = engine.getState();
		
		//initEngineSizes(size);
		engineSizes.get(scanSize).incrementAndGet();
		allSizedEngines.get(size).add(engine);
		engineMaps.get(state).get(size).add(engine);
		allNamedEngines.put(engine.getName(), engine);
		engine.setEnginePool(this);
	}
	
	public int getEngineCount() {
		return allNamedEngines.size();
	}

	public DynamicEngine getEngineByName(String name) {
		return allNamedEngines.get(name);
	}

	Map<String, DynamicEngine> getAllEnginesByName() {
		return allNamedEngines;
	}

	Map<String, Set<DynamicEngine>> getAllEnginesBySize() {
		return allSizedEngines;
	}

	Map<String, Set<DynamicEngine>> getActiveEngines() {
		return activeEngines;
	}

	public ImmutableMap<String, Set<DynamicEngine>> getIdleEngines() {
		return null;
	}

	Map<String, Set<DynamicEngine>> getExpiringEngines() {
		return expiringEngines;
	}

	Map<String, Set<DynamicEngine>> getUnprovisionedEngines() {
		return unprovisionedEngines;
	}

	/**
	 * Replaces an existing engine with the supplied engine.
	 * 
	 * @param newEngine to add
	 * @return the old engine that was replaced
	 */
	public DynamicEngine replaceEngine(DynamicEngine newEngine) {
		log.info("replaceEngine(): {}", newEngine);
		
		final String name = newEngine.getName();
		final String size = newEngine.getSize();
		
		final DynamicEngine curEngine = allNamedEngines.get(name);
		if (curEngine == null) return null;
		
		final State curState = curEngine.getState();

		allSizedEngines.get(size).remove(curEngine);
		engineMaps.get(curState).get(size).remove(curEngine);
		
		addEngine(newEngine);
		return curEngine;
	}

	public void changeState(DynamicEngine engine, State fromState, State toState) {
		if (toState.equals(State.ALL)) 
			throw new IllegalArgumentException("Cannot set Engine state to ALL");
		
		String size = engine.getSize();
		
		if (fromState.equals(toState)) return;
		
		engineMaps.get(fromState).get(size).remove(engine);
		engineMaps.get(toState).get(size).add(engine);
	}
	
	void changeState(DynamicEngine engine, State toState) {
		engine.setState(toState);
	}
	
	public EngineSize calcEngineSize(long loc) {
		log.trace("calcEngineSize() : loc={}", loc);
		
		for (EngineSize size : engineSizes.keySet()) {
			if (size.isMatch(loc)) return size;
		}
		return null;
	}
	
	public DynamicEngine allocateEngine(EngineSize scanSize, State fromState) {
		log.trace("allocateEngine() : size={}; state={}", scanSize.getName(), fromState);
		
		final String size = scanSize.getName();
		final Map<String, Set<DynamicEngine>> engineMap = engineMaps.get(fromState);
		if (engineMap == null) return null;
		final Set<DynamicEngine> engineList = engineMap.get(size);
		
		synchronized(this) {
			if (engineList == null || engineList.size() == 0) return null;
			
			final DynamicEngine engine = Iterables.getFirst(engineList, null);
			changeState(engine, State.SCANNING);
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
		allSizedEngines.forEach((size,engines)->logEngines(engines));
	}
	
	public void logEngines(DynamicEngine.State state) {
		engineMaps.get(state).forEach((size,engines)->logEngines(engines));
	}
	
	private void logEngines(Set<DynamicEngine> engines) {
		engines.forEach(engine->log.debug("{}", engine));
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		allSizedEngines.forEach((size,engines)->
				engines.forEach(engine->sb.append(String.format("%s; ", engine))));
		final StringBuilder sbSizes = new StringBuilder();
		engineSizes.forEach((size,count)->
				sbSizes.append(String.format("%s:%d, ", size.getName(), count.get())));
		return MoreObjects.toStringHelper(this)
				.add("engineSizes", "[" + sbSizes.toString().replaceAll(", $", "") + "]")
				.add("engines", "[" + sb.toString().replaceAll("; $", "") + "]")
				.toString();
	}
	
	public static class EnginePoolEntry {
		
		private final EngineSize scanSize;
		private final int count;
		
		public EnginePoolEntry(EngineSize scanSize, int count) {
			this.scanSize = scanSize;
			this.count = count;
		}

		public EngineSize getScanSize() {
			return scanSize;
		}

		public int getCount() {
			return count;
		}
		
		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("scanSize", scanSize)
					.add("count", count)
					.toString();
		}
	}

}
