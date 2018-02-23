package de.fhdw.deviceanalyzer.parser;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;

import de.fhdw.deviceanalyzer.ParsingModule;
import de.fhdw.deviceanalyzer.parser.core.DeviceInfo;
import de.fhdw.deviceanalyzer.parser.core.DeviceState;
import de.fhdw.deviceanalyzer.parser.core.Event;
import de.fhdw.deviceanalyzer.parser.core.Session;
import de.fhdw.deviceanalyzer.util.FileUtil;

public class SessionParser implements IParser {

	private static final List<String> EVENT_KEYS = Arrays.asList(
			// Session events
			"screen|power",
			"power|charger",
			"pause",
			"startup",
			"shutdown",
			"phone|idle",
			"phone|offhook",
			"phone|calling",
			"phone|ringing",
			"phone|keyguardremoved",
			"hf|locked",

			// Device meta data events
			"dock",
			"root|suBinary",
			"root|superuserApk",
			"root|testKeys",
			"system|apiversion",
			"system|display|resolution",
			"system|display|dpi",
			"system|settings|lock",
			"system|settings|locktactile",
			"system|settings|lockvisible",
			"system|settings|nonmarketapps",
			"system|manufacturer",
			"system|locale",
			"system|device",
			"system|settings|screenoff",
			"system|settings|stayon");

	private static final List<String> RESET_KEYS = Arrays.asList(
			"pause",
			"startup",
			"shutdown");

	private DeviceState state;

	private Set<LocalDate> foulDates;
	private Set<LocalDate> validDates;
	private List<String> lockedSessionLine;
	private List<String> unlockedSessionLine;
	private String deviceId;

	private SessionFoulDateParser sessionFoulDateParser;

	private DeviceInfoParser deviceInfoParser;

	private DeviceInfo deviceInfo;

	public SessionParser(DeviceInfoParser deviceInfoParser, SessionFoulDateParser sessionFoulDateParser) {
		this.deviceInfoParser = deviceInfoParser;
		this.sessionFoulDateParser = sessionFoulDateParser;
	}

	@Override
	public void startParsing(String deviceId) {
		this.deviceId = deviceId;
		lockedSessionLine = new ArrayList<>();
		unlockedSessionLine = new ArrayList<>();
		foulDates = sessionFoulDateParser.getFoulDates();
		validDates = sessionFoulDateParser.getValidDates();
		deviceInfo = deviceInfoParser.getDeviceInfo();
		state = DeviceState.LOCKED_DISPLAY_OFF;
	}

	@Override
	public void parse(Event event) {
		// TODO: Think about the wallClock issue
		if (EVENT_KEYS.stream().anyMatch(k -> k.equals(event.key)) && event.wallClock != null) {
			parseEventForSession(event);
		}
	}

	private Session lockedSession;
	private Session unlockedSession;
	private Session authenticationSession;

	private void handleStateChange(DeviceState oldState, DeviceState newState, Event event) {
		if ("screen|power".equals(event.key) && event.value.startsWith("on")) {
			if (oldState != newState) {
				deviceInfo.usedScreenOnEvents += 1;
			} else {
				deviceInfo.skippedScreenOnEvents += 1;
			}
		}

		if (oldState == newState) return;

		// System.out.println(event.id + " " + oldState + " --> " + newState);

		if (oldState == DeviceState.LOCKED_DISPLAY_OFF && newState == DeviceState.LOCKED_DISPLAY_ON) {
			startLockedSession(event);
			startAuthenticationSession(event);
		}

		if (oldState == DeviceState.LOCKED_DISPLAY_ON && newState == DeviceState.LOCKED_DISPLAY_OFF) {
			endLockedSession(event);
			discardAuthenticationSession();
		}

		if (oldState == DeviceState.LOCKED_DISPLAY_ON && newState == DeviceState.UNLOCKED) {
			endAuthenticationSession(event);
			startUnlockedSession(event);

			if (Objects.equal(lockedSession.start, authenticationSession.start)) {
				discardLockedSession();
			} else {
				endLockedSession(event);
			}
		}

		if (oldState == DeviceState.UNLOCKED && newState == DeviceState.LOCKED_DISPLAY_OFF) {
			endUnlockedSession(event);
		}

		if (oldState == DeviceState.UNLOCKED && newState == DeviceState.LOCKED_DISPLAY_ON) {
			endUnlockedSession(event);
			startAuthenticationSession(event);
			startLockedSession(event);
		}

		if (oldState == DeviceState.LOCKED_RINGING && newState == DeviceState.LOCKED_ACTIVE_CALL) {
			startLockedSession(event);
		}

		if (oldState == DeviceState.LOCKED_ACTIVE_CALL && newState == DeviceState.LOCKED_DISPLAY_ON) {
			startAuthenticationSession(event);
		}

	}

	private void startLockedSession(Event event) {
		lockedSession = newSession(event);
	}

	private void endLockedSession(Event event) {
		lockedSession.end = event.timestamp;
		lockedSession.endEvent = event.id;
		lockedSession.endDate = event.wallClock;

		if (event.wallClock != null && !foulDates.contains(toLocalDate(event.wallClock))) {
			lockedSessionLine.add(lockedSession.toLine(deviceInfo, null));
			validDates.add(toLocalDate(event.wallClock));
		}
	}

	private void discardLockedSession() {
		lockedSession = null;
	}

	private void startUnlockedSession(Event event) {
		unlockedSession = newSession(event);
	}

	private void endUnlockedSession(Event event) {
		unlockedSession.end = event.timestamp;
		unlockedSession.endEvent = event.id;
		unlockedSession.endDate = event.wallClock;

		if (event.wallClock != null && !foulDates.contains(toLocalDate(event.wallClock))) {
			unlockedSessionLine.add(unlockedSession.toLine(deviceInfo, authenticationSession));
		}

	}

	private void startAuthenticationSession(Event event) {
		authenticationSession = newSession(event);
	}

	private void endAuthenticationSession(Event event) {
		authenticationSession.end = event.timestamp;
		authenticationSession.endEvent = event.id;
		authenticationSession.endDate = event.wallClock;
	}

	private void discardAuthenticationSession() {
		authenticationSession = null;
	}

	private Session newSession(Event event) {
		Session session = new Session(event.timestamp);
		session.beginEvent = event.id;
		session.beginDate = event.wallClock;

		deviceInfoParser.updateContext(event.wallClock);

		return session;
	}

	@Override
	public void endParsing(String deviceid) {
		deviceInfo.locked = lockedSessionLine.size();
		deviceInfo.unlocked = unlockedSessionLine.size();
		deviceInfo.validDays = validDates.size();
		deviceInfo.foulDays = foulDates.size();

		lockedSessionLine.add(0, Session.getFileHeading(false));
		unlockedSessionLine.add(0, Session.getFileHeading(true));

		FileUtil.writeToFile(lockedSessionLine, new File(ParsingModule.DIR_LOCKED.toFile(), deviceId + ".csv"));
		FileUtil.writeToFile(unlockedSessionLine, new File(ParsingModule.DIR_UNLOCKED.toFile(), deviceId + ".csv"));
		deviceInfo.writeToFile(new File(ParsingModule.DIR_DEVICES.toFile(), "devices.csv"));
	}

	private void parseEventForSession(Event event) {
		// if (event.wallClock == null) return; // FIXME: Destroys current day!!

		// Just resumed from pause or restart, state might be instable
		if (RESET_KEYS.stream().anyMatch(k -> k.equals(event.key))) {
			state = DeviceState.LOCKED_DISPLAY_OFF;
			lockedSession = null;
			unlockedSession = null;
			authenticationSession = null;
		} else {
			handleStateChange(state, state = state.nextState(event), event);
		}
	}

	private static LocalDate toLocalDate(Date date) {
		if (date == null) return null;
		else
			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

}
