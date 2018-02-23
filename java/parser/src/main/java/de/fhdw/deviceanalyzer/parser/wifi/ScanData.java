package de.fhdw.deviceanalyzer.parser.wifi;

import java.util.Date;
import java.util.Set;

public class ScanData {

	public Date wallClock;
	public Set<String> wifiIds;

	public ScanData(Date wallClock, Set<String> wifiIds) {

		this.wallClock = wallClock;
		this.wifiIds = wifiIds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((wallClock == null) ? 0 : wallClock.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ScanData other = (ScanData) obj;
		if (wallClock == null) {
			if (other.wallClock != null) return false;
		} else if (!wallClock.equals(other.wallClock)) return false;
		return true;
	}
}