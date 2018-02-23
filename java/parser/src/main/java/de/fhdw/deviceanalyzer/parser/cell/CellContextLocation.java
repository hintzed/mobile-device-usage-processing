package de.fhdw.deviceanalyzer.parser.cell;

import java.util.Set;

import de.fhdw.deviceanalyzer.parser.core.AbstractContextLocation;

public class CellContextLocation extends AbstractContextLocation {

	String id;
	Set<Cell> cells;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return super.toString() + " cells=" + cells;
	}

	public Set<Cell> getCells() {
		return cells;
	}

}
