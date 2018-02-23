package de.fhdw.deviceanalyzer.parser.cell;

import java.util.Date;

import de.fhdw.deviceanalyzer.parser.IParser;
import de.fhdw.deviceanalyzer.parser.core.Event;

public abstract class AbstractCellBasedParser implements IParser {

	private String tmpCellId;

	@Override
	public void startParsing(String deviceId) {
		tmpCellId = null;
	}

	@Override
	public void parse(Event event) {
		if (RESET_KEYS.contains(event.key)) onCellUnknown();

		if (event.key.contains("phone|celllocation|cid")) {
			tmpCellId = event.value;
		} else if (event.key.contains("phone|celllocation|lac")) {
			if (tmpCellId != null) {
				String lac = event.value;
				Cell cell = new Cell(lac, tmpCellId);

				onCellFound(cell, event.wallClock);
			}

			tmpCellId = null;
		}
	}

	protected abstract void onCellFound(Cell cell, Date date);

	protected abstract void onCellUnknown();

}
