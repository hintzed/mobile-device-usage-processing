package de.fhdw.deviceanalyzer.parser;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.fhdw.deviceanalyzer.parser.core.AbstractContextLocation.Context;
import de.fhdw.deviceanalyzer.parser.core.Event;
import de.fhdw.deviceanalyzer.parser.wifi.WifiCluster;

public class WifiScanContextParser implements IParser {

	private static final int TIMEOUT_MIN = 10;

	private WifiScanContextLocationParser locationParser;
	private Map<String, WifiCluster> locationIndex;

	public WifiScanContextParser(WifiScanContextLocationParser locationParser) {
		this.locationParser = locationParser;
	}

	private WifiCluster currentLocation;
	private Date currentTS;

	private long officeContexts;

	private long homeContexts;

	private long otherContexts;

	@Override
	public void startParsing(String deviceId) {
		List<WifiCluster> locations = locationParser.getLocations();

		homeContexts = locations.stream().filter(l -> l.isMeaningful() && l.getContext() == Context.HOME).count();
		officeContexts = locations.stream().filter(l -> l.isMeaningful() && l.getContext() == Context.OFFICE).count();
		otherContexts = locations.stream().filter(l -> l.isMeaningful() && l.getContext() == Context.OTHER_MEANINGFUL).count();

		locationIndex = createLocationIndex(locations);
	}

	@Override
	public void parse(Event event) {
		if (event.key.startsWith("wifi|scan") && event.key.contains("ssid")) {
			String mac = event.key.split("\\|")[2];
			currentLocation = locationIndex.get(mac);
			currentTS = event.wallClock;
		}

	}

	public Optional<WifiCluster> getCurrentLocation(Date now) {
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

	private Map<String, WifiCluster> createLocationIndex(List<WifiCluster> locations) {
		Map<String, WifiCluster> index = new HashMap<>();

		locations.stream().forEach(l -> l.clusterIds.forEach(c -> index.put(c, l)));

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
