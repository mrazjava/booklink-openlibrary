package com.github.mrazjava.booklink.openlibrary;

import com.github.mrazjava.booklink.openlibrary.dataimport.DataImport;
import com.github.mrazjava.booklink.openlibrary.dataimport.ImportInputValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

/**
 * Imports data dump from openlibrary.org into a mongo database. Supports imports for authors,
 * works and editions but each must be ran in a separate process.
 */
@Profile(OpenLibraryImportApp.PROFILE)
@Slf4j
@ComponentScan(
		excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern="com.github.mrazjava.booklink.openlibrary.rest.*")
)
@SpringBootApplication
public class OpenLibraryImportApp implements CommandLineRunner {

	public static final String PROFILE = "IMPORT";

	@Autowired
	private DataImport importer;

	@Value("${booklink.di.ol-dump-file}")
	private String dumpFilePath;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private ImportInputValidator inputValidator;


	public static void main(String[] args) {

		ConfigurableApplicationContext context = new SpringApplicationBuilder()
				.sources(OpenLibraryImportApp.class)
				.profiles(OpenLibraryImportApp.PROFILE)
				.run(args);

		log.info("import finished, asking Spring to exit the process...");

		context.close();
	}

	@Override
	public void run(String... args) throws Exception {

		boolean sample = !StringUtils.startsWith(dumpFilePath, "/");
		File importFile = sample ?
				(new ClassPathResource(dumpFilePath)).getFile() :
				new File(dumpFilePath);

		inputValidator.validate(importFile);
		importer.runImport(importFile);
	}

	public static File openFile(String workingDirectory, String file) {
		File sampleAuthorIdFile = new File(file);
		return sampleAuthorIdFile.isAbsolute() ?
				sampleAuthorIdFile :
				new File(workingDirectory + File.separator + file);
	}
}
