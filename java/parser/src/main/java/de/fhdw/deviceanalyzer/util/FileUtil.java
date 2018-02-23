package de.fhdw.deviceanalyzer.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileUtil {
	public static void writeToFile(List<String> lines, File file, OpenOption... options) {
		try {
			Files.write(file.toPath(), lines, Charset.forName("UTF-8"), options);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static void writeToFile(List<String> lines, File file) {
		file.delete();
		writeToFile(lines, file, StandardOpenOption.CREATE);
	}
}
