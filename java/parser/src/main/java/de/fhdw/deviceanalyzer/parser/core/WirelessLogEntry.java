package de.fhdw.deviceanalyzer.parser.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class WirelessLogEntry {
	public Date date;

	public String type;
	public int deviceCount;

	public boolean isHomeGSM;
	public boolean isOfficeGSM;
	public boolean isHomeWifi;
	public boolean isOfficeWifi;

	public static final String getFileHeading() {
		List<String> columns = new ArrayList<>();

		columns.addAll(DeviceInfo.getSubsetFileHeading());

		columns.add("date");
		columns.add("type");
		columns.add("deviceCount");

		return Joiner.on(",").join(columns);
	}

	public String toLine(DeviceInfo currentDeviceInfo) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");

		List<String> columns = new ArrayList<>();

		columns.addAll(currentDeviceInfo.subsetToLine());

		columns.add(date != null ? df.format(date) : "");
		columns.add(type);
		columns.add("" + deviceCount);

		columns = columns.stream()
				.map(Strings::nullToEmpty)
				.map(s -> s.trim())
				.map(s -> s.contains(",") ? "\"" + s + "\"" : s)
				.collect(Collectors.toList());

		return Joiner.on(",").join(columns);
	}

}