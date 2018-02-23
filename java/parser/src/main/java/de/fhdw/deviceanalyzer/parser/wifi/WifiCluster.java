package de.fhdw.deviceanalyzer.parser.wifi;

import java.util.HashSet;
import java.util.Set;

import de.fhdw.deviceanalyzer.parser.core.AbstractContextLocation;

public class WifiCluster extends AbstractContextLocation {
	public String rootId;
	public Set<String> clusterIds;

	public WifiCluster(String rootId) {
		this.rootId = rootId;
		this.clusterIds = new HashSet<>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rootId == null) ? 0 : rootId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		WifiCluster other = (WifiCluster) obj;
		if (rootId == null) {
			if (other.rootId != null) return false;
		} else if (!rootId.equals(other.rootId)) return false;
		return true;
	}

	@Override
	public String getId() {
		return rootId;
	}

}