package de.fhdw.deviceanalyzer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

public class DeviceAnalyzerClient {

	private static final String CMD_PARSE = "parse";

	public static void main(String[] args) throws IOException {
		Locale.setDefault(Locale.UK);

		final IModule module = selectModule(args);

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
			System.err.println("usage: parse config-file-path");
		}
	}

	private static IModule selectModule(String [] args) throws FileNotFoundException, IOException {
		String command = args[0].toLowerCase();
		switch (command) {
		case CMD_PARSE:
			if (args.length !=  2)
				return null;

			// Reads the properties file
			String configPath = args[1];
			Properties properties = new Properties();
			properties.load(new FileInputStream(configPath));
			return new ParsingModule(properties);

		default:
			return null;
		}
	}
}
