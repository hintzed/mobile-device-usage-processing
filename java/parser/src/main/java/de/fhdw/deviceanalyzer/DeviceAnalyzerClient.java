package de.fhdw.deviceanalyzer;

import java.io.IOException;
import java.util.Locale;

public class DeviceAnalyzerClient {

	private static final String CMD_PARSE = "parse";

	public static void main(String[] args) throws IOException {
		Locale.setDefault(Locale.UK);

		final IModule module = (args.length == 1) ? selectModule(args[0].toLowerCase()) : null;

		final Thread mainThread = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (module != null) module.shutdown();
				try {
					mainThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		if (module != null) {
			module.execute();
		} else {
			System.err.println("usage: [parse]");
		}
	}

	private static IModule selectModule(String command) {
		switch (command) {
		case CMD_PARSE:
			return new ParsingModule();
		default:
			return null;
		}
	}
}
