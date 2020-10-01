package com.github.mrazjava.booklink.openlibrary;

import com.github.mrazjava.booklink.openlibrary.dataimport.DataImport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

/**
 * Read json, row by row, from openlibrary dump file. Assumes file had been prepped to contain
 * exactly one JSON record per line. The {@code dumpFile} argument can be just the name of a
 * (sample) file, or a full path to a real import file.
 */
@Slf4j
@SpringBootApplication(scanBasePackageClasses = {
		MongoConfiguration.class,
		ObjectMapperConfiguration.class
})
public class OpenLibraryImportApp implements ApplicationRunner {

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


	public static void main(String[] args) {

		SpringApplication.run(OpenLibraryImportApp.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		boolean sample = !StringUtils.startsWith(dumpFilePath, "/");
		File importFile = sample ?
				(new ClassPathResource(dumpFilePath)).getFile() :
				new File(dumpFilePath);

		if(log.isInfoEnabled()) {

			log.info("starting...\n\n" +
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
