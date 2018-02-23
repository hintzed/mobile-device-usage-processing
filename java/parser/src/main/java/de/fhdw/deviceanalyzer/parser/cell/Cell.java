package de.fhdw.deviceanalyzer.parser.cell;

import java.io.Serializable;

public class Cell implements Serializable {

	private static final long serialVersionUID = 1L;

	public final String lac;
	public final String cellid;

	private final int hashCode;

	public Cell(String lac, String cellid) {
		this.lac = lac;
		this.cellid = cellid;

		this.hashCode = calcHashCode();
	}

	private int calcHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cellid == null) ? 0 : cellid.hashCode());
		result = prime * result + ((lac == null) ? 0 : lac.hashCode());
		return result;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Cell other = (Cell) obj;
		if (cellid == null) {
			if (other.cellid != null) return false;
		} else if (!cellid.equals(other.cellid)) return false;
		if (lac == null) {
			if (other.lac != null) return false;
		} else if (!lac.equals(other.lac)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "Cell [lac=" + lac + ", cellid=" + cellid + "]";
	}

}
