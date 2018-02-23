package de.fhdw.deviceanalyzer.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

	public static Date timestampToWallclock(long ts) {
		Date date = new Date(TimeUnit.NANOSECONDS.toMillis(ts));
		LocalDateTime localDate = date.toInstant().atZone(ZoneId.of("UTC-7")).toLocalDateTime();
		Date date2 = Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());

		return date2;

	}

}