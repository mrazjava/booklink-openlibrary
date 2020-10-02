package com.github.mrazjava.booklink.openlibrary;

import com.github.mrazjava.booklink.openlibrary.dataimport.DataImport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

/**
 * Imports data dump from openlibrary.org into a mongo database. Supports imports for authors,
 * works and editions but each must be ran separately.
 */
@Profile(OpenLibraryImportApp.PROFILE)
@Slf4j
@SpringBootApplication(scanBasePackageClasses = {
		MongoConfiguration.class,
		ObjectMapperConfiguration.class
})
public class OpenLibraryImportApp {

	public static final String PROFILE = "IMPORT";

	@Autowired
	private DataImport importer;

	@Value("${booklink.di.ol-dump-file}")
	private String dumpFilePath;

	@Value("${booklink.di.handler-class}")
	private String handlerClass;

	@Value("${booklink.di.start-from-record-no}")
	private int startWithRecordNo;

	/**
	 * For large files, it may be desirable to every so often perform some processing.
	 * For example, if total rows is 1000, setting this value to 100, would instruct
	 * the importer to perform some action ten times after each batch of 100 records
	 * processed.
	 */
	@Value("${booklink.di.frequency-check}")
	private int frequencyCheck;

	@Value("${booklink.di.persist}")
	private Boolean persistData;

	@Value("${booklink.di.persist-override}")
	private Boolean persistDataOverride;

	@Value("${booklink.di.image-pull}")
	private Boolean imagePull;

	@Value("${booklink.di.image-dir}")
	private String imageDir;

	@Value("${booklink.di.with-mongo-images}")
	private Boolean withMongoImages;

	@Value("${booklink.di.fetch-original-images}")
	private Boolean fetchOriginalImages;

	@Autowired
	private ApplicationContext context;


	public static void main(String[] args) {

		new SpringApplicationBuilder()
				.sources(OpenLibraryImportApp.class)
				.profiles(OpenLibraryImportApp.PROFILE)
				.run(args);
	}

	@EventListener(ApplicationReadyEvent.class)
	void initialize() throws Exception {

		boolean sample = !StringUtils.startsWith(dumpFilePath, "/");
		File importFile = sample ?
				(new ClassPathResource(dumpFilePath)).getFile() :
				new File(dumpFilePath);

		if(log.isInfoEnabled()) {

			log.info("Booklink-OpenLibrary Import\n\n" +
					"booklink.di:\n" +
					" ol-dump-file: {}\n" +
					" handler-class: {}\n" +
					" start-from-record-no: {}\n" +
					" frequency-check: {}\n" +
					" persist: {}\n" +
					" persist-override: {}\n" +
					" image-pull: {}\n" +
					" image-dir: {}\n" +
					" with-mongo-images: {}\n" +
					" fetch-original-images: {}\n",
					importFile.getAbsolutePath(),
					handlerClass,
					startWithRecordNo,
					frequencyCheck,
					persistData,
					persistDataOverride,
					imagePull,
					StringUtils.isBlank(imageDir) ? "feature DISABLED" : imageDir,
					withMongoImages,
					fetchOriginalImages
			);
		}

		importer.runImport(importFile);
	}
}
