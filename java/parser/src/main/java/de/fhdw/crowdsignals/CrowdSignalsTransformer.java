package de.fhdw.crowdsignals;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.fhdw.deviceanalyzer.parser.core.Event;

public class CrowdSignalsTransformer {

	private static final File DATASET = new File("~/CrowdSignals");

	static HashSet<Object> callTypes = new HashSet<>();

	public static void main(String[] args) throws Exception {
		for (File userFolder : new File(DATASET, "JSON-CALLS").listFiles()) {
			List<Event> callEvents = processUserCalls(userFolder);

			System.out.println(callEvents.size());
		}

		System.out.println(callTypes);
	}

	private static List<Event> processUserCalls(File userFolder) throws Exception {
		String user = userFolder.getName().substring(0, userFolder.getName().indexOf('-'));
		System.out.println(user);

		List<Event> events = new ArrayList<Event>();

		for (File jsonFile : userFolder.listFiles()) {
			List<String> lines = Files.readAllLines(jsonFile.toPath());
			Collections.sort(lines);

			for (String jsonString : lines) {
				JsonElement jelement = new JsonParser().parse(jsonString);
				JsonObject jobject = jelement.getAsJsonObject();

				JsonArray timestamps = jobject.getAsJsonArray("timestamp");
				JsonArray types = jobject.getAsJsonArray("type");
				JsonArray durations = jobject.getAsJsonArray("duration");

				Preconditions.checkState(timestamps.size() == types.size());
				Preconditions.checkState(durations.size() == types.size());

				for (int i = 0; i < timestamps.size(); i++) {
					String type = types.get(i).getAsString();
					long timestamp = timestamps.get(i).getAsLong();
					long tsMillis = TimeUnit.NANOSECONDS.toMillis(timestamp);
					long duration = TimeUnit.SECONDS.toMillis(timestamps.get(i).getAsLong());

					callTypes.add(type);

					switch (type) {
					case "CALL_INCOMING_TYPE (1)":
						events.add(new Event(null, timestamp, new Date(tsMillis), "phone|ringing", null));
						events.add(new Event(null, timestamp, new Date(tsMillis + 1), "phone|offhook", null));
						events.add(new Event(null, timestamp, new Date(tsMillis + 1 + duration), "phone|idle", null));
						break;
					case "CALL_OUTGOING_TYPE (2)":
						events.add(new Event(null, timestamp, new Date(tsMillis), "phone|calling", null));
						events.add(new Event(null, timestamp, new Date(tsMillis + 1), "phone|offhook", null));
						events.add(new Event(null, timestamp, new Date(tsMillis + 1 + duration), "phone|idle", null));
						break;
					case "CALL_MISSED_TYPE (3)":
						events.add(new Event(null, timestamp, new Date(tsMillis), "phone|ringing", null));
						// We don't know the duration of the missed call, just
						// assuming 30 seconds should be fine, since we are
						// mostly
						// concerned with the display state change
						events.add(new Event(null, timestamp, new Date(tsMillis + 1 + TimeUnit.SECONDS.toMillis(30)), "phone|idle", null));
						break;
					case "CALL_REJECTED_TYPE (5)":
						// Ignore, since I assume rejection would not trigger
						// display state changes
						break;
					// Ignore
					case "UNKNOWN (6501)":
					case "UNKNOWN (10)":
					case "UNKNOWN (6504)":
					case "UNKNOWN (6503)":
					case "UNKNOWN (6502)":
						break;
					default:
						throw new IllegalArgumentException(type);
					}

				}
			}
		}

		return events;

	}

}
