package de.fhdw.deviceanalyzer.parser.core;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class ContextUtil {

	public static boolean isWorkingHour(Date date) {
		Calendar instance = Calendar.getInstance();
		instance.setTime(date);

		int dayOfWeek = instance.get(Calendar.DAY_OF_WEEK);
		int hour = instance.get(Calendar.HOUR_OF_DAY);

		if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) return false;
		else return hour >= 10 && hour <= 16;
	}

	public static boolean isWeekday(Date date) {
		Calendar instance = Calendar.getInstance();
		instance.setTime(date);

		int dayOfWeek = instance.get(Calendar.DAY_OF_WEEK);

		return (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY);
	}

	public static boolean isNightHour(Date date) {
		Calendar instance = Calendar.getInstance();
		instance.setTime(date);

		int hour = instance.get(Calendar.HOUR_OF_DAY);

		return hour >= 0 && hour <= 6;
	}
	
	public static void tagMeaningfullLocations(Collection<? extends AbstractContextLocation> locations) {
		Double mean = locations.stream().collect(Collectors.averagingLong(l -> l.getTotal()));

		locations.stream().filter(l -> l.getTotal() > mean).forEach(AbstractContextLocation::setMeaningful);
	}
}
