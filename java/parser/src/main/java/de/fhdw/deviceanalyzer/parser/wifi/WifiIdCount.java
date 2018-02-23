package de.fhdw.deviceanalyzer.parser.wifi;

public class WifiIdCount implements Comparable<WifiIdCount> {
	
	public String id;
	public int count;

	public WifiIdCount(String id) {
		this.id = id;
		this.count = 1;
	}

	public void count() {
		count++;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		WifiIdCount other = (WifiIdCount) obj;
		if (id == null) {
			if (other.id != null) return false;
		} else if (!id.equals(other.id)) return false;
		return true;
	}

	@Override
	public int compareTo(WifiIdCount o) {
		return o.count - this.count;
	}

	@Override
	public String toString() {
		return "WifiIdCount [id=" + id + ", count=" + count + "]";
	}
}