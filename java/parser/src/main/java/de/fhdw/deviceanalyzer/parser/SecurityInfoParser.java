package de.fhdw.deviceanalyzer.parser;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.fhdw.deviceanalyzer.parser.core.DeviceInfo;
import de.fhdw.deviceanalyzer.parser.core.Event;

public class SecurityInfoParser implements IParser {

	private static final List<String> EVENT_KEYS = Arrays.asList(
			// Device meta data events
			"dock",
			"root|suBinary",
			"root|superuserApk",
			"root|testKeys",
			"power|charger",
			"system|apiversion",
			"system|display|resolution",
			"system|display|dpi",
			"system|settings|lock",
			"system|settings|locktactile",
			"system|settings|lockvisible",
			"system|settings|nonmarketapps",
			"system|manufacturer",
			"system|locale",
			"system|device",
			"system|settings|screenoff",
			"system|settings|stayon");

	private DeviceInfo deviceInfo;

	private List<Event> events;
	private Map<LocalDate, String> days;

	private List<String> lines;

	// @Override
	// public void begin() {
	// lines = new ArrayList<>();
	// }

	@Override
	public void startParsing(String deviceId) {
		events = new ArrayList<>();
		days = new HashMap<>();

		deviceInfo = new DeviceInfo();

		// Device device = CachedDatabaseRepository.readDeviceById(deviceId);
		//
		// if (device != null) {
		deviceInfo.deviceid = deviceId;
		// deviceInfo.devicemodel = device.devicemodel;
		// deviceInfo.starttime = device.starttime;
		// deviceInfo.endtime = device.endtime;
		// }
	}

	@Override
	public void parse(Event event) {
		// TODO: Think about the wallClock issue
		if (EVENT_KEYS.stream().anyMatch(k -> k.equals(event.key)) && event.wallClock != null) {
			events.add(event);
		}
	}

	@Override
	public void endParsing(String deviceid) {
		Collections.sort(events);

		for (Event event : events) {
			parseEventForDeviceInfos(event);
			// parseEventForSession(event);

			days.put(toLocalDate(event.wallClock), deviceInfo.toLine());
		}

		List<String> dayLines = days.keySet().stream().sorted().map(k -> days.get(k)).collect(Collectors.toList());
		lines.addAll(dayLines);
	}

	private void parseEventForDeviceInfos(Event event) {
		try {
			if ("power|charger".equals(event.key)) deviceInfo.charger = event.value;
			if ("system|display|dpi".equals(event.key)) deviceInfo.dpi = event.value;
			if ("system|display|resolution".equals(event.key)) deviceInfo.resolution = event.value;
			if ("root|suBinary".equals(event.key)) deviceInfo.suBinary = Boolean.parseBoolean(event.value);
			if ("root|superuserApk".equals(event.key)) deviceInfo.superuserApk = Boolean.parseBoolean(event.value);
			if ("root|testKeys".equals(event.key)) deviceInfo.testKeys = Boolean.parseBoolean(event.value);
			if ("system|apiversion".equals(event.key)) deviceInfo.apiversion = event.value;
			if ("system|manufacturer".equals(event.key)) deviceInfo.manufacturer = event.value;
			if ("system|locale".equals(event.key)) deviceInfo.locale = event.value;
			if ("system|device".equals(event.key)) deviceInfo.device = event.value;
			if ("system|settings|screenoff".equals(event.key)) deviceInfo.screenoff = Integer.parseInt(event.value);
			if ("system|settings|stayon".equals(event.key)) deviceInfo.stayon = Integer.parseInt(event.value);
			if ("system|settings|lock".equals(event.key)) deviceInfo.lock = event.value;
			if ("system|settings|lockvisible".equals(event.key)) deviceInfo.lockVisible = event.value;
			if ("system|settings|locktactile".equals(event.key)) deviceInfo.lockTactile = event.value;
			if ("system|settings|nonmarketapps".equals(event.key)) deviceInfo.nonmarketapps = event.value;
			if ("dock".equals(event.key)) deviceInfo.dock = event.value;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//
	// @Override
	// public void finish() {
	// lines.add(0, DeviceInfo.getFileHeading());
	//
	// FileUtil.writeToFile(lines, new File(ParsingModule.DIR_DEVICES.toFile(),
	// "deviceConfigDaily.csv"));
	// }

	private static LocalDate toLocalDate(Date date) {
		if (date == null) return null;
		else
			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

}