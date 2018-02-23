package de.fhdw.deviceanalyzer.parser.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class Session {
	public Long start;
	public Long end;
	public Long beginEvent;
	public Long endEvent;
	public Date beginDate;
	public Date endDate;

	public Session(Long start) {
		this.start = start;
	}

	public Duration duration() {
		if (end > start) {
			return Duration.between(Instant.ofEpochMilli(start), Instant.ofEpochMilli(end));
		} else {
			return Duration.between(Instant.ofEpochMilli(beginDate.getTime()), Instant.ofEpochMilli(endDate.getTime()));
		}
	}

	public static final String getFileHeading(boolean authenticated) {
		List<String> columns = new ArrayList<>();

		if (authenticated) {
			columns.add("authenticationDuration");
			columns.add("unlockedSession");
		} else {
			columns.add("lockedSession");
		}

		columns.addAll(DeviceInfo.getSubsetFileHeading());

		columns.add("beginEvent");
		columns.add("endEvent");
		columns.add("beginDate");
		columns.add("endDate");

		// columns = columns.stream().map(s -> "\"" + s +
		// "\"").collect(Collectors.toList());

		return Joiner.on(",").join(columns);
	}

	public String toLine(DeviceInfo currentDeviceInfo, Session authentionSession) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");

		List<String> columns = new ArrayList<>();

		if (authentionSession != null) {
			columns.add("" + authentionSession.duration().toMillis());
			columns.add("" + duration().toMillis());
		} else {
			columns.add("" + duration().toMillis());
		}

		columns.addAll(currentDeviceInfo.subsetToLine());

		columns.add("" + beginEvent);
		columns.add("" + endEvent);
		columns.add(beginDate != null ? df.format(beginDate) : "");
		columns.add(endDate != null ? df.format(endDate) : "");

		columns = columns.stream()
				.map(Strings::nullToEmpty)
				.map(s -> s.trim())
				.map(s -> s.contains(",") ? "\"" + s + "\"" : s)
				.collect(Collectors.toList());

		return Joiner.on(",").join(columns);
	}

}