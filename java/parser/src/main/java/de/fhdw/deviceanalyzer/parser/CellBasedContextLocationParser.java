package de.fhdw.deviceanalyzer.parser;

import java.util.Date;
import java.util.List;

import de.fhdw.deviceanalyzer.parser.cell.AbstractCellBasedParser;
import de.fhdw.deviceanalyzer.parser.cell.Cell;
import de.fhdw.deviceanalyzer.parser.cell.CellContextLocation;
import de.fhdw.deviceanalyzer.parser.cell.CellContextLocationGenerator;

public class CellBasedContextLocationParser extends AbstractCellBasedParser {

	private CellContextLocationGenerator contextLocationGenerator;

	private List<CellContextLocation> locations;


	@Override
	public void startParsing(String deviceId) {
		super.startParsing(deviceId);

		contextLocationGenerator = new CellContextLocationGenerator();
	}

	@Override
	protected void onCellFound(Cell cell, Date date) {
		contextLocationGenerator.addCell(cell, date);
	}

	@Override
	protected void onCellUnknown() {
		contextLocationGenerator.addCell(CellContextLocationGenerator.MISSING_CELL, null);
	}

	@Override
	public void endParsing(String deviceId) {
		locations = contextLocationGenerator.getLocations();
	}

	public List<CellContextLocation> getLocations() {
		return locations;
	}


}
