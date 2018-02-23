package de.fhdw.crowdsignals.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.fhdw.deviceanalyzer.parser.core.Event;
import de.fhdw.deviceanalyzer.util.TimeUtil;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WLAN implements Comparable<WLAN> {

	public long pane_start;
	public String[] bssid;
	public long[] timestamp;
	public int timezone;
	public String device_type;

	@Override
	public String toString() {
		return "WLAN [pane_start=" + pane_start + ", timezone=" + timezone + ", device_type=" + device_type + "]";
	}

	@Override
	public int compareTo(WLAN o) {
		return Long.compare(this.pane_start, o.pane_start);
	}

	public List<Event> toEvents() {
		List<Event> events = new ArrayList<>();

		for (int i = 0; i < bssid.length; i++) {
			Event event = new Event();
			event.id = timestamp[i];
			event.key = "wifi|scan|" + bssid[i] + "|ssid";
			event.wallClock = TimeUtil.timestampToWallclock(timestamp[i]);
			events.add(event);
		}

		return events;
	}

}
