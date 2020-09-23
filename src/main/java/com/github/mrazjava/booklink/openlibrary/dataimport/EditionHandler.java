package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.BooklinkUtils;
import com.github.mrazjava.booklink.openlibrary.OpenLibraryIntegrationException;
import com.github.mrazjava.booklink.openlibrary.repository.EditionRepository;
import com.github.mrazjava.booklink.openlibrary.schema.DefaultImageSupport;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.github.mrazjava.booklink.openlibrary.dataimport.ImageSize.*;

@Slf4j
@Component
public class EditionHandler extends AbstractImportHandler<EditionSchema> {

    @Autowired
    private EditionRepository repository;

    @Autowired
    private WorkIdFilter workIdFilter;

    private int workMatchCount = 0;


    @Override
    public void prepare(File workingDirectory) {

        super.prepare(workingDirectory);
        workIdFilter.load(workingDirectory);
        imageDownloader.setThrottleMs(1000);
    }

    @Override
    public void handle(EditionSchema record, long sequenceNo) {

        cleanBadData(record);

        if((authorIdFilter.isEnabled() || workIdFilter.isEnabled()) && sequenceNo % frequencyCheck == 0) {
            log.info("FILTER MATCHES -- BY-{}: {}, BY-{}: {}, SAVED: {}",
                    authorIdFilter.getFilterName(), authorMatchCount,
                    workIdFilter.getFilterName(), workMatchCount,
                    savedCount);
            savedCount = authorMatchCount = workMatchCount = 0;
        }

        String matchedId = runAuthorIdFilter(record, authorIdFilter, sequenceNo);
        if(StringUtils.isBlank(matchedId)) {
            matchedId = runAuthorIdFilter(record, workIdFilter, sequenceNo);
            if(StringUtils.isBlank(matchedId)) {
                return;
            }
            else {
                workMatchCount++;
            }
        }
        else {
            authorMatchCount++;
        }

        if(BooleanUtils.isTrue(imagePull)) {
            downloadImages(record, sequenceNo);
        }

        if(persistData) {
            if(!persistDataOverride) {
                if(repository.findById(record.getId()).isPresent()) {
                    return;
                }
            }
            repository.save(record);
            savedCount++;
        }
    }

    private void downloadImages(EditionSchema record, long sequenceNo) {

        if(CollectionUtils.isEmpty(record.getCovers())) {
            return;
        }

        AtomicLong lastId = new AtomicLong(0L);
        record.getCovers().stream()
                .filter(id -> id > 0)
                .filter(id -> {
                    log.info("edition[{}] img[{}]", record.getId(), id);
                    if(lastId.get() > 0) {
                        log.info(".... trying alternate coverId[{}] (edition={}, sequenceNo={})",
                                id, record.getId(), sequenceNo);
                    }
                    boolean success = loadCovers(record, id);
                    if(!success) {
                        lastId.set(id);
                    }
                    return success;
                })
                .findFirst();
    }

    /**
     * Attempts to fetch book cover images of all sizes for a specific edition and a cover. Depending
     * on configuration, may attempt a download from internet (openlibrary) if cover is missing in a
     * bulk TAR archive. Also depending on configuration, may set fetched cover images as part of a
     * mongo record.
     *
     * @return {@code true} if cover images were loaded successfully using whichever means
     */
    private boolean loadCovers(EditionSchema record, Long coverId) {

        // pull images from cover TARs downloaded manually in bulk
        Set<ImageSize> fetchStatus = imageDownloader.fetchImageToBinary(
                coverId, record, new File(getCoverDownloadPath())
        );

        Set<ImageSize> downloadStatus = new HashSet<>();

        // not all covers exist in a bulk archive; those that did not succeed, try to download directly
        Arrays.stream(ImageSize.values())
                .filter(size -> !fetchStatus.contains(size) && !ImageSize.O.equals(size))
                .forEach(size -> {
            if(downloadMissingCoverAndSet(coverId, size, record)) {
                downloadStatus.add(size);
            }
        });

        return SetUtils.union(fetchStatus, downloadStatus).containsAll(Set.of(S, M, L));
    }

    /**
     * @return {@code true} if image was successfully downloaded
     */
    private boolean downloadMissingCoverAndSet(Long coverId, ImageSize size, DefaultImageSupport imageSupport) {

        if(size == ImageSize.O && BooleanUtils.isFalse(fetchOriginalImages)) {
            return true;
        }

        try {
            byte[] imageBytes = imageDownloader.downloadImageToFile(
                    getCoverDownloadPath(),
                    coverId,
                    size,
                    urlProvider.getBookIdUrlTemplate()
            );

            boolean status = (imageBytes != null) && imageBytes.length > ImageDownloader.MINIMUM_VALID_IMAGE_BYTE_SIZE;

            if(status) {
                if(BooleanUtils.isTrue(storeImagesInMongo)) {
                    imageSupport.setImage(
                            BooklinkUtils.buildImage(Long.toString(coverId), imageBytes),
                            size
                    );
                }
            }

            return status;

        } catch (IOException e) {
            log.warn("cover [{}] download error: {}", e.getMessage());
            return false;
        }
    }

    private String getCoverDownloadPath() {
        return imageDir + File.separator + "editions";
    }

    /**
     * Sanitizes data known to be bad in the raw reed, so that it is not imported.
     *
     * @param record to clean
     */
    private void cleanBadData(EditionSchema record) {

        if(CollectionUtils.isEmpty(record.getCovers())) {
            record.setCovers(null);
        }
        else {
            List<Long> coverIds = record.getCovers().stream()
                    .filter(id -> id != null && id > 0).collect(Collectors.toList());
            record.setCovers(coverIds.isEmpty() ? null : coverIds);
        }
    }

    private String runAuthorIdFilter(EditionSchema record, AbstractIdFilter filter, long sequenceNo) {

        String matchedId = null;
        Set<String> ids = AuthorIdFilter.FILTER_NAME.equals(filter.getFilterName()) ?
                record.getAuthors() : record.getWorks();

        if(filter.isEnabled() && !CollectionUtils.isEmpty(ids)) {
            matchedId = ids.stream()
                    .filter(id -> filter.exists(id))
                    .findFirst()
                    .orElse(null);
        }

        if(matchedId != null) {
            if(log.isDebugEnabled()) {
                log.debug("[{} FILTER] edition # {} matched ID[{}]\n{}",
                        filter.getFilterName(), sequenceNo, matchedId, toText(record));
            }
        }

        return matchedId;
    }

    @Override
    protected Class<EditionSchema> getSchemaType() {
        return EditionSchema.class;
    }
}
