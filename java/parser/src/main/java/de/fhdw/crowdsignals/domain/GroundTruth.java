package de.fhdw.crowdsignals.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.fhdw.deviceanalyzer.parser.core.Event;
import de.fhdw.deviceanalyzer.util.TimeUtil;

@JsonIgnoreProperties({ "surveyId", "viewId", "layoutGroupId" })
public class GroundTruth implements Comparable<GroundTruth> {

	public String value;

	public long timestamp;

	public String wasInteracted;

	@Override
	public String toString() {
		return "GroundTruth [value=" + value + ", timestamp=" + TimeUtil.timestampToWallclock(timestamp) + ", wasInteracted=" + wasInteracted + "]";
	}

	@Override
	public int compareTo(GroundTruth o) {
		return Long.compare(this.timestamp, o.timestamp);
	}

	public List<Event> toEvents() {
		List<Event> events = new ArrayList<>();

		events.add(new GTEvent(timestamp, timestamp, TimeUtil.timestampToWallclock(timestamp), "GroundTruth", value, wasInteracted));

		return events;
	}

	public class GTEvent extends Event {
		public String interacted;

		public GTEvent(Long id, Long timestamp, Date wallClock, String key, String value, String interacted) {
			this.id = id;
			this.timestamp = timestamp;
			this.wallClock = wallClock;
			this.key = key;
			this.value = value;
			this.interacted = interacted;
		}
	}

}
