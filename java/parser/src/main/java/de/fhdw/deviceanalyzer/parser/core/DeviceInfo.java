package de.fhdw.deviceanalyzer.parser.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import de.fhdw.deviceanalyzer.parser.core.AbstractContextLocation.Context;

public class DeviceInfo {
	public Integer stayon;
	public Integer screenoff;
	public String device;
	public String locale;
	public String manufacturer;
	public String lockVisible;
	public String lockTactile;
	public String nonmarketapps;
	public String osbuildtype;
	public String osstring;
	public String apiversion;
	public String lock;
	public String deviceid;
	public String devicemodel;
	public Date starttime;
	public Date endtime;
	public Boolean suBinary;
	public Boolean superuserApk;
	public Boolean testKeys;
	public String resolution;
	public String dpi;
	public String charger;
	public String dock;
	public Integer locked;
	public Integer unlocked;
	public Integer validDays;
	public Integer foulDays;
	public int usedScreenOnEvents;
	public int skippedScreenOnEvents;
	public boolean usesDock;
	public boolean usesStayon;
	public boolean isPhone;

	public Context gsmContext;
	public Context wifiContext;
	public Context combinedContext;

	public long homeContextsGSM;
	public long officeContextsGSM;
	public long otherContextsGSM;

	public long homeContextsWifi;
	public long officeContextsWifi;
	public long otherContextsWifi;

	public AtomicInteger rooted_true_count = new AtomicInteger(0);
	public AtomicInteger rooted_false_count = new AtomicInteger(0);
	public AtomicInteger nonmarketapps_true_count = new AtomicInteger(0);
	public AtomicInteger nonmarketapps_false_count = new AtomicInteger(0);
	public AtomicInteger lock_true_count = new AtomicInteger(0);
	public AtomicInteger lock_false_count = new AtomicInteger(0);
	public AtomicInteger lockVisible_true_count = new AtomicInteger(0);
	public AtomicInteger lockVisible_false_count = new AtomicInteger(0);
	public AtomicInteger lockTactile_true_count = new AtomicInteger(0);
	public AtomicInteger lockTactile_false_count = new AtomicInteger(0);

	public static List<String> getSubsetFileHeading() {
		List<String> columns = new ArrayList<>();

		columns.add("deviceId");
		columns.add("manufacturer");
		columns.add("devicemodel");
		columns.add("screensize");

		columns.add("country");
		columns.add("starttime");
		columns.add("endtime");
		columns.add("duration");

		columns.add("validDays");
		columns.add("foulDays");
		columns.add("locked");
		columns.add("unlocked");

		columns.add("screenoff");

		columns.add("patternLock");
		columns.add("patternLockVisible");
		columns.add("patternLockTactile");
		columns.add("rooted");

		columns.add("nonmarketapps");
		columns.add("osbuildtype");
		columns.add("osstring");
		columns.add("apiversion");

		columns.add("gsmContext");
		columns.add("wifiContext");
		columns.add("combinedContext");

		return columns;
	}

	public List<String> subsetToLine() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");

		List<String> columns = new ArrayList<>();

		columns.add(deviceid);
		columns.add(manufacturer);
		columns.add(devicemodel);
		columns.add(getScreenSize() != null ? String.format("%.2g", getScreenSize()) : null);

		columns.add(getCountry());
		columns.add(starttime != null ? df.format(starttime) : "");
		columns.add(endtime != null ? df.format(endtime) : "");
		columns.add(starttime != null && endtime != null ? "" + ChronoUnit.DAYS.between(toLocalDate(starttime), toLocalDate(endtime)) : "");

		columns.add(validDays != null ? "" + validDays : null);
		columns.add(foulDays != null ? "" + foulDays : null);
		columns.add(locked != null ? "" + locked : null);
		columns.add(unlocked != null ? "" + unlocked : null);

		columns.add("" + screenoff);

		columns.add(lock);
		columns.add(lockVisible);
		columns.add(lockTactile);
		columns.add(getRooted() == null ? null : getRooted() ? "1" : "0");

		columns.add(nonmarketapps);
		columns.add(osbuildtype);
		columns.add(osstring);
		columns.add(apiversion);

		columns.add(Optional.fromNullable(gsmContext).or(Context.ELSEWHERE).toString());
		columns.add(Optional.fromNullable(wifiContext).or(Context.ELSEWHERE).toString());
		columns.add(Stream
				.builder()
				.add(gsmContext)
				.add(wifiContext)
				.build()
				.filter(f -> f != null)
				.sorted()
				.findFirst()
				.orElse(Context.ELSEWHERE)
				.toString());

		return columns;
	}

	public static final String getFileHeading() {
		List<String> columns = new ArrayList<>();

		columns.addAll(getSubsetFileHeading());

		columns.add("usedScreenOnEvents");
		columns.add("skippedScreenOnEvents");

		columns.add("usesDock");
		columns.add("usesStayon");
		columns.add("isPhone");

		columns.add("rooted_true_count");
		columns.add("rooted_false_count");
		columns.add("nonmarketapps_true_count");
		columns.add("nonmarketapps_false_count");

		columns.add("lock_true_count");
		columns.add("lock_false_count");
		columns.add("lockVisible_true_count");
		columns.add("lockVisible_false_count");
		columns.add("lockTactile_true_count");
		columns.add("lockTactile_false_count");

		columns.add("homeContextsGSM");
		columns.add("officeContextsGSM");
		columns.add("otherContextsGSM");

		columns.add("homeContextsWifi");
		columns.add("officeContextsWifi");
		columns.add("otherContextsWifi");

		// columns = columns.stream().map(s -> "\"" + s +
		// "\"").collect(Collectors.toList());

		return Joiner.on(", ").join(columns);
	}

	public String toLine() {

		List<String> columns = new ArrayList<>();

		columns.addAll(subsetToLine());

		columns.add("" + usedScreenOnEvents);
		columns.add("" + skippedScreenOnEvents);

		columns.add(usesDock ? "1" : "0");
		columns.add(usesStayon ? "1" : "0");
		columns.add(isPhone ? "1" : "0");

		columns.add(rooted_true_count.toString());
		columns.add(rooted_false_count.toString());
		columns.add(nonmarketapps_true_count.toString());
		columns.add(nonmarketapps_false_count.toString());

		columns.add(lock_true_count.toString());
		columns.add(lock_false_count.toString());
		columns.add(lockVisible_true_count.toString());
		columns.add(lockVisible_false_count.toString());
		columns.add(lockTactile_true_count.toString());
		columns.add(lockTactile_false_count.toString());

		columns.add("" + homeContextsGSM);
		columns.add("" + officeContextsGSM);
		columns.add("" + otherContextsGSM);

		columns.add("" + homeContextsWifi);
		columns.add("" + officeContextsWifi);
		columns.add("" + otherContextsWifi);

		columns = columns.stream()
				.map(Strings::nullToEmpty)
				.map(s -> s.replace('"', ' '))
				.map(s -> s.trim())
				.map(s -> s.contains(",") ? "\"" + s + "\"" : s)
				.collect(Collectors.toList());

		return Joiner.on(",").join(columns);
	}

	public Double getScreenSize() {
		if (dpi == null || resolution == null) return null;

		try {
			String[] dpis = dpi.split("x");
			String[] resolutions = resolution.split("x");

			double dpiX = Double.parseDouble(dpis[0]);
			double dpiY = Double.parseDouble(dpis[1]);

			double resolutionX = Double.parseDouble(resolutions[0]);
			double resolutionY = Double.parseDouble(resolutions[1]);

			double inchX = resolutionX / dpiX;
			double inchY = resolutionY / dpiY;

			return Math.sqrt(Math.pow(inchX, 2) + Math.pow(inchY, 2));

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Boolean getRooted() {
		if (suBinary == null && superuserApk == null && testKeys == null) return null;
		if (suBinary == Boolean.TRUE) return true;
		if (superuserApk == Boolean.TRUE) return true;
		if (testKeys == Boolean.TRUE) return true;

		return false;
	}

	public String getCountry() {
		if (locale == null) return null;

		String[] parts = locale.split("_");
		return new Locale(parts[0], parts.length > 1 ? parts[1] : "", parts.length > 2 ? parts[2] : "").getDisplayCountry();
	}

	private static LocalDate toLocalDate(Date date) {
		if (date == null) return null;
		else
			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public synchronized void writeToFile(File file) {
		try {
			if (file.exists()) {
				Files.write(file.toPath(), Lists.newArrayList(toLine()), Charset.forName("UTF-8"), StandardOpenOption.CREATE,
						StandardOpenOption.APPEND,
						StandardOpenOption.WRITE);
			} else {
				Files.write(file.toPath(), Lists.newArrayList(getFileHeading(), toLine()), Charset.forName("UTF-8"), StandardOpenOption.CREATE,
						StandardOpenOption.APPEND,
						StandardOpenOption.WRITE);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}