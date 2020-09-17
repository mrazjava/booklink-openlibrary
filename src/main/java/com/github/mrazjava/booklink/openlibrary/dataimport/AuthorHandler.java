package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.OpenLibraryIntegrationException;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.mrazjava.booklink.openlibrary.dataimport.ImageSize.*;

@Slf4j
@Component
public class AuthorHandler extends AbstractImportHandler<AuthorSchema> {

    @Autowired
    private AuthorRepository repository;

    @Override
    public void prepare(File workingDirectory) {

        super.prepare(workingDirectory);

        if(StringUtils.isNotBlank(imageDir)) {
            imageDirectoryLocation = Path.of(imageDir).getParent() == null ?
                    Path.of(workingDirectory.getAbsolutePath() + File.separator + imageDir).toFile() :
                    Path.of(imageDir).toFile();
            if(BooleanUtils.isTrue(imagePull) && !imageDirectoryLocation.exists()) {
                throw new OpenLibraryIntegrationException(
                        String.format("booklink.di.image-dir does not exist! %s", imageDirectoryLocation.getAbsolutePath())
                );
            }
        }

        log.info("destinationAuthorImg: {}", imageDirectoryLocation);
    }

    @Override
    public void handle(AuthorSchema record, long sequenceNo) {

        if(record == null) return;

        if((sequenceNo % frequencyCheck) == 0 && authorIdFilter.isEnabled()) {
            log.info("MATCHED-FILTERS...{}: {}, SAVED: {}",
                    authorIdFilter.getFilterName(), authorMatchCount,
                    savedCount);
            savedCount = authorMatchCount = 0;
        }

        if(authorIdFilter.isEnabled()) {
            if (authorIdFilter.exists(record.getId())) {
                log.trace("FILTER: author # {} matched author ID[{}]:\n{}", sequenceNo, record.getId(), toText(record));
                authorMatchCount++;
            } else {
                return;
            }
        }

        AuthorSchema author = null;

        if(persistData) {
            AuthorSchema saved = null;

            if (!persistDataOverride || BooleanUtils.isTrue(storeImagesInMongo)) {
                saved = repository.findById(record.getId()).orElse(null);
                if (!persistDataOverride && saved != null) {
                    return;
                }
            }

            if (persistDataOverride) {
                author = record;
                if (saved != null) {
                    author.setImageSmall(saved.getImageSmall());
                    author.setImageMedium(saved.getImageMedium());
                    author.setImageLarge(saved.getImageLarge());
                }
            }

            author = Optional.ofNullable(saved).orElse(record);
        }
        else {
            author = record;
        }

        if(BooleanUtils.isTrue(imagePull)) {
            try {
                downloadImages(author, sequenceNo);
            } catch (IOException e) {
                throw new OpenLibraryIntegrationException("problem downloading author images", e);
            }
        }

        if(persistData) {
            repository.save(author);
            savedCount++;
        }
    }

    @Override
    protected Class<AuthorSchema> getSchemaType() {
        return AuthorSchema.class;
    }

    private void downloadImages(AuthorSchema record, long sequenceNo) throws IOException {

        if(CollectionUtils.isEmpty(record.getPhotos())) {
            return;
        }
        AtomicLong lastId = new AtomicLong(0L);
        record.getPhotos().stream()
                .filter(id -> id > 0)
                .map(Long::valueOf)
                .filter(id -> {
                    log.info("author[{}] img[{}]", record.getId(), id);
                    if(lastId.get() > 0) {
                        log.info(".... trying alternate photoId[{}] (author={}, sequenceNo={})",
                                id, record.getId(), sequenceNo);
                    }
                    boolean success = downloadImages(record, id, sequenceNo);
                    if(!success) {
                        lastId.set(id);
                    }
                    return success;
                })
                .findFirst();
    }

    /**
     * @return {@code true} if operation succeeded; {@code false} otherwise
     */
    private boolean downloadImages(AuthorSchema record, long photoId, long sequenceNo) {

        boolean downloadToFile = StringUtils.isNotBlank(imageDir);
        boolean downloadToBinary = BooleanUtils.isTrue(storeImagesInMongo);

        if(log.isDebugEnabled()) {
            if ((downloadToFile || downloadToBinary) && !imageDownloader.filesExist(
                    imageDirectoryLocation.getAbsolutePath(),
                    photoId,
                    List.of(S, M, L)
            )) {
                log.debug("author #{} [{}]; checking images ...", sequenceNo, record.getId());
            }
        }

        Map<ImageSize, byte[]> images = Map.of();

        try {
            if (downloadToFile) {
                images = imageDownloader.downloadImageFiles(
                        imageDirectoryLocation.getAbsolutePath(),
                        photoId, urlProvider.getAuthorIdUrlTemplate());
            }
            if (downloadToBinary) {
                imageDownloader.downloadImageToBinary(
                        photoId, urlProvider.getAuthorIdUrlTemplate(), record, images
                );
            }
        }
        catch(IOException e) {
            log.error("download error! authorId[{}], photoId[{}], sequenceNo[{}]: {}",
                    record.getId(), photoId, sequenceNo, e.getMessage());
        }

        return (images.containsKey(S) || record.getImageSmall() != null) &&
                (images.containsKey(M) || record.getImageMedium() != null) &&
                (images.containsKey(L) || record.getImageLarge() != null);
    }
}