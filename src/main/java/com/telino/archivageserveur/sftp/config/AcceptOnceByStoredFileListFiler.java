package com.telino.archivageserveur.sftp.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

import org.springframework.integration.file.filters.AcceptOnceFileListFilter;

public class AcceptOnceByStoredFileListFiler extends AcceptOnceFileListFilter<File> {
	
	private final File filterFile;
	
	public AcceptOnceByStoredFileListFiler(final String fileUri) {
		super();
		filterFile = new File(fileUri);
	}

	@Override
	public boolean accept(File file) {
		if ( super.accept(file) ) {
			try {
				if ( isFileDownloaded(file.getName()) ) {
					// Already downloaded
					return false;
				} else {
					// not yet, remember in the downloaded list
					addFileName(file.getName());
					return true;
				}
			} catch (IOException e) {
				return false;
			}
			
		} else {
			return false;
		}
	}

	private void addFileName(String name) throws IOException {
		Files.write(filterFile.toPath(), (name+System.lineSeparator()).getBytes(), 
				StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}

	private boolean isFileDownloaded(String name) throws IOException {
		try (Stream<String> stream = Files.lines(filterFile.toPath())) {
			return stream.anyMatch(line -> line.contains(name));
		} 
	}	
}
