package de.fhdw.deviceanalyzer.parser;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import de.fhdw.deviceanalyzer.parser.core.ContextUtil;
import de.fhdw.deviceanalyzer.parser.core.Event;
import de.fhdw.deviceanalyzer.parser.wifi.ScanData;
import de.fhdw.deviceanalyzer.parser.wifi.WifiCluster;
import de.fhdw.deviceanalyzer.parser.wifi.WifiIdCount;

public class WifiScanContextLocationParser implements IParser {

	public Map<String, WifiIdCount> resultWifiIdCount;
	public Map<String, WifiCluster> resultWifiCluster;
	public Map<Date, ScanData> scannedData;
	private List<WifiCluster> locations;

	@Override
	public void startParsing(String deviceId) {
		resultWifiCluster = new HashMap<>();
		resultWifiIdCount = new HashMap<>();
		scannedData = new HashMap<>();
	}

	@Override
	public void parse(Event event) {
		if (event.key.startsWith("wifi|scan") && event.key.contains("ssid")) {
			String mac = event.key.split("\\|")[2];
			saveScannedData(mac, event.wallClock);
			countWifiId(mac);
		}
	}

	@Override
	public void endParsing(String deviceId) {
		printTime("Parsing Done");
		// Sort Wifi Ids
		List<WifiIdCount> sortedWifiCount = new LinkedList<>(resultWifiIdCount.values());
		Collections.sort(sortedWifiCount);

		//sortedWifiCount.stream().limit(40).forEach(c -> System.out.println(c));

		// Cluster Build - Count - Sort
		buildCluster(sortedWifiCount);
		// printTime("Clusterbuild Done");
		countCluster();
		// printTime("ClusterCount Done");
		locations =  Lists.newArrayList(resultWifiCluster.values());
		ContextUtil.tagMeaningfullLocations(locations);

//		resultWifiCluster.values().forEach(System.out::println);
//		meaningfullLocations.forEach(System.out::println);

		printTime("sysout Done");
	}

	public List<WifiCluster> getLocations() {
		return locations;
	}

	private void saveScannedData(String wifiId, Date wallClock) {
		ScanData scanData = scannedData.get(wallClock);
		if (scanData == null) {
			scanData = new ScanData(wallClock, new HashSet<>());
			scannedData.put(wallClock, scanData);
		}
		scanData.wifiIds.add(wifiId);
	}

	private void countWifiId(String wifiId) {
		WifiIdCount wifiIdCount = resultWifiIdCount.get(wifiId);
		if (wifiIdCount == null) {
			resultWifiIdCount.put(wifiId, new WifiIdCount(wifiId));
		} else {
			wifiIdCount.count();
		}
	}

	private void buildCluster(List<WifiIdCount> sortedWifiIdCount) {
		Set<String> alreadyUsedWifiIds = new HashSet<>();

		for (WifiIdCount eachWifiIdCount : sortedWifiIdCount) {
			String wifiId = eachWifiIdCount.id;
			if (alreadyUsedWifiIds.contains(wifiId)) continue;
			WifiCluster wifiCluster = new WifiCluster(wifiId);
			for (ScanData eachScannedData : scannedData.values()) {
				if (eachScannedData.wifiIds.contains(wifiId)) {
					Set<String> wifiIdsToAdd = new HashSet<>(eachScannedData.wifiIds);
					wifiIdsToAdd.removeAll(alreadyUsedWifiIds);
					wifiCluster.clusterIds.addAll(wifiIdsToAdd);
					alreadyUsedWifiIds.addAll(wifiIdsToAdd);
				}
			}
			resultWifiCluster.put(wifiId, wifiCluster);
		}
	}

	private void countCluster() {
		resultWifiCluster
				.values()
				.stream()
				.forEach(c -> scannedData.values()
						.stream()
						.filter(sd -> !Collections.disjoint(c.clusterIds, sd.wifiIds))
						.forEach(d -> c.count(d.wallClock))
				);
	}

	private void printTime(String action) {
		// System.out.println(action + ": " + new Date());
	}

}
