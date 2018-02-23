package de.fhdw.deviceanalyzer.parser;

import java.util.Arrays;
import java.util.List;

import de.fhdw.deviceanalyzer.parser.core.Event;

public interface IParser {

	static final List<String> RESET_KEYS = Arrays.asList(
			"pause",
			"startup",
			"shutdown");
	

	public void startParsing(String deviceId);

	public void parse(Event event);

	public void endParsing(String deviceId);

}
