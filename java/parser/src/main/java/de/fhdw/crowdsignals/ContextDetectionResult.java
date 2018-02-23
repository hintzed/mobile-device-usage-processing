package de.fhdw.crowdsignals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.fhdw.deviceanalyzer.parser.core.AbstractContextLocation.Context;
import de.fhdw.deviceanalyzer.parser.core.DeviceInfo;

public class ContextDetectionResult {

	public int total;

	public int homeCorrectCombined;
	public int officeCorrectCombined;
	public int homeIncorrectCombined;
	public int officeIncorrectCombined;

	public int homeCorrectWiFi;
	public int officeCorrectWiFi;
	public int homeIncorrectWiFi;
	public int officeIncorrectWiFi;

	public int homeCorrectGSM;
	public int officeCorrectGSM;
	public int homeIncorrectGSM;
	public int officeIncorrectGSM;

	public Map<String, Integer> contextCount = new HashMap<>();

	public void scoreResult(Date walltime, String value, DeviceInfo deviceInfo) {
		if (!contextCount.containsKey(value)) {
			contextCount.put(value, 1);
		} else {
			contextCount.put(value, contextCount.get(value) + 1);
		}

		Context groundTruth = groundTruthContext(value);
		Context wifi = deviceInfo.wifiContext;
		Context gsm = deviceInfo.gsmContext;
		Context combined = combinedContext(deviceInfo);

		if (groundTruth == Context.HOME) {
			if (wifi == Context.HOME) homeCorrectWiFi++;
			else homeIncorrectWiFi++;

			if (gsm == Context.HOME) homeCorrectGSM++;
			else homeIncorrectGSM++;

			if (combined == Context.HOME) homeCorrectCombined++;
			else homeIncorrectCombined++;
		} else if (groundTruth == Context.OFFICE) {
			if (wifi == Context.OFFICE) officeCorrectWiFi++;
			else officeIncorrectWiFi++;

			if (gsm == Context.OFFICE) officeCorrectGSM++;
			else officeIncorrectGSM++;

			if (combined == Context.OFFICE) officeCorrectCombined++;
			else officeIncorrectCombined++;
		} else {
			if (wifi != Context.OFFICE) officeCorrectWiFi++;
			else officeIncorrectWiFi++;

			if (wifi != Context.HOME) homeCorrectWiFi++;
			else homeIncorrectWiFi++;

			if (gsm != Context.OFFICE) officeCorrectGSM++;
			else officeIncorrectGSM++;

			if (gsm != Context.HOME) homeCorrectGSM++;
			else homeIncorrectGSM++;

			if (combined != Context.OFFICE) officeCorrectCombined++;
			else officeIncorrectCombined++;

			if (combined != Context.HOME) homeCorrectCombined++;
			else homeIncorrectCombined++;
		}

	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();

		b.append("homeCombined: " + String.format("%.2f%%\n",
				100.0 * homeCorrectCombined / (homeCorrectCombined + homeIncorrectCombined)));
		b.append("officeCombined: " + String.format("%.2f%%\n",
				100.0 * officeCorrectCombined / (officeCorrectCombined + officeIncorrectCombined)));

		b.append("homeWiFi: "
				+ String.format("%.2f%%\n", 100.0 * homeCorrectWiFi / (homeCorrectWiFi + homeIncorrectWiFi)));
		b.append("officeWiFi: "
				+ String.format("%.2f%%\n", 100.0 * officeCorrectWiFi / (officeCorrectWiFi + officeIncorrectWiFi)));

		b.append("homeGSM: " + String.format("%.2f%%\n", 100.0 * homeCorrectGSM / (homeCorrectGSM + homeIncorrectGSM)));
		b.append("officeGSM: "
				+ String.format("%.2f%%\n", 100.0 * officeCorrectGSM / (officeCorrectGSM + officeIncorrectGSM)));

		return b.toString();
	}

	public Context groundTruthContext(String place) {
		switch (place) {
		case "Home":
			return Context.HOME;
		case "Work":
			return Context.OFFICE;
		case "Home of Friend or Family":
		case "Fitness/Sports Facility":
		case "Restaurant":
		case "Bar or Nightlife Location":
		case "Supermarket / Grocery":
		case "Shop or Store":
		case "Library":
		case "Museum":
		case "Hospital or Clinic":
		case "School":
		case "Bank":
			return Context.OTHER_MEANINGFUL;
		case "Not in a place, I'm in Transit!":
		case "Government Building":
		case "Currently in Transit":
		case "Transit Station":
		case "Outdoors / Park":
		case "Hotel, Motel, or Hostel":
		case "Other":
		case "Church":
		case "Traffic Stop / Rest Area":
		case "Tourist Attraction":
		case "Performance Venue":
		case "Cafe":
			return Context.ELSEWHERE;
		default:
			System.out.println("Unklassified: " + place);

			return Context.ELSEWHERE;
		}
	}

	public Context combinedContext(DeviceInfo deviceInfo) {
		Context wifiContext = deviceInfo.wifiContext;
		Context gsmContext = deviceInfo.gsmContext;

		if (wifiContext == Context.NO_DATA && gsmContext == Context.NO_DATA) {
			return Context.NO_DATA;
		} else if (wifiContext == Context.HOME || gsmContext == Context.HOME) {
			return Context.HOME;
		} else if (wifiContext == Context.OFFICE || gsmContext == Context.OFFICE) {
			return Context.OFFICE;
		} else if (wifiContext == Context.OTHER_MEANINGFUL || gsmContext == Context.OTHER_MEANINGFUL) {
			return Context.OTHER_MEANINGFUL;
		} else {
			return Context.ELSEWHERE;
		}
	}

}
