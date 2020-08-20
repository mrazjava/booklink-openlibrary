package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.MongoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.File;

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
		MongoConfiguration.class
})
public class ImporterApp implements ApplicationRunner {

	@Autowired
	private FileImporter importer;

	@Value("${dumpFile:works-tail-n30.json}")
	private String dumpFile;

	@Value("${schemaClassName:WorkSchema}")
	private String schemaClassName;

	/**
	 * For large files, it may be desirable to every so often perform some processing.
	 * For example, if total rows is 1000, setting this value to 100, would instruct
	 * the importer to perform some action ten times after each batch of 100 records
	 * processed.
	 */
	@Value("${booklink.data-importer.frequency-check}")
	private int frequencyCheck;

	public static void main(String[] args) {

		SpringApplication.run(ImporterApp.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		log.debug("OpenLibrary dump import. INPUTS:\n\n- dumpFile: {}\n- schemaClassName: {}\n- frequencyCheck: {}\n", dumpFile, schemaClassName, frequencyCheck);

		Class schemaClass = Class.forName(
				schemaClassName.contains(".") ?
				schemaClassName :
				String.format("com.github.mrazjava.booklink.openlibrary.schema.%s", schemaClassName)
		);

		File importFile = null;

		if(dumpFile.contains("/") || dumpFile.contains("\\")) {
			importFile = new File(dumpFile);
		}

		if(importFile == null) {
			String sampleFilePath = String.format("/openlibrary/samples/%s", dumpFile);
			importFile = new File(getClass().getResource(sampleFilePath).getFile());
		}

		log.info("starting...\n\n- importFile: {}\n- schemaClass: {}\n- frequencyCheck: {}\n", importFile.getAbsolutePath(), schemaClass.getCanonicalName(), frequencyCheck);

		importer.runImport(importFile, schemaClass);
	}
}
