package de.fhdw.deviceanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import de.fhdw.deviceanalyzer.parser.CellBasedContextLocationParser;
import de.fhdw.deviceanalyzer.parser.CellBasedContextParser;
import de.fhdw.deviceanalyzer.parser.DeviceInfoParser;
import de.fhdw.deviceanalyzer.parser.IParser;
import de.fhdw.deviceanalyzer.parser.SessionFoulDateParser;
import de.fhdw.deviceanalyzer.parser.SessionParser;
import de.fhdw.deviceanalyzer.parser.WifiScanContextLocationParser;
import de.fhdw.deviceanalyzer.parser.WifiScanContextParser;
import de.fhdw.deviceanalyzer.parser.core.Event;

public class ParsingModule implements IModule {

	// Property keys
	public static final String DIR_DOWNLOAD_KEY = "DIR_DOWNLOAD";
	public static final String DIR_LOCKED_KEY = "DIR_LOCKED";
	public static final String DIR_WIRELESS_KEY = "DIR_WIRELESS";
	public static final String DIR_UNLOCKED_KEY = "DIR_UNLOCKED";
	public static final String DIR_DEVICES_KEY = "DIR_DEVICES";
	public static final String DIR_DONE_KEY = "DIR_DONE";

	private final Path DIR_DOWNLOAD;
	private final Path DIR_LOCKED;
	private final Path DIR_WIRELESS;
	private final Path DIR_UNLOCKED;
	private final Path DIR_DEVICES;
	private final Path DIR_DONE;

	private Properties properties;

	private volatile boolean shutdown = false;

	private List<File> files = new ArrayList<>();;

	private AtomicInteger counter = new AtomicInteger();

	public ParsingModule(Properties properties) {
		this.properties = properties;

		DIR_DOWNLOAD = Paths.get(properties.getProperty(DIR_DOWNLOAD_KEY));
		DIR_LOCKED = Paths.get(properties.getProperty(DIR_LOCKED_KEY));
		DIR_WIRELESS = Paths.get(properties.getProperty(DIR_WIRELESS_KEY));
		DIR_UNLOCKED = Paths.get(properties.getProperty(DIR_UNLOCKED_KEY));
		DIR_DEVICES = Paths.get(properties.getProperty(DIR_DEVICES_KEY));
		DIR_DONE = Paths.get(properties.getProperty(DIR_DONE_KEY));

		DIR_LOCKED.toFile().mkdirs();
		DIR_UNLOCKED.toFile().mkdirs();
		// DIR_WIRELESS.toFile().mkdirs();
		DIR_DEVICES.toFile().mkdirs();

		if (!DIR_DONE.toFile().exists()) {
			DIR_DONE.toFile().mkdir();

			Arrays.asList(DIR_LOCKED.toFile().listFiles()).stream().forEach(file -> file.delete());
			Arrays.asList(DIR_UNLOCKED.toFile().listFiles()).stream().forEach(file -> file.delete());
			// Arrays.asList(DIR_WIRELESS.toFile().listFiles()).stream().forEach(file
			// -> file.delete());
			Arrays.asList(DIR_DEVICES.toFile().listFiles()).stream().forEach(file -> file.delete());
		}

	}

	@Override
	public void execute() {
		files = Arrays.asList(DIR_DOWNLOAD.toFile().listFiles());

		Instant start = Instant.now();
		files.stream().parallel().forEach(this::parseFile);
		// Collections.shuffle(files);
		// files.stream().parallel().limit(1000).forEach(this::parseFile);

		System.out.println(Duration.between(start, Instant.now()));
	}

	private void parseFile(File file) {
		if (shutdown) return;
		File doneFile = new File(DIR_DONE.toFile(), file.getName() + ".done");

		if (doneFile.exists()) {
			System.out.println("Done " + (counter.incrementAndGet()) + " from " + files.size() + " - " + file.getName());
			return;
		} else {
			System.out.println("Parsing " + (counter.incrementAndGet()) + " from " + files.size() + " - " + file.getName());
		}

		CellBasedContextLocationParser cellBasedContectLocationParser = new CellBasedContextLocationParser();
		CellBasedContextParser cellBasedContectParser = new CellBasedContextParser(cellBasedContectLocationParser);

		WifiScanContextLocationParser wifiScanContextLocationParser = new WifiScanContextLocationParser();
		WifiScanContextParser wifiScanContextParser = new WifiScanContextParser(wifiScanContextLocationParser);

		DeviceInfoParser deviceInfoParser = new DeviceInfoParser(cellBasedContectParser, wifiScanContextParser);
		SessionFoulDateParser sessionFoulDateParser = new SessionFoulDateParser();

		// WirelessInfoParser wirelessInfoParser = new
		// WirelessInfoParser(deviceInfoParser);

		List<IParser> mainParser = new ArrayList<>();
		List<IParser> vanguardParser = new ArrayList<>();
		vanguardParser.add(cellBasedContectLocationParser);
		vanguardParser.add(wifiScanContextLocationParser);
		vanguardParser.add(sessionFoulDateParser);

		// Maintain order!
		mainParser.add(cellBasedContectParser);
		mainParser.add(wifiScanContextParser);
		mainParser.add(deviceInfoParser);
		// mainParser.add(wirelessInfoParser);
		mainParser.add(new SessionParser(deviceInfoParser, sessionFoulDateParser, properties));

		parse(file, vanguardParser);
		parse(file, mainParser);

		try {
			doneFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parse(File file, List<IParser> parser) {
		String deviceId = file.getName().replace(".csv", "");

		parser.forEach(p -> p.startParsing(deviceId));

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")))) {
			String line = null;
			Date lastTS = null;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(";");

				Long id = Long.parseLong(parts[0]);
				Long timestamp = Long.parseLong(parts[1]);

				Date tmpWallClock = null;

				try {
					tmpWallClock = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(parts[2]);
				} catch (Exception e) {
					// e.printStackTrace();
				}

				Date wallClock = tmpWallClock;

				String key = parts[3];
				String value = parts.length > 4 ? parts[4] : null;

				Event event = new Event(id, timestamp, wallClock, key, value);

				// Try to avoid errors instead of sorting events (which does not
				// work because because of the memory requirements)
				if (event.wallClock != null && (lastTS == null || lastTS.getTime() <= event.wallClock.getTime())) {
					parser.forEach(p -> p.parse(event));
				}
				lastTS = event.wallClock;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		parser.forEach(p -> p.endParsing(deviceId));
	}

	@Override
	public void shutdown() {
		shutdown = true;
	}

}
