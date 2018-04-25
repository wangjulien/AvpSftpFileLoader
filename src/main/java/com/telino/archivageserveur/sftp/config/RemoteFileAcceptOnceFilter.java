package com.telino.archivageserveur.sftp.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.springframework.integration.file.filters.AbstractFileListFilter;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class RemoteFileAcceptOnceFilter extends AbstractFileListFilter<ChannelSftp.LsEntry> {

	private final String fileNamePath;

	private final File filterFileFolder;
	private File filterFile;

	public RemoteFileAcceptOnceFilter(final String filterFileFolder, final String filterFileName) {
		super();
		this.fileNamePath = filterFileFolder + filterFileName;
		this.filterFileFolder = new File(filterFileFolder);
	}

	@Override
	public boolean accept(LsEntry file) {
		try {
			if (isFileDownloaded(getFilename(file))) {
				// Already downloaded
				return false;
			} else {
				// not yet, remember in the downloaded list
				addFileName(getFilename(file));
				return true;
			}
		} catch (IOException e) {
			System.err.println(e);
			return false;
		}
	}

	private String getFilename(LsEntry entry) {
		return (entry != null) ? entry.getFilename() : null;
	}

	private void addFileName(String name) throws IOException {
		StringBuilder lineToApp = new StringBuilder(LocalDateTime.now().toString());
		lineToApp.append("\t");
		lineToApp.append(name);
		lineToApp.append(System.lineSeparator());

		Files.write(filterFile.toPath(), lineToApp.toString().getBytes(), StandardOpenOption.CREATE,
				StandardOpenOption.APPEND);
	}

	private boolean isFileDownloaded(String name) throws IOException {
		filterFile = new File(fileNamePath + "_" + LocalDate.now().toString());
		if (!filterFile.exists())
			filterFile.createNewFile();

		// Check
		for (File f : filterFileFolder.listFiles()) {
			try (Stream<String> stream = Files.lines(f.toPath())) {
				if (stream.anyMatch(line -> line.contains(name)))
					return true;
			}
		}

		return false;
	}
}
