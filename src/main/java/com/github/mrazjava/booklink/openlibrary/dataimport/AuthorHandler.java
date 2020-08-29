package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema.AUTHOR_PHOTOID_IMG_URL_TEMPLATE;

@Slf4j
@Component
public class AuthorHandler extends AbstractImportHandler<AuthorSchema> {

    @Autowired
    private AuthorRepository repository;

    @Autowired
    private ImageDownloader imageDownloader;

    private File authorImagesDestination;

    @Value("${booklink.di.author-image-dir}")
    private String authorImgDir;

    @Value("${booklink.di.author-image-mongo}")
    private Boolean storeAuthorImgInMongo;

    /**
     * Optional file; if exists, only authors in that list will be persisted.
     * One ID per line. Comments are allowed and start with a #. Empty lines
     * are allowed and ignored.
     */
    public static final String FILENAME_ALLOWED_IDS = "author-ids.txt";

    private Set<String> allowedIds = new HashSet<>();


    @Override
    public void prepare(File workingDirectory) {

        if(StringUtils.isNotBlank(authorImgDir)) {
            authorImagesDestination = Path.of(authorImgDir).getParent() == null ?
                    Path.of(workingDirectory.getAbsolutePath() + File.separator + authorImgDir).toFile() :
                    Path.of(authorImgDir).toFile();
            if(!authorImagesDestination.exists()) {
                authorImagesDestination.mkdir();
            }
        }

        File allowedIdsFile = new File(workingDirectory.getAbsolutePath() + File.separator + FILENAME_ALLOWED_IDS);
        if(allowedIdsFile.exists()) {
            log.info("detected author IDs file: {}", allowedIdsFile.getAbsoluteFile());
            try {
                LineIterator iterator = FileUtils.lineIterator(allowedIdsFile, "UTF-8");
                while(iterator.hasNext()) {
                    String line = iterator.next();
                    if(StringUtils.isNotBlank(line) && !line.startsWith("#")) {
                        allowedIds.add(line);
                    }
                }
                log.info("loaded {} allowed IDs:\n{}", allowedIds.size(), allowedIds);
            } catch (IOException e) {
                log.error("problem loading author id file: {}", e.getMessage());
                allowedIds.clear();
            }
        }

        log.info("destinationAuthorImg: {}", authorImagesDestination);
    }

    @Override
    public void handle(AuthorSchema record, long sequenceNo) {

        if(record == null || !persistData) {
            return;
        }

        if(!allowedIds.isEmpty()) {
            if (allowedIds.contains(record.getId())) {
                log.info("ALLOWED ID AUTHOR:\n{}", toText(record));
            } else {
                return;
            }

        }

        AuthorSchema saved = null;

        if(!persistDataOverride || BooleanUtils.isTrue(storeAuthorImgInMongo)) {
            saved = repository.findById(record.getId()).orElse(null);
            if(!persistDataOverride && saved != null) {
                return;
            }
        }

        AuthorSchema author = null;

        if(persistDataOverride) {
            author = record;
            if(saved != null) {
                author.setImageSmall(saved.getImageSmall());
                author.setImageMedium(saved.getImageMedium());
                author.setImageLarge(saved.getImageLarge());
            }
        }
        else {
            author = Optional.ofNullable(saved).orElse(record);
        }

        try {
            downloadImages(author, sequenceNo);
        }
        catch(IOException e) {
            log.error("problem downloading author images: {}", e.getMessage());
        }

        repository.save(author);
    }

    @Override
    protected Class<AuthorSchema> getSchemaType() {
        return AuthorSchema.class;
    }

    private void downloadImages(AuthorSchema record, long sequenceNo) throws IOException {

        if(CollectionUtils.isEmpty(record.getPhotos())) {
            return;
        }

        Integer photoId = record.getPhotos().stream().filter(id -> id > 0).findFirst().orElse(0);

        if(photoId == 0) {
            return;
        }

        boolean downloadToFile = StringUtils.isNotBlank(authorImgDir);
        boolean downloadToBinary = BooleanUtils.isTrue(storeAuthorImgInMongo);

        if(downloadToFile || downloadToBinary) {
            log.info("author #{} [{}]; fetching images ...", sequenceNo, record.getId());
        }

        Map<ImageSize, File> imgFiles = downloadToFile ?
                imageDownloader.downloadImageToFile(
                        authorImagesDestination.getAbsolutePath(),
                        String.valueOf(photoId), AUTHOR_PHOTOID_IMG_URL_TEMPLATE) :
                null;

        if(downloadToBinary) {
            imageDownloader.downloadImageToBinary(
                    String.valueOf(photoId), AUTHOR_PHOTOID_IMG_URL_TEMPLATE,
                    record,
                    imgFiles == null ? Map.of() : imgFiles
            );
        }
    }
}
