package de.fhdw.crowdsignals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import de.fhdw.crowdsignals.domain.GroundTruth.GTEvent;
import de.fhdw.deviceanalyzer.parser.DeviceInfoParser;
import de.fhdw.deviceanalyzer.parser.IParser;
import de.fhdw.deviceanalyzer.parser.core.AbstractContextLocation.Context;
import de.fhdw.deviceanalyzer.parser.core.Event;

public class GroundTruthParser implements IParser {

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private DeviceInfoParser deviceInfoParser;

	private HashSet<String> transport = Sets.newHashSet("Bicycle", "Bus", "Car", "Ferry", "Light Rail", "Train", "Taxi/Uber", "Scooter", "Walked");

	public List<String> data = new ArrayList<>();

	private String deviceId;

	public GroundTruthParser(DeviceInfoParser deviceInfoParser) {
		this.deviceInfoParser = deviceInfoParser;
		sdf.setTimeZone(TimeZone.getTimeZone("UTC-7"));
	}

	@Override
	public void startParsing(String deviceId) {
		this.deviceId = deviceId;
	}

	public ContextDetectionResult contextDetectionResult = new ContextDetectionResult();

	@Override
	public void parse(Event event) {
		if (event.key.equals("GroundTruth") && !transport.contains(event.value)) {
			deviceInfoParser.updateContext(event.wallClock);

			List<String> line = new ArrayList<>();

			line.add(sdf.format(event.wallClock));
			line.add(deviceId);
			line.add(event.value);
			line.add(toString(contextDetectionResult.groundTruthContext(event.value)));
			line.add(toString(contextDetectionResult.combinedContext(deviceInfoParser.getDeviceInfo())));
			line.add(toString(deviceInfoParser.getDeviceInfo().wifiContext));
			line.add(toString(deviceInfoParser.getDeviceInfo().gsmContext));
			line.add("" + ((GTEvent) event).interacted);

			data.add(line.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
		}
	}

	private String toString(Context c) {
		if (c == Context.HOME) return "Home";
		if (c == Context.OFFICE) return "Office";
		if (c == Context.NO_DATA) return "NO_DATA";
		return "Other/Elsewhere";
	}

	@Override
	public void endParsing(String deviceid) {
	}

}
