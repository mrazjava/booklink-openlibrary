package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.repository.EditionRepository;
import com.github.mrazjava.booklink.openlibrary.repository.WorkRepository;
import com.github.mrazjava.booklink.openlibrary.schema.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Value("${booklink.di.persist-override}")
    private boolean persistDataOverride;

    @Value("${booklink.di.author-image-dir}")
    private String authorImgDir;

    @Value("${booklink.di.author-image-mongo}")
    private Boolean storeAuthorImgInMongo;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private EditionRepository editionRepository;

    private File workingDirectory;

    private File authorImagesDestination;

    public static final String AUTHOR_OLID_IMG_URL_TEMPLATE = "http://covers.openlibrary.org/a/olid/%s-%s.jpg";

    public static final String AUTHOR_PHOTOID_IMG_URL_TEMPLATE = "http://covers.openlibrary.org/a/id/%s-%s.jpg";

    public static final long AUTHOR_IMG_THROTTLE_MS = 2000;

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
            prepareImport(schema);

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
            log.error("JSON #{} failed:\n{}", counter, line, e);
        }
    }

    private void processLine(String line, Class schema) throws JsonProcessingException {

        Object pojo = objectMapper.readValue(line, schema);

        if(isAuthor(schema)) {
            processAuthor((AuthorSchema)pojo);
        }
        else if(isWork(schema)) {
            processWork((WorkSchema)pojo);
        }
        else if(isEdition(schema)) {
            processEdition((EditionSchema)pojo);
        }
        else {
            log.warn("unsupported schema type: {}", schema);
        }
    }

    private void prepareImport(Class schema) {
        if (isAuthor(schema)) {
            prepareAuthorImport();
        }
        else if(isWork(schema)) {
            prepareWorkImport();
        }
        else if(isEdition(schema)) {
            prepareEditionImport();
        }
    }

    private void prepareAuthorImport() {

        if(StringUtils.isNotBlank(authorImgDir)) {
            authorImagesDestination = Path.of(authorImgDir).getParent() == null ?
                    Path.of(workingDirectory.getAbsolutePath() + File.separator + authorImgDir).toFile() :
                    Path.of(authorImgDir).toFile();
            if(!authorImagesDestination.exists()) {
                authorImagesDestination.mkdir();
            }
        }

        log.info("destinationAuthorImg: {}", authorImagesDestination);
    }

    private void processAuthor(AuthorSchema author) {

        AuthorSchema saved = null;

        if(persistData) {
            saved = BooleanUtils.isTrue(persistDataOverride) ?
                    authorRepository.save(author) :
                    authorRepository.findById(author.getId()).orElse(authorRepository.save(author));
        }

        try {
            downloadAuthorImages(Optional.ofNullable(saved).orElse(author));
        }
        catch(IOException e) {
            log.error("problem downloading author images: {}", e.getMessage());
        }
    }

    private void prepareWorkImport() {

    }

    private void processWork(WorkSchema work) {

        WorkSchema saved = null;

        if(persistData) {
            saved = workRepository.findById(work.getId()).orElse(workRepository.save(work));
        }
    }

    private void prepareEditionImport() {

    }

    private void processEdition(EditionSchema edition) {

        EditionSchema saved = null;

        if(persistData) {
            saved = editionRepository.findById(edition.getId()).orElse(editionRepository.save(edition));
        }
    }

    private void downloadAuthorImages(AuthorSchema author) throws IOException {

        if(CollectionUtils.isEmpty(author.getPhotos())) {
            return;
        }

        Integer photoId = author.getPhotos().stream().filter(id -> id > 0).findFirst().orElse(0);

        if(photoId == 0) {
            return;
        }

        Map<ImageSize, File> imgFiles = StringUtils.isNotBlank(authorImgDir) ?
            downloadImageToFile(String.valueOf(photoId), AUTHOR_PHOTOID_IMG_URL_TEMPLATE) :
            null;

        if(BooleanUtils.isTrue(storeAuthorImgInMongo)) {
            downloadImageToBinary(
                    String.valueOf(photoId), AUTHOR_PHOTOID_IMG_URL_TEMPLATE,
                    author,
                    imgFiles == null ? Map.of() : imgFiles
            );
        }
    }

    private Map<ImageSize, File> downloadImageToFile(String imgId, String imgTemplate) throws IOException {

        Map<ImageSize, File> files = new HashMap<>();

        for(ImageSize imgSize : ImageSize.values()) {
            File imgById = new File(
                    authorImagesDestination.getAbsolutePath() +
                            File.separator +
                            String.format("%s-%s.jpg", imgId, imgSize)
            );

            files.put(imgSize, imgById);

            if(imgById.exists()) {
                log.debug("file exists, skipping: {}", imgById);
                continue;
            }
            String imgUrl = String.format(imgTemplate, imgId, imgSize);

            log.info("downloading.... {}", imgUrl);

            FileUtils.copyToFile(new ByteArrayInputStream(downloadImage(imgUrl)), imgById);
        }

        return files;
    }

    private void downloadImageToBinary(
            String imgId, String imgTemplate, DefaultImageSupport imgSupport, Map<ImageSize, File> cache) throws IOException {

        if(!imgSupport.hasSmallImage()) {
            byte[] image = cache.containsKey(ImageSize.S) ?
                    FileUtils.readFileToByteArray(cache.get(ImageSize.S)) :
                    downloadImage(String.format(imgTemplate, imgId, ImageSize.S));
            imgSupport.setSmallImage(image);
        }
        else {
            log.info("skipping binary image[{}]-{}; already exists", imgId, ImageSize.S);
        }

        if(!imgSupport.hasMediumImage()) {
            byte[] image = cache.containsKey(ImageSize.M) ?
                    FileUtils.readFileToByteArray(cache.get(ImageSize.M)) :
                    downloadImage(String.format(imgTemplate, imgId, ImageSize.M));
            imgSupport.setMediumImage(image);
        }
        else {
            log.info("skipping binary image[{}]-{}; already exists", imgId, ImageSize.M);
        }

        if(!imgSupport.hasLargeImage()) {
            byte[] image = cache.containsKey(ImageSize.L) ?
                    FileUtils.readFileToByteArray(cache.get(ImageSize.L)) :
                    downloadImage(String.format(imgTemplate, imgId, ImageSize.L));
            imgSupport.setLargeImage(image);
        }
        else {
            log.info("skipping binary image[{}]-{}; already exists", imgId, ImageSize.L);
        }
    }

    private byte[] downloadImage(String imgUrl) throws IOException {

        URL remoteImg = new URL(imgUrl);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096); // 1000000/2
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try(
                ReadableByteChannel inChannel = Channels.newChannel(remoteImg.openStream());
                WritableByteChannel outChannel = Channels.newChannel(baos);
        ) {
            int read;
            while((read = inChannel.read(byteBuffer)) > 0) {
                byteBuffer.rewind();
                byteBuffer.limit(read);
                while(read > 0) {
                    read = outChannel.write(byteBuffer);
                }
                byteBuffer.clear();
            }

            // only 100 requests/IP are allowed for every 5 minutes
            // see RATE LIMITING: https://openlibrary.org/dev/docs/api/covers
            //
            // 5 min * 60 secs = 300s / 100 requests = 3 sec per request
            //
            // randomize sleep value within 1 second of a defined throttle value
            long sleep = RandomUtils.nextLong(AUTHOR_IMG_THROTTLE_MS-500, AUTHOR_IMG_THROTTLE_MS+500);

            log.info("OK! {} | sleeping {}ms", imgUrl, sleep);
            Thread.sleep(sleep); // throttle to ensure no more than 100 requests per 5 min

        } catch (InterruptedException e) {
            log.error("invalid url[{}]: {}", imgUrl, e.getMessage());
        }

        return baos.toByteArray();
    }

    private boolean isAuthor(Class schema) {
        return AuthorSchema.class.equals(schema);
    }

    private boolean isWork(Class schema) {
        return WorkSchema.class.equals(schema);
    }

    private boolean isEdition(Class schema) {
        return EditionSchema.class.equals(schema);
    }
}
