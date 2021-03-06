package com.github.mrazjava.booklink.openlibrary.dataimport;

import static com.github.mrazjava.booklink.openlibrary.BooklinkUtils.extractSampleText;
import static com.github.mrazjava.booklink.openlibrary.dataimport.ImageSize.L;
import static com.github.mrazjava.booklink.openlibrary.dataimport.ImageSize.M;
import static com.github.mrazjava.booklink.openlibrary.dataimport.ImageSize.S;
import static java.util.Optional.ofNullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.github.mrazjava.booklink.openlibrary.OpenLibraryImportApp;
import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthorHandler extends AbstractImportHandler<AuthorSchema> {

    @Autowired
    private AuthorRepository repository;

    private Set<Long> indexesToSample;

    @Value("${booklink.di.author-sample-output-file}")
    private String authorSampleFile;

    @Value("${booklink.di.frequency-check}")
    private int frequencyCheck;

    @Autowired
    private Optional<AuthorSampleRandomizer> sampleRandomizer;
    
    @Autowired
    private SampleAuthorTracker sampleAuthorTracker;

    @Override
    public void prepare(File workingDirectory) {

    	authorIdFilter.setIdTransformer(s -> s.startsWith("SA:") ? s.substring(3) : s);
    	
        super.prepare(workingDirectory);

        indexesToSample = sampleRandomizer.map(r -> r.randomize()).orElse(Set.of());

        if(StringUtils.isNotBlank(imageDir)) {
            imageDirectoryLocation = Path.of(imageDir).getParent() == null ?
                    Path.of(workingDirectory.getAbsolutePath() + File.separator + imageDir).toFile() :
                    Path.of(imageDir).toFile();
            if(BooleanUtils.isTrue(imagePull) && !imageDirectoryLocation.exists()) {
                throw new OpenLibraryImportException(
                        String.format("booklink.di.image-dir does not exist! %s", imageDirectoryLocation.getAbsolutePath())
                );
            }
        }

        if(log.isDebugEnabled()) {
	        log.debug("[AUTHOR IMG DOWNLOAD DIR] - {}", Optional.ofNullable(imageDirectoryLocation).map(
	                f -> f.getAbsolutePath()
	            ).orElse("DISABLED")
	        );
        }
    }

    @Override
    public void handle(AuthorSchema record, long sequenceNo) {

        if(record == null) return;

        if((sequenceNo % frequencyCheck) == 0) {
            totalSavedCount += savedCount;
            if (authorIdFilter.isEnabled()) {
                log.info("FILTER MATCHES -- BY-{}: {}, SAVED: {}({})",
                        authorIdFilter.getFilterName(), authorMatchCount, savedCount, totalSavedCount);
                authorMatchCount = 0;
            }
            else if(persistData) {
                log.info("SAVED: {}({})", savedCount, totalSavedCount);
            }
            savedCount = 0;
        }

        if(isAuthorIdSampleEnabled()) {
            
            if((indexesToSample.isEmpty() && (sequenceNo % frequencyCheck == 0)) ||
                (!indexesToSample.isEmpty() && indexesToSample.contains(sequenceNo))) {
            
                sampleAuthorTracker.addSampleId(new SampleAuthorIdRecord(
                        record.getId(),
                        StringUtils.firstNonBlank(record.getName(), record.getFullName(), record.getPersonalName()),
                        sequenceNo
                   )
                   .withImage(!CollectionUtils.isEmpty(record.getPhotos()))
                );
            }
            else if(sampleAuthorTracker.sampleIdCount() > sampleAuthorTracker.sampleIdWithImageCount()) {
                if(!CollectionUtils.isEmpty(record.getPhotos())) {
                    sampleAuthorTracker.addSampleIdWithImage(new SampleAuthorIdRecord(
                        record.getId(),
                        StringUtils.firstNonBlank(record.getName(), record.getFullName(), record.getPersonalName()),
                        sequenceNo
                   )
                   .withImage(true)
                   );
                }
            }
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

            if (!persistDataOverride || BooleanUtils.isTrue(withMongoImages)) {
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
                checkImages(author);
            } catch (IOException e) {
                throw new OpenLibraryImportException("problem downloading author images", e);
            }
        }

        if(persistData) {
        	enhanceData(author);
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
                    log.debug("author[{}] img[{}]", record.getId(), id);
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
        boolean downloadToBinary = BooleanUtils.isTrue(withMongoImages);

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

    @Override
    public void conclude(File dataSource) {
        super.conclude(dataSource);
        if(isAuthorIdSampleEnabled()) {
            recordSampleAuthorIds(dataSource);
        }
    }

    private boolean isAuthorIdSampleEnabled() {
        return StringUtils.isNotBlank(authorSampleFile);
    }

    @Override
	protected void enhanceData(AuthorSchema record) {

    	ofNullable(record.getBio()).ifPresent(b -> record.setBioSample(extractSampleText(b)));
	}

	private void recordSampleAuthorIds(File dataSource) {

	    List<SampleAuthorIdRecord> sampleAuthors = sampleAuthorTracker.buildSample(20);
	    
        if(log.isInfoEnabled()) {
            log.info("{} sample author IDs:\n{}",
                    sampleAuthors.size(),
                    StringUtils.join(sampleAuthors.stream().map(SampleAuthorIdRecord::getId).collect(Collectors.toList()), ",")
            );

        }
        try {
            File sampleAuthorIds = OpenLibraryImportApp.openFile(dataSource.getParent(), authorSampleFile);
            log.info("saving random samples to: {}", sampleAuthorIds.getAbsolutePath());
            FileUtils.writeStringToFile(
                    sampleAuthorIds,
                    StringUtils.joinWith("\n", sampleAuthors.toArray()),
                    Charset.defaultCharset()
            );
        } catch (IOException e) {
            log.error("problem saving sample author IDs", e);
        }
    }

    static class SampleAuthorIdRecord implements Comparable<SampleAuthorIdRecord> {

        private String id;
        private String name;
        private long sequenceNo;
        private boolean image;

        SampleAuthorIdRecord(String id, String name, long sequenceNo) {
            this.id = id;
            this.name = name;
            this.sequenceNo = sequenceNo;
        }

        String getId() {
            return id;
        }
        
        SampleAuthorIdRecord withImage(boolean image) {
            this.image = image;
            return this;
        }
        
        boolean hasImage() {
            return image;
        }

        @Override
        public int compareTo(SampleAuthorIdRecord that) {            
            return (int)(this.sequenceNo - that.sequenceNo);
        }

        @Override
        public String toString() {
            StringBuilder record = new StringBuilder();
            record.append("# " + sequenceNo + ": " + name + 
                    String.format(" (IMG ? %s)", BooleanUtils.toStringYesNo(image))
            );
            record.append("\n");
            record.append(id);
            return record.toString();
        }
    }
}