package de.fhdw.deviceanalyzer.parser.cell;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class CellEvent {

	public final Date ts;
	public final Cell cell;
	
	public CellEvent(Date ts, Cell cell) {
		this.ts = ts;
		this.cell = cell;
	}
	
	public Date getTs() {
		return ts;
	}
	
	public LocalDate getLocalDate() {
		return ts == null? null : ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	public Cell getCell() {
		return cell;
	}
	
	
	@Override
	public String toString() {
		return "CellEvent [ts=" + ts + ", cell=" + cell + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cell == null) ? 0 : cell.hashCode());
		result = prime * result + ((ts == null) ? 0 : ts.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CellEvent other = (CellEvent) obj;
		if (cell == null) {
			if (other.cell != null) return false;
		} else if (!cell.equals(other.cell)) return false;
		if (ts == null) {
			if (other.ts != null) return false;
		} else if (!ts.equals(other.ts)) return false;
		return true;
	}

}
