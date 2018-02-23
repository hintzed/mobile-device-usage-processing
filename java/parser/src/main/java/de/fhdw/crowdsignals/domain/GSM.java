package de.fhdw.crowdsignals.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.fhdw.deviceanalyzer.parser.core.Event;
import de.fhdw.deviceanalyzer.util.TimeUtil;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GSM implements Comparable<GSM> {

	public String[] mnc;
	public String[] mcc;
	public String[] lac;
	public String[] cid;
	public String[] ci;
	public String[] pci;
	public String[] tac;
	public String[] bid;
	public String[] nid;
	public String[] sid;
	public String[] psc;
	public long[] timestamp;

	public long pane_start;

	@Override
	public int compareTo(GSM o) {
		return Long.compare(this.pane_start, o.pane_start);
	}

	public List<Event> toEvents() {
		List<Event> events = new ArrayList<>();

		for (int i = 0; i < mnc.length; i++) {
			if (cid.length > i && !"null".equals(cid[i])) { // GSM
				events.add(new Event(timestamp[i], timestamp[i], TimeUtil.timestampToWallclock(timestamp[i]), "phone|celllocation|cid", cid[i]));
				events.add(new Event(timestamp[i], timestamp[i], TimeUtil.timestampToWallclock(timestamp[i]), "phone|celllocation|lac", lac[i]));
			} else if (bid.length > i && !"null".equals(bid[i])) { // BID
				events.add(new Event(timestamp[i], timestamp[i], TimeUtil.timestampToWallclock(timestamp[i]), "phone|celllocation|cid", sid[i] + nid[i]));
				events.add(new Event(timestamp[i], timestamp[i], TimeUtil.timestampToWallclock(timestamp[i]), "phone|celllocation|lac", bid[i]));
			} else if (!"null".equals(tac[i])) { // LTE
				events.add(new Event(timestamp[i], timestamp[i], TimeUtil.timestampToWallclock(timestamp[i]), "phone|celllocation|cid", mnc[i] + mcc[i]));
				events.add(new Event(timestamp[i], timestamp[i], TimeUtil.timestampToWallclock(timestamp[i]), "phone|celllocation|lac", tac[i] + ci[i]));
			}
		}

		return events;
	}

}
