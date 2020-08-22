package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.repository.EditionRepository;
import com.github.mrazjava.booklink.openlibrary.repository.WorkRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

/**
 * <a href="https://itnext.io/using-java-to-read-really-really-large-files-a6f8a3f44649">Benchmarks</a>
 */
@Slf4j
@Component
public class CommonsLineIterator implements FileImporter {

    private ObjectMapper objectMapper;

    @Value("${booklink.di.frequency-check}")
    private int frequencyCheck;

    @Value("${booklink.di.persist}")
    private boolean persistData;

    @Value("${booklink.di.author-image-dir}")
    private String authorImgDir;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private EditionRepository editionRepository;

    private File workingDirectory;

    private File destinationAuthorImg;

    public static final char[] IMAGE_SIZES = {'S', 'M', 'L'};

    public static final String AUTHOR_OLID_IMG_URL_TEMPLATE = "http://covers.openlibrary.org/a/olid/%s-%s.jpg";

    public static final long AUTHOR_IMG_THROTTLE_MS = 3000;

    public CommonsLineIterator() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void runImport(File jsonFile, Class schema) {

        String line = null;
        long counter = 0;
        try {
            LineIterator iterator = FileUtils.lineIterator(jsonFile, "UTF-8");
            final String workingDir = jsonFile.getParent();

            log.info("workingDir: {}", workingDir);

            workingDirectory = new File(workingDir);
            if(StringUtils.isNotBlank(authorImgDir)) {
                destinationAuthorImg = Path.of(authorImgDir).getParent() == null ?
                        Path.of(workingDir + File.separator + authorImgDir).toFile() :
                        Path.of(authorImgDir).toFile();
                if(!destinationAuthorImg.exists()) {
                    destinationAuthorImg.mkdir();
                }
            }

            log.info("destinationAuthorImg: {}", destinationAuthorImg);

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            while(iterator.hasNext()) {
                line = iterator.next();
                counter++;
                Object pojo = objectMapper.readValue(line, schema);
                if(counter % frequencyCheck == 0) {
                    if(log.isDebugEnabled()) {
                        log.debug("raw JSON #{}:\n{}", counter, line);
                    }
                    if(log.isInfoEnabled()) {
                        log.info("JSON #{}:\n{}", counter, objectMapper.writeValueAsString(pojo));
                    }
                }
                processLine(line, schema);

                if(log.isTraceEnabled()) {
                    log.trace("raw JSON #{}:\n{}", counter, line);
                    log.trace("raw JSON #{}:\n{}", counter, objectMapper.writeValueAsString(pojo));
                }
            }
            stopWatch.stop();
            log.info("TOTAL RECORDS: {}, time: {}", counter, stopWatch.getTime());
        } catch (Exception e) {
            log.error("JSON #{} failed:\n{}\n\nERROR: {}", counter, line, e.getMessage());
        }
    }

    private void processLine(String line, Class schema) throws JsonProcessingException {

        Object pojo = objectMapper.readValue(line, schema);

        if(AuthorSchema.class.equals(schema)) {
            processAuthor((AuthorSchema)pojo);
        }
        else if(WorkSchema.class.equals(schema)) {
            processWork((WorkSchema)pojo);
        }
        else if(EditionSchema.class.equals(schema)) {
            processEdition((EditionSchema)pojo);
        }
        else {
            log.warn("unsupported schema type: {}", schema);
        }
    }

    private void processAuthor(AuthorSchema author) {
        if(persistData) {
            authorRepository.save(author);
        }
        if(StringUtils.isNotBlank(authorImgDir)) {
            downloadAuthorImages(author);
        }
    }

    private void processWork(WorkSchema work) {
        if(persistData) {
            workRepository.save(work);
        }
    }

    private void processEdition(EditionSchema edition) {
        if(persistData) {
            editionRepository.save(edition);
        }
    }

    private void downloadAuthorImages(AuthorSchema author) {

        if(CollectionUtils.isEmpty(author.getPhotos())) {
            return;
        }

        for(char imgSize : IMAGE_SIZES) {

            File imgById = new File(
                    destinationAuthorImg.getAbsolutePath() +
                            File.separator +
                            author.getId() +
                            String.format("-%s.jpg", imgSize)
            );

            if(imgById.exists()) {
                log.debug("file exists, skipping: {}", imgById);
                return;
            }

            String imgUrl = String.format(AUTHOR_OLID_IMG_URL_TEMPLATE, author.getId(), imgSize);

            log.info("downloading.... {}", imgUrl);

            try {
                FileUtils.copyURLToFile(new URL(imgUrl), imgById,1000,500);

                // only 100 requests/IP are allowed for every 5 minutes
                // see RATE LIMITING: https://openlibrary.org/dev/docs/api/covers
                //
                // 5 min * 60 secs = 300s / 100 requests = 3 sec per request
                //
                // randomize sleep value within 1 second of a defined throttle value
                long sleep = RandomUtils.nextLong(AUTHOR_IMG_THROTTLE_MS-1000, AUTHOR_IMG_THROTTLE_MS);

                log.info("OK! {} | sleeping {}ms", imgUrl, sleep);

                Thread.sleep(sleep); // throttle to ensure no more than 100 requests per 5 min
            } catch (Exception e) {
                log.error("download failed: {}", e.getMessage());
            }
        }
    }
}
