package de.fhdw.deviceanalyzer.parser;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import de.fhdw.deviceanalyzer.parser.cell.CellContextLocation;
import de.fhdw.deviceanalyzer.parser.core.AbstractContextLocation.Context;
import de.fhdw.deviceanalyzer.parser.core.DeviceInfo;
import de.fhdw.deviceanalyzer.parser.core.Event;
import de.fhdw.deviceanalyzer.parser.wifi.WifiCluster;

public class DeviceInfoParser implements IParser {

	private static final List<String> EVENT_KEYS = Arrays.asList(
			// Session events
			"screen|power",
			"power|charger",
			"pause",
			"startup",
			"shutdown",
			"phone|idle",
			"phone|offhook",
			"phone|calling",
			"phone|ringing",
			"phone|keyguardremoved",
			"hf|locked",

			// Device meta data events
			"dock",
			"root|suBinary",
			"root|superuserApk",
			"root|testKeys",
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

	private CellBasedContextParser cellBasedContectParser;

	private WifiScanContextParser wifiScanContextParser;

	public DeviceInfoParser(CellBasedContextParser cellBasedContectParser, WifiScanContextParser wifiScanContextParser) {
		this.cellBasedContectParser = cellBasedContectParser;
		this.wifiScanContextParser = wifiScanContextParser;
	}

	@Override
	public void startParsing(String deviceId) {
		deviceInfo = new DeviceInfo();

		deviceInfo.homeContextsGSM = cellBasedContectParser.getHomeContexts();
		deviceInfo.officeContextsGSM = cellBasedContectParser.getOfficeContexts();
		deviceInfo.otherContextsGSM = cellBasedContectParser.getOtherContexts();

		deviceInfo.homeContextsWifi = wifiScanContextParser.getHomeContexts();
		deviceInfo.officeContextsWifi = wifiScanContextParser.getOfficeContexts();
		deviceInfo.otherContextsWifi = wifiScanContextParser.getOtherContexts();

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
			parseEventForDeviceInfos(event);
		}
	}

	@Override
	public void endParsing(String deviceid) {
		// deviceInfo.writeToFile(new File(ParsingModule.DIR_DEVICES.toFile(),
		// "devices.csv"));
	}

	private void parseEventForDeviceInfos(Event event) {
		try {
			if (deviceInfo.starttime == null) deviceInfo.starttime = event.wallClock;
			deviceInfo.endtime = event.wallClock;

			if ("power|charger".equals(event.key)) deviceInfo.charger = event.value;
			if ("system|display|dpi".equals(event.key)) deviceInfo.dpi = event.value;
			if ("system|display|resolution".equals(event.key)) deviceInfo.resolution = event.value;
			if ("root|suBinary".equals(event.key)) deviceInfo.suBinary = Boolean.parseBoolean(event.value);
			if ("root|superuserApk".equals(event.key)) deviceInfo.superuserApk = Boolean.parseBoolean(event.value);
			if ("root|testKeys".equals(event.key)) deviceInfo.testKeys = Boolean.parseBoolean(event.value);
			if ("system|apiversion".equals(event.key)) deviceInfo.apiversion = event.value;
			if ("system|osbuildtype".equals(event.key)) deviceInfo.osbuildtype = event.value;
			if ("system|osstring".equals(event.key)) deviceInfo.osstring = event.value;
			if ("system|manufacturer".equals(event.key)) deviceInfo.manufacturer = event.value;
			if ("system|locale".equals(event.key)) deviceInfo.locale = event.value;
			if ("system|device".equals(event.key)) deviceInfo.device = event.value;
			if ("system|settings|screenoff".equals(event.key)) deviceInfo.screenoff = Integer.parseInt(event.value);
			if ("system|settings|stayon".equals(event.key)) deviceInfo.stayon = Integer.parseInt(event.value);
			if ("system|settings|stayon".equals(event.key) && !"0".equals(event.value)) deviceInfo.usesStayon = true;
			if ("system|settings|lock".equals(event.key)) deviceInfo.lock = event.value;
			if ("system|settings|lockvisible".equals(event.key)) deviceInfo.lockVisible = event.value;
			if ("system|settings|locktactile".equals(event.key)) deviceInfo.lockTactile = event.value;
			if ("system|settings|nonmarketapps".equals(event.key)) deviceInfo.nonmarketapps = event.value;
			if ("dock".equals(event.key)) deviceInfo.dock = event.value;
			if ("dock".equals(event.key) && !"disconnected".equals(event.value)) deviceInfo.usesDock = true;
			if ("phone|idle".equals(event.key)) deviceInfo.isPhone = true;
			if ("phone|offhook".equals(event.key)) deviceInfo.isPhone = true;
			if ("phone|calling".equals(event.key)) deviceInfo.isPhone = true;
			if ("phone|ringing".equals(event.key)) deviceInfo.isPhone = true;

			if ("system|settings|nonmarketapps".equals(event.key) && "1".equals(event.value)) deviceInfo.nonmarketapps_true_count.incrementAndGet();
			if ("system|settings|nonmarketapps".equals(event.key) && "0".equals(event.value)) deviceInfo.nonmarketapps_false_count.incrementAndGet();

			if (event.key.startsWith("root") && deviceInfo.getRooted() == Boolean.TRUE) deviceInfo.rooted_true_count.incrementAndGet();
			if (event.key.startsWith("root") && deviceInfo.getRooted() == Boolean.FALSE) deviceInfo.rooted_false_count.incrementAndGet();

			if ("system|settings|lock".equals(event.key) && "1".equals(event.value)) deviceInfo.lock_true_count.incrementAndGet();
			if ("system|settings|lock".equals(event.key) && "0".equals(event.value)) deviceInfo.lock_false_count.incrementAndGet();

			if ("system|settings|lockvisible".equals(event.key) && "1".equals(event.value)) deviceInfo.lockVisible_true_count.incrementAndGet();
			if ("system|settings|lockvisible".equals(event.key) && "0".equals(event.value)) deviceInfo.lockVisible_false_count.incrementAndGet();

			if ("system|settings|locktactile".equals(event.key) && "1".equals(event.value)) deviceInfo.lockTactile_true_count.incrementAndGet();
			if ("system|settings|locktactile".equals(event.key) && "0".equals(event.value)) deviceInfo.lockTactile_false_count.incrementAndGet();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateContext(Date referenceDate) {
		Optional<CellContextLocation> cellLocation = cellBasedContectParser.getCurrentLocation(referenceDate);
		deviceInfo.gsmContext = cellLocation.isPresent() ? cellLocation.get().getContext() : Context.NO_DATA;

		Optional<WifiCluster> wifiLocation = wifiScanContextParser.getCurrentLocation(referenceDate);
		deviceInfo.wifiContext = wifiLocation.isPresent() ? wifiLocation.get().getContext() : Context.NO_DATA;
	}

	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}

}
