package com.checkmarx.engine.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class ScanSize {
	
	private final String name;
	private final long minLOC;
	private final long maxLOC;
	
	public ScanSize(String name, int minLOC, int maxLOC) {
		this.name = name;
		this.minLOC = minLOC;
		this.maxLOC = maxLOC;
	}

	public String getName() {
		return name;
	}

	public long getMinLOC() {
		return minLOC;
	}

	public long getMaxLOC() {
		return maxLOC;
	}

	public boolean isMatch(long loc) {
		return loc >= minLOC && loc <= maxLOC;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(name, minLOC, maxLOC);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanSize other = (ScanSize) obj;
		if (maxLOC != other.maxLOC)
			return false;
		if (minLOC != other.minLOC)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", name)
				.add("minLOC", minLOC)
				.add("maxLOC", maxLOC)
				.toString();
	}
}
