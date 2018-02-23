package de.fhdw.deviceanalyzer.parser;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.fhdw.deviceanalyzer.parser.cell.AbstractCellBasedParser;
import de.fhdw.deviceanalyzer.parser.cell.Cell;
import de.fhdw.deviceanalyzer.parser.cell.CellContextLocation;
import de.fhdw.deviceanalyzer.parser.core.AbstractContextLocation.Context;

public class CellBasedContextParser extends AbstractCellBasedParser {

	private static final int TIMEOUT_MIN = 10;

	private CellBasedContextLocationParser locationParser;
	private Map<Cell, CellContextLocation> locationIndex;

	public CellBasedContextParser(CellBasedContextLocationParser locationParser) {
		this.locationParser = locationParser;
	}

	private CellContextLocation currentLocation;
	private Date currentTS;

	private long homeContexts;
	private long officeContexts;
	private long otherContexts;

	@Override
	public void startParsing(String deviceId) {
		List<CellContextLocation> locations = locationParser.getLocations();

		homeContexts = locations.stream().filter(l -> l.isMeaningful() && l.getContext() == Context.HOME).count();
		officeContexts = locations.stream().filter(l -> l.isMeaningful() && l.getContext() == Context.OFFICE).count();
		otherContexts = locations.stream().filter(l -> l.isMeaningful() && l.getContext() == Context.OTHER_MEANINGFUL).count();

		locationIndex = createLocationIndex(locations);
	}

	@Override
	protected void onCellFound(Cell cell, Date date) {
		currentLocation = locationIndex.get(cell);
		currentTS = date;

		// System.out.println(date + " " + currentLocation.getContext());
	}

	@Override
	protected void onCellUnknown() {
		currentLocation = null;
		currentTS = null;
	}

	public Optional<CellContextLocation> getCurrentLocation(Date now) {
		if (currentLocation == null
				|| currentTS == null
				|| Duration.between(Instant.ofEpochMilli(currentTS.getTime()), Instant.ofEpochMilli(now.getTime())).toMinutes() > TIMEOUT_MIN) {
			return Optional.empty();
		} else {
			return Optional.of(currentLocation);
		}
	}

	@Override
	public void endParsing(String deviceId) {
	}

	private Map<Cell, CellContextLocation> createLocationIndex(List<CellContextLocation> locations) {
		Map<Cell, CellContextLocation> index = new HashMap<>();

		locations.stream().forEach(l -> l.getCells().forEach(c -> index.put(c, l)));

		return index;
	}

	public long getHomeContexts() {
		return homeContexts;
	}

	public long getOfficeContexts() {
		return officeContexts;
	}

	public long getOtherContexts() {
		return otherContexts;
	}
}
