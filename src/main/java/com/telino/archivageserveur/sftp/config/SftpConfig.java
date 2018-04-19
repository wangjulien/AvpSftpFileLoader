package com.telino.archivageserveur.sftp.config;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

import com.jcraft.jsch.ChannelSftp.LsEntry;

@Configuration
@PropertySource("classpath:sftp.properties")
public class SftpConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(SftpConfig.class);
	
	public static final String FILTER_FILE_FOLDER = "/log";

	@Value("${sftp.host}")
	private String sftpHost;

	@Value("${sftp.port:22}")
	private int sftpPort;

	@Value("${sftp.user}")
	private String sftpUser;

	@Value("${sftp.privateKey:#{null}}")
	private Resource sftpPrivateKey;

	@Value("${sftp.privateKeyPassphrase:}")
	private String sftpPrivateKeyPassphrase;

	@Value("${sftp.password:#{null}}")
	private String sftpPasword;

	@Value("${sftp.remote.directory.fourn_papier}")
	private String sftpRemoteDirectoryFournPapier;
	@Value("${sftp.local.directory.fourn_papier}")
	private String sftpLocalDirectoryFournPapier;

	@Value("${sftp.remote.directory.fourn_mail}")
	private String sftpRemoteDirectoryFournMail;
	@Value("${sftp.local.directory.fourn_mail}")
	private String sftpLocalDirectoryFournMail;

	@Value("${sftp.remote.directory.evac}")
	private String sftpRemoteDirectoryEvac;
	@Value("${sftp.local.directory.evac}")
	private String sftpLocalDirectoryEvac;

	@Value("${sftp.remote.directory.autofac}")
	private String sftpRemoteDirectoryAutofac;
	@Value("${sftp.local.directory.autofac}")
	private String sftpLocalDirectoryAutofac;

	@Value("${sftp.remote.directory.download.filter:*.*}")
	private String sftpRemoteDirectoryDownloadFilter;
		
	@Bean
	public SessionFactory<LsEntry> sftpSessionFactory() {
		DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
		factory.setHost(sftpHost);
		factory.setPassword(sftpPasword);
		factory.setPort(sftpPort);
		factory.setUser(sftpUser);
		if (sftpPrivateKey != null) {
			factory.setPrivateKey(sftpPrivateKey);
			factory.setPrivateKeyPassphrase(sftpPrivateKeyPassphrase);
		}
		factory.setAllowUnknownKeys(true);
		return new CachingSessionFactory<LsEntry>(factory);
	}

	// factures_fourn_papier

	@Bean
	public SftpInboundFileSynchronizer fournPapierSftpInboundFileSynchronizer() {
		SftpInboundFileSynchronizer fileSynchronizer = new SftpInboundFileSynchronizer(sftpSessionFactory());
		fileSynchronizer.setDeleteRemoteFiles(true);
		fileSynchronizer.setRemoteDirectory(sftpRemoteDirectoryFournPapier);
		fileSynchronizer.setFilter(new RemoteFileAcceptOnceFilter(
				sftpLocalDirectoryFournPapier + FILTER_FILE_FOLDER, sftpRemoteDirectoryFournPapier));
		return fileSynchronizer;
	}

	@Bean
	@InboundChannelAdapter(channel = "fournPapierSftpChannel", poller = @Poller(cron = "0/5 * * * * *"))
	public MessageSource<File> fournPapierSftpMessageSource() {
		SftpInboundFileSynchronizingMessageSource source = new SftpInboundFileSynchronizingMessageSource(
				fournPapierSftpInboundFileSynchronizer());
		source.setLocalDirectory(new File(sftpLocalDirectoryFournPapier));
		source.setAutoCreateLocalDirectory(true);
		source.setLocalFilter(new AcceptOnceFileListFilter<File>());
		return source;
	}

	@Bean
	@ServiceActivator(inputChannel = "fournPapierSftpChannel")
	public MessageHandler fournPapierResultFileHandler() {
		return new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) {
				LOGGER.info("File downloaded : {} ", message.getPayload().toString());
			}
		};
	}

	// factures_fourn_mail

	@Bean
	public SftpInboundFileSynchronizer fournMailSftpInboundFileSynchronizer() {
		SftpInboundFileSynchronizer fileSynchronizer = new SftpInboundFileSynchronizer(sftpSessionFactory());
		fileSynchronizer.setDeleteRemoteFiles(true);
		fileSynchronizer.setRemoteDirectory(sftpRemoteDirectoryFournMail);
		fileSynchronizer.setFilter(new RemoteFileAcceptOnceFilter(
				sftpLocalDirectoryFournMail + FILTER_FILE_FOLDER, sftpRemoteDirectoryFournMail));
		return fileSynchronizer;
	}

	@Bean
	@InboundChannelAdapter(channel = "fournMailSftpChannel", poller = @Poller(cron = "0/32 * * * * *"))
	public MessageSource<File> fournEmailSftpMessageSource() {
		SftpInboundFileSynchronizingMessageSource source = new SftpInboundFileSynchronizingMessageSource(
				fournMailSftpInboundFileSynchronizer());
		source.setLocalDirectory(new File(sftpLocalDirectoryFournMail));
		source.setAutoCreateLocalDirectory(true);
		source.setLocalFilter(new AcceptOnceFileListFilter<File>());
		return source;
	}

	@Bean
	@ServiceActivator(inputChannel = "fournMailSftpChannel")
	public MessageHandler fournMailResultFileHandler() {
		return new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) {
				LOGGER.info("File downloaded : {} ", message.getPayload());
			}
		};
	}

	// factures_evac

	@Bean
	public SftpInboundFileSynchronizer evacSftpInboundFileSynchronizer() {
		SftpInboundFileSynchronizer fileSynchronizer = new SftpInboundFileSynchronizer(sftpSessionFactory());
		fileSynchronizer.setDeleteRemoteFiles(true);
		fileSynchronizer.setRemoteDirectory(sftpRemoteDirectoryEvac);
		fileSynchronizer.setFilter(new RemoteFileAcceptOnceFilter(
				sftpLocalDirectoryEvac + FILTER_FILE_FOLDER, sftpRemoteDirectoryEvac));
		return fileSynchronizer;
	}

	@Bean
	@InboundChannelAdapter(channel = "evacSftpChannel", poller = @Poller(cron = "0/33 * * * * *"))
	public MessageSource<File> evacSftpMessageSource() {
		SftpInboundFileSynchronizingMessageSource source = new SftpInboundFileSynchronizingMessageSource(
				evacSftpInboundFileSynchronizer());
		source.setLocalDirectory(new File(sftpLocalDirectoryEvac));
		source.setAutoCreateLocalDirectory(true);
		source.setLocalFilter(new AcceptOnceFileListFilter<File>());
		return source;
	}

	@Bean
	@ServiceActivator(inputChannel = "evacSftpChannel")
	public MessageHandler evacResultFileHandler() {
		return new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) {
				LOGGER.info("File downloaded : {} ", message.getPayload());
			}
		};
	}

	// factures_autofac

	@Bean
	public SftpInboundFileSynchronizer autofacSftpInboundFileSynchronizer() {
		SftpInboundFileSynchronizer fileSynchronizer = new SftpInboundFileSynchronizer(sftpSessionFactory());
		fileSynchronizer.setDeleteRemoteFiles(true);
		fileSynchronizer.setRemoteDirectory(sftpRemoteDirectoryAutofac);
		fileSynchronizer.setFilter(new RemoteFileAcceptOnceFilter(
				sftpLocalDirectoryAutofac + FILTER_FILE_FOLDER, sftpRemoteDirectoryAutofac));
		return fileSynchronizer;
	}

	@Bean
	@InboundChannelAdapter(channel = "autofacSftpChannel", poller = @Poller(cron = "0/34 * * * * *"))
	public MessageSource<File> autofacSftpMessageSource() {
		SftpInboundFileSynchronizingMessageSource source = new SftpInboundFileSynchronizingMessageSource(
				autofacSftpInboundFileSynchronizer());
		source.setLocalDirectory(new File(sftpLocalDirectoryAutofac));
		source.setAutoCreateLocalDirectory(true);
		source.setLocalFilter(new AcceptOnceFileListFilter<File>());
		return source;
	}

	@Bean
	@ServiceActivator(inputChannel = "autofacSftpChannel")
	public MessageHandler autofacResultFileHandler() {
		return new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) {
				LOGGER.info("File downloaded : {} ", message.getPayload().toString());
			}
		};
	}
}