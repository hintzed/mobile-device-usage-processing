package de.fhdw.deviceanalyzer.parser;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Strings;

import de.fhdw.deviceanalyzer.parser.core.Event;

public class SessionFoulDateParser implements IParser {

	private static final List<String> EVENT_KEYS = Arrays.asList(
			"pause",
			"startup",
			"shutdown",
			"phone|calling",
			"phone|ringing",
			"phone|keyguardremoved",
			"dock");

	private Set<LocalDate> foulDates;
	private Set<LocalDate> validDates;
	private boolean requiredVersion;

	@Override
	public void startParsing(String deviceId) {
		foulDates = new HashSet<>();
		validDates = new HashSet<>();
		requiredVersion = false;

		// Device device = CachedDatabaseRepository.readDeviceById(deviceId);
		//
		// if (device != null) {
		// // Skip first and last day for sake of completeness
		// foulDates.add(toLocalDate(device.starttime));
		// foulDates.add(toLocalDate(device.endtime));
		// }
	}

	@Override
	public void parse(Event event) {
		if (event.wallClock != null) {
			if (EVENT_KEYS.stream().anyMatch(k -> k.equals(event.key))) {
				parseEventForFoulDates(event);
			}

			validDates.add(toLocalDate(event.wallClock));
		}
	}

	@Override
	public void endParsing(String deviceId) {
		validDates.removeAll(foulDates);
	}

	public Set<LocalDate> getValidDates() {
		return validDates;
	}

	public Set<LocalDate> getFoulDates() {
		return foulDates;
	}

	private void parseEventForFoulDates(Event event) {
		// We require at least version 1.1.5 for keyguardremoved events
		if ("phone|keyguardremoved".equals(event.key)) {
			requiredVersion = Boolean.TRUE;
		}

		// Starting with version 1.1.5 this key holds the current wall clock
		// time as value.
		if ("startup".equals(event.key) && !Strings.isNullOrEmpty(event.value)) {
			requiredVersion = Boolean.TRUE;
		}

		// Since version 1.1.5 Device Analyzer also attempts to parse the
		// number. If successful the following fields are appended to
		// the number, like such: ... [valid,valid-foreign or invalid]
		if (("phone|ringing".equals(event.key) || "phone|calling".equals(event.key)) && event.value.contains("valid")) {
			requiredVersion = Boolean.TRUE;
		}

		// Present since version 1.2.0.
		if ("dock".equals(event.key)) {
			requiredVersion = Boolean.TRUE;
		}

		// We know for sure we skipped events, so discard the whole day later
		if ("pause".equals(event.key) && event.wallClock != null) {
			foulDates.add(toLocalDate(event.wallClock));
		}

		// We don't have (or don't know if we do) the required version (>=
		// 1.1.5), so discard the whole day later
		if (!requiredVersion && event.wallClock != null) {
			foulDates.add(toLocalDate(event.wallClock));
		}

	}

	private static LocalDate toLocalDate(Date date) {
		if (date == null) return null;
		else
			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

}
