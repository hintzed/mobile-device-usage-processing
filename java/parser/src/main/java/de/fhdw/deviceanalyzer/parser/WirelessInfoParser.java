package de.fhdw.deviceanalyzer.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import de.fhdw.deviceanalyzer.ParsingModule;
import de.fhdw.deviceanalyzer.parser.core.DeviceInfo;
import de.fhdw.deviceanalyzer.parser.core.Event;
import de.fhdw.deviceanalyzer.parser.core.WirelessLogEntry;
import de.fhdw.deviceanalyzer.util.FileUtil;

public class WirelessInfoParser implements IParser {

	private DeviceInfoParser deviceInfoParser;
	private DeviceInfo deviceInfo;
	private List<String> lines;
	
	private WirelessLogEntry currentBluetoothLog;
	private Set<String> currentMACs = new HashSet<>();

	private Properties properties;

	public WirelessInfoParser(DeviceInfoParser deviceInfoParser, Properties properties) {
		this.deviceInfoParser = deviceInfoParser;
		this.properties = properties;
	}

	@Override
	public void startParsing(String deviceId) {
		deviceInfo = deviceInfoParser.getDeviceInfo();
		lines = new ArrayList<>();
	}

	@Override
	public void parse(Event event) {
		parseForWifi(event);
		parseForBluetooth(event);
	}

	private void parseForWifi(Event event) {
		if ("wifi|scancomplete".equals(event.key)) {
			deviceInfoParser.updateContext(event.wallClock);

			WirelessLogEntry logEntry = new WirelessLogEntry();
			logEntry.date = event.wallClock;
			logEntry.type = "wifi";
			logEntry.deviceCount = Integer.parseInt(event.value);

			lines.add(logEntry.toLine(deviceInfo));
		}
	}

	private void parseForBluetooth(Event event) {
		if ("bluetooth|discovery".equals(event.key)) {
			if ("started".equals(event.value) && currentBluetoothLog == null) {
				currentBluetoothLog = new WirelessLogEntry();
				currentBluetoothLog.date = event.wallClock;
				currentBluetoothLog.type = "bluetooth";
				currentMACs.clear();
		
				deviceInfoParser.updateContext(event.wallClock);
			} else if ("finished".equals(event.value) && currentBluetoothLog != null) {
				currentBluetoothLog.deviceCount = currentMACs.size();
				
				lines.add(currentBluetoothLog.toLine(deviceInfo));
				currentBluetoothLog = null;
			}
		}
		
		if (event.key.startsWith("bluetooth|found") && currentBluetoothLog != null) {
			currentMACs.add(event.key.split("\\|")[2]);
		}
	}

	@Override
	public void endParsing(String deviceId) {
		lines.add(0, WirelessLogEntry.getFileHeading());

		FileUtil.writeToFile(lines, new File(properties.getProperty(ParsingModule.DIR_WIRELESS_KEY), deviceId + ".csv"));
	}

}
