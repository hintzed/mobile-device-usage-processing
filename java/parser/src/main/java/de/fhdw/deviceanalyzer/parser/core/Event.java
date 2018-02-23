package de.fhdw.deviceanalyzer.parser.core;

import java.util.Date;

import com.google.common.collect.ComparisonChain;

public class Event implements Comparable<Event> {

	public Long id;
	public Long timestamp;
	public Date wallClock;
	public String key;
	public String value;

	public Event() {
	}

	public Event(Long id, Long timestamp, Date wallClock, String key, String value) {
		this.id = id;
		this.timestamp = timestamp;
		this.wallClock = wallClock;
		this.key = key;
		this.value = value;
	}

	@Override
	public String toString() {
		return "Event [id=" + id + ", timestamp=" + timestamp + ", wallClock=" + wallClock + ", key=" + key + ", value=" + value + "]";
	}

	@Override
	public int compareTo(Event other) {
		return ComparisonChain
				.start()
				.compare(wallClock.getTime(), other.wallClock.getTime())
				.compare(id, other.id)
				.result();
	}

}
