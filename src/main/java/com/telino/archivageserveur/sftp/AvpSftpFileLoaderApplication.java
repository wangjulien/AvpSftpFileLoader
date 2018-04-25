package com.telino.archivageserveur.sftp;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.telino.archivageserveur.sftp.config.SftpConfig;

@SpringBootApplication
@IntegrationComponentScan
@EnableIntegration
@EnableScheduling
//@PropertySource("classpath:sftp.properties")
public class AvpSftpFileLoaderApplication implements CommandLineRunner {

	@Value("${sftp.local.directory.fourn_papier}")
	private String sftpLocalDirectoryFournPapier;

	@Value("${sftp.local.directory.fourn_mail}")
	private String sftpLocalDirectoryFournMail;

	@Value("${sftp.local.directory.evac}")
	private String sftpLocalDirectoryEvac;

	@Value("${sftp.local.directory.autofac}")
	private String sftpLocalDirectoryAutofac;

	@Value("${sftp.filter.purg-weeks:1}")
	private long purgeWeeks;
	
	private List<File> filterFolders;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(AvpSftpFileLoaderApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
	}

	// access command line arguments
	@Override
	public void run(String... args) throws Exception {
		filterFolders = Arrays.asList(
				new File(sftpLocalDirectoryFournPapier + SftpConfig.FILTER_FILE_FOLDER),
				new File(sftpLocalDirectoryFournMail + SftpConfig.FILTER_FILE_FOLDER),
				new File(sftpLocalDirectoryEvac + SftpConfig.FILTER_FILE_FOLDER),
				new File(sftpLocalDirectoryAutofac + SftpConfig.FILTER_FILE_FOLDER));
		filterFolders.forEach(File::mkdir);
	}

	@Scheduled(cron = "${sftp.filter.purg.cronexp}")
	public void purgeFilterFiles() {
		// Purge
		if (purgeWeeks > 0) {
			for (File filterFileFolder : filterFolders) {
				for (File f : filterFileFolder.listFiles()) {
					String fileName = f.getName();
					if (LocalDate.parse(fileName.substring(fileName.length() - 10))
							.isBefore(LocalDate.now().minusWeeks(purgeWeeks)))
						f.delete();
				}
			}
		}
	}
}
