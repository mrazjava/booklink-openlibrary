package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema.AUTHOR_PHOTOID_IMG_URL_TEMPLATE;

@Slf4j
@Component
public class AuthorHandler extends AbstractImportHandler<AuthorSchema> {

    @Autowired
    private AuthorRepository repository;

    @Autowired
    private ImageDownloader imageDownloader;

    private File authorImagesDestination;

    @Autowired
    private AuthorIdFilter authorIdFilter;

    private int authorMatchCount = 0;

    @Override
    public void prepare(File workingDirectory) {

        if(StringUtils.isNotBlank(imageDir)) {
            authorImagesDestination = Path.of(imageDir).getParent() == null ?
                    Path.of(workingDirectory.getAbsolutePath() + File.separator + imageDir).toFile() :
                    Path.of(imageDir).toFile();
            if(!authorImagesDestination.exists()) {
                authorImagesDestination.mkdir();
            }
        }

        authorIdFilter.load(workingDirectory);

        log.info("destinationAuthorImg: {}", authorImagesDestination);
    }

    @Override
    public void handle(AuthorSchema record, long sequenceNo) {

        if((sequenceNo % frequencyCheck) == 0) {
            log.info("FILTER MATCHES -- {}: {}, SAVED: {}",
                    authorIdFilter.getFilterName(), authorMatchCount,
                    savedCount);
            savedCount = authorMatchCount = 0;
        }

        if(authorIdFilter.isEnabled()) {
            if (authorIdFilter.exists(record.getId())) {
                log.debug("FILTER: author # {} matched author ID[{}]:\n{}", sequenceNo, record.getId(), toText(record));
                authorMatchCount++;
            } else {
                return;
            }
        }

        if(record == null || !persistData) {
            return;
        }

        AuthorSchema saved = null;

        if(!persistDataOverride || BooleanUtils.isTrue(storeImagesInMongo)) {
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

        if(BooleanUtils.isTrue(downloadImages)) {
            try {
                downloadImages(author, sequenceNo);
            } catch (IOException e) {
                log.error("problem downloading author images: {}", e.getMessage());
            }
        }

        repository.save(author);
        savedCount++;
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

        boolean downloadToFile = StringUtils.isNotBlank(imageDir);
        boolean downloadToBinary = BooleanUtils.isTrue(storeImagesInMongo);

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
