package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.MongoConfiguration;
import com.github.mrazjava.booklink.openlibrary.ObjectMapperConfiguration;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.nio.file.Path;

/**
 * Read json, row by row, from openlibrary dump file. Assumes file had been prepped to contain
 * exactly one JSON record per line. The {@code dumpFile} argument can be just the name of a
 * (sample) file, or a full path to a real import file.
 *
 * Sample invocations:
 * mvn spring-boot:run -Dspring-boot.run.arguments="--dumpFile=/tmp/ol_dump_works_latest.json --schemaClassName=WorkSchema --frequencyCheck=5000"
 * mvn spring-boot:run -Dspring-boot.run.arguments="--dumpFile=authors-tail-n1000.json --schemaClassName=AuthorSchema --frequencyCheck=100"
 * mvn clean spring-boot:run -Dspring-boot.run.arguments="--dumpFile=works-head-n1000.json"
 */
@Slf4j
@SpringBootApplication(scanBasePackageClasses = {
		MongoConfiguration.class,
		ObjectMapperConfiguration.class
})
public class ImporterApp implements ApplicationRunner {

	@Autowired
	private FileImporter importer;

	@Value("${booklink.di.ol-dump-file}")
	private String dumpFile;

	@Value("${booklink.di.schema-class-name}")
	private String schemaClassName;

	/**
	 * For large files, it may be desirable to every so often perform some processing.
	 * For example, if total rows is 1000, setting this value to 100, would instruct
	 * the importer to perform some action ten times after each batch of 100 records
	 * processed.
	 */
	@Value("${booklink.di.frequency-check}")
	private int frequencyCheck;

	@Value("${booklink.di.persist}")
	private boolean persistData;

	@Value("${booklink.di.persist-override}")
	private boolean persistDataOverride;

	@Value("${booklink.di.author-image-dir}")
	private String authorImgDir;

	@Value("${booklink.di.author-image-mongo}")
	private Boolean storeAuthorImgInMongo;


	public static void main(String[] args) {

		SpringApplication.run(ImporterApp.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		Class schemaClass = Class.forName(
				schemaClassName.contains(".") ?
				schemaClassName :
				String.format("com.github.mrazjava.booklink.openlibrary.schema.%s", schemaClassName)
		);

		File importFile = null;

		if(FilenameUtils.indexOfLastSeparator(dumpFile) != -1) {
			importFile = new File(dumpFile);
		}

		if(importFile == null) {
			String sampleFilePath = String.format("%sopenlibrary%ssamples%s%s", StringUtils.repeat(File.separator, 3), dumpFile);
			importFile = new File(getClass().getResource(sampleFilePath).getFile());
		}

		if(log.isInfoEnabled()) {
			StringBuilder msg = new StringBuilder("starting...\n\n- importFile: {}\n- schemaClass: {}\n- frequencyCheck: {}\n- persistData: {}\n- persistDataOverride: {}");

			if(schemaClass.equals(AuthorSchema.class)) {
				msg.append("\n- authorImgDir: ");
				msg.append(StringUtils.isBlank(authorImgDir) ? "feature DISABLED" : authorImgDir);
				msg.append("\n- storeAuthorImgInMongo: ");
				msg.append(storeAuthorImgInMongo);
			}

			msg.append("\n");

			log.info(msg.toString(),
					importFile.getAbsolutePath(),
					schemaClass.getCanonicalName(),
					frequencyCheck,
					persistData,
					persistDataOverride);
		}

		importer.runImport(importFile, schemaClass);
	}
}
