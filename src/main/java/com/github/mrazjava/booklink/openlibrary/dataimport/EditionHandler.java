package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.BooklinkUtils;
import com.github.mrazjava.booklink.openlibrary.OpenLibraryIntegrationException;
import com.github.mrazjava.booklink.openlibrary.repository.EditionRepository;
import com.github.mrazjava.booklink.openlibrary.schema.DefaultImageSupport;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

        if(sequenceNo % frequencyCheck == 0) {
            log.info("FILTER MATCHES -- {}: {}, {}: {}, SAVED: {}",
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

        if(BooleanUtils.isTrue(downloadImages)) {
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

        Long coverId = Optional.ofNullable(record.getCovers()).orElse(List.of()).stream().findFirst().orElse(0L);

        if(coverId == 0) {
            return;
        }

        if(BooleanUtils.isTrue(storeImagesInMongo)) {
            // pull images from cover TARs downloaded manually in bulk
            Set<ImageSize> failedSizes = imageDownloader.fetchImageToBinary(
                    coverId, record, new File(getCoverDownloadPath())
            );
            // not all covers exist in a bulk archive; those that failed, try to download directly
            failedSizes.stream().forEach(size -> downloadMissingCoverAndSet(coverId, size, record));
        }
    }

    private void downloadMissingCoverAndSet(Long coverId, ImageSize size, DefaultImageSupport imageSupport) {

        if(size == ImageSize.O && BooleanUtils.isFalse(fetchOriginalImages)) {
            return;
        }

        try {
            byte[] imageBytes = imageDownloader.downloadImageToFile(
                    getCoverDownloadPath(),
                    coverId,
                    size,
                    urlProvider.getBookIdUrlTemplate()
            );

            if(imageBytes != null) {
                imageSupport.setImage(
                        BooklinkUtils.buildImage(Long.toString(coverId), imageBytes),
                        size
                );
            }

        } catch (IOException e) {
            log.warn("cover [{}] download error: {}", e.getMessage());
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
