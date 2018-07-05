package com.telino.archivageserveur.sftp.config;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.springframework.integration.file.filters.AbstractFileListFilter;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class RemoteFileAcceptOnceFilter extends AbstractFileListFilter<ChannelSftp.LsEntry> {

	private final String fileNamePath;
	private final Path filterFileFolder;

	public RemoteFileAcceptOnceFilter(final String filterFileFolder, final String filterFileName) {
		super();
		this.fileNamePath = filterFileFolder + filterFileName.substring(filterFileName.lastIndexOf("/"));
		this.filterFileFolder = Paths.get(filterFileFolder);
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
			System.err.println(e.getMessage());
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

		Path filterFile = Paths.get(fileNamePath + "_" + LocalDate.now().toString());
		if (!Files.exists(filterFile))
			Files.createFile(filterFile);

		Files.write(filterFile, lineToApp.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}

	private boolean isFileDownloaded(String name) throws IOException {
		try (DirectoryStream<Path> folderFilesStream = Files.newDirectoryStream(filterFileFolder)) {
			for (Path file : folderFilesStream) {
				try (Stream<String> stream = Files.lines(file)) {
					if (stream.anyMatch(line -> line.contains(name) && !line.contains("filepart")))
						return true;
				}
			}
		}

		return false;
	}
}
