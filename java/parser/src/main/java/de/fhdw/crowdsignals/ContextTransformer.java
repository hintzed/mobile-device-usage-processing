package de.fhdw.crowdsignals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import de.fhdw.crowdsignals.domain.GSM;
import de.fhdw.crowdsignals.domain.GroundTruth;
import de.fhdw.crowdsignals.domain.WLAN;
import de.fhdw.deviceanalyzer.parser.CellBasedContextLocationParser;
import de.fhdw.deviceanalyzer.parser.CellBasedContextParser;
import de.fhdw.deviceanalyzer.parser.DeviceInfoParser;
import de.fhdw.deviceanalyzer.parser.IParser;
import de.fhdw.deviceanalyzer.parser.WifiScanContextLocationParser;
import de.fhdw.deviceanalyzer.parser.WifiScanContextParser;

public class ContextTransformer {

	private String user;

	private File basedir = new File("~/CrowdSignals-hintze/Mobility");

	private static List<String> allUsers = Lists.newArrayList("User1", "User2", "User6", "User8", "User9", "User10",
			"User11", "User12", "User16", "User19", "User20", "User21", "User23", "User24", "User25", "User26",
			"User27", "User28", "User29", "User30", "User31", "User32", "User34", "User35", "User36", "User37",
			"User38", "User39", "User41", "User42", "User43");

	private ObjectMapper mapper = new ObjectMapper();

	public static void main(String[] args) throws Exception {

		allUsers.stream().map(ContextTransformer::new).parallel().forEach(t -> {
			try {
				t.transform();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void transform() throws Exception {
		List<GroundTruth> groundTruth = readGroundTruth();
		List<WLAN> wiFi = readWiFi();
		List<GSM> gsm = readGSM();

		Collections.sort(groundTruth);
		Collections.sort(wiFi);
		Collections.sort(gsm);

		CellBasedContextLocationParser cellBasedContectLocationParser = new CellBasedContextLocationParser();
		CellBasedContextParser cellBasedContectParser = new CellBasedContextParser(cellBasedContectLocationParser);

		WifiScanContextLocationParser wifiScanContextLocationParser = new WifiScanContextLocationParser();
		WifiScanContextParser wifiScanContextParser = new WifiScanContextParser(wifiScanContextLocationParser);

		DeviceInfoParser deviceInfoParser = new DeviceInfoParser(cellBasedContectParser, wifiScanContextParser);

		GroundTruthParser groundTruthParser = new GroundTruthParser(deviceInfoParser);

		List<IParser> mainParser = new ArrayList<>();
		List<IParser> vanguardParser = new ArrayList<>();
		vanguardParser.add(wifiScanContextLocationParser);
		vanguardParser.add(cellBasedContectLocationParser);
		mainParser.add(cellBasedContectParser);
		mainParser.add(wifiScanContextParser);
		mainParser.add(deviceInfoParser);
		mainParser.add(groundTruthParser);

		parse(groundTruth, wiFi, gsm, vanguardParser);
		parse(groundTruth, wiFi, gsm, mainParser);

		printResults(cellBasedContectParser, wifiScanContextParser, groundTruthParser);
	}

	private synchronized void printResults(CellBasedContextParser cellBasedContectParser,
			WifiScanContextParser wifiScanContextParser,
			GroundTruthParser groundTruthParser) throws IOException {
		System.out.println("\n\n" + user);

		System.out.println(groundTruthParser.contextDetectionResult);
		for (String key : groundTruthParser.contextDetectionResult.contextCount.keySet()) {
			System.out.println(key + " " + groundTruthParser.contextDetectionResult.contextCount.get(key));
		}

		Files.write(Paths.get("data2.csv"), groundTruthParser.data, StandardOpenOption.APPEND,
				StandardOpenOption.CREATE);
	}

	private void parse(List<GroundTruth> groundTruth, List<WLAN> wiFi, List<GSM> gsm, List<IParser> parser) {
		parser.forEach(p -> p.startParsing(user));

		Stream.concat(Stream.concat(groundTruth.stream().flatMap(g -> g.toEvents().stream()),
				wiFi.stream().flatMap(g -> g.toEvents().stream())),
				gsm.stream().flatMap(g -> g.toEvents().stream()))
				.sorted()
				.forEachOrdered(event -> parser.forEach(p -> p.parse(event)));

		parser.forEach(p -> p.endParsing(user));
	}

	public ContextTransformer(String user) {
		this.user = user;
	}

	private List<GSM> readGSM() throws Exception {
		File surveyTar = new File(basedir, "Copy of JSON-" + user + "-Mobility.tar.gz");

		List<GSM> gsm = new ArrayList<>();

		try (TarArchiveInputStream tarInput = new TarArchiveInputStream(
				new GzipCompressorInputStream(new FileInputStream(surveyTar)));) {

			TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
			while (currentEntry != null) {
				if (currentEntry.getName().contains("cell-alias")) {
					BufferedReader br = new BufferedReader(new InputStreamReader(tarInput));

					// br.lines().forEach(s -> System.out.println(s));
					br.lines().map(line -> read(line, GSM.class)).forEach(gsm::add);
				}
				currentEntry = tarInput.getNextTarEntry();
			}
		}

		return gsm;
	}

	private List<WLAN> readWiFi() throws Exception {
		File surveyTar = new File(basedir, "Copy of JSON-" + user + "-Mobility.tar.gz");

		List<WLAN> wifi = new ArrayList<>();

		try (TarArchiveInputStream tarInput = new TarArchiveInputStream(
				new GzipCompressorInputStream(new FileInputStream(surveyTar)));) {

			TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
			while (currentEntry != null) {
				if (currentEntry.getName().contains("wlan-alias")) {
					BufferedReader br = new BufferedReader(new InputStreamReader(tarInput));

					// br.lines().forEach(s -> System.out.println(s));
					br.lines().map(line -> read(line, WLAN.class)).forEach(wifi::add);
				}
				currentEntry = tarInput.getNextTarEntry();
			}
		}

		return wifi;
	}

	private List<GroundTruth> readGroundTruth() throws Exception {
		File surveyTar = new File(basedir, "Copy of JSON-" + user + "-Survey.tar.gz");

		List<GroundTruth> groundTruth = new ArrayList<>();

		try (TarArchiveInputStream tarInput = new TarArchiveInputStream(
				new GzipCompressorInputStream(new FileInputStream(surveyTar)));) {

			TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
			while (currentEntry != null) {
				if (currentEntry.getName().contains("Current Place")) {
					BufferedReader br = new BufferedReader(new InputStreamReader(tarInput));
					br.lines().map(line -> read(line, GroundTruth.class)).forEach(groundTruth::add);
				}
				currentEntry = tarInput.getNextTarEntry();
			}
		}

		return groundTruth;
	}

	private <T> T read(String line, Class<T> clazz) {
		try {
			return mapper.readValue(line, clazz);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
