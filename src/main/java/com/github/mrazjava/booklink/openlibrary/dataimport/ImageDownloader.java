package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.schema.CoverImage;
import com.github.mrazjava.booklink.openlibrary.schema.DefaultImageSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.mrazjava.booklink.openlibrary.dataimport.ImageSize.*;

@Slf4j
@Component
public class ImageDownloader {

    /**
     * When downloading images individually from Openlibrary.org, slow down the
     * frequency of image requests to avoid blokade from openlibrary which limits
     * the number of requests allowed per a specific time period.
     *
     * Number of milliseconds to wait after downloading an image.
     */
    public static final long OPENLIB_IMG_THROTTLE_MS = 2000;

    static final String MSG_EXISTS = "exists";

    static final String MSG_BLOCKED = "blocked";

    static final String MSG_DOWNLOADED = "downloaded";

    static final String MSG_FAILURE = "failed";

    @Value("${booklink.di.image-download}")
    private Boolean downloadImages;

    @Value("${booklink.di.fetch-original-images}")
    private Boolean fetchOriginalImages;

    private File imageDirectory;

    private Set<String> failedImageDownloads = new HashSet<>();

    private final int MINIMUM_VALID_IMAGE_BYTE_SIZE = 850;

    private IdFilter idFilter;


    public ImageDownloader(@Value("${booklink.di.image-dir}") String imgDir) {
        if(StringUtils.isNotBlank(imgDir)) {
            imageDirectory = new File(imgDir);
        }
    }

    public void setIdFilter(IdFilter idFilter) {
        this.idFilter = idFilter;
    }

    public Map<ImageSize, File> downloadImageToFile(String destinationDir, String imgId, String imgTemplate) throws IOException {

        Map<ImageSize, File> files = new HashMap<>();

        if(BooleanUtils.isFalse(downloadImages)) {
            return files;
        }

        for(ImageSize imgSize : ImageSize.values()) {

            if(imgSize == O && BooleanUtils.isFalse(fetchOriginalImages)) continue;

            File imgById = getImageFile(destinationDir, imgSize, imgId);

            if(imgById.exists()) {
                log.debug("file exists, skipping: {}", imgById);
                continue;
            }
            if(idFilter.exists(imgId) || idFilter.exists(FilenameUtils.getBaseName(imgById.getAbsolutePath()))) {
                log.info("filter matched; ignoring! {}", imgById.getName());
                continue;
            }

            String imgUrl = String.format(imgTemplate, imgId, imgSize);

            log.info("downloading.... {}", imgUrl);

            byte[] imageBytes = downloadImage(imgUrl);
            if(imageBytes != null && imageBytes.length > MINIMUM_VALID_IMAGE_BYTE_SIZE) {
                FileUtils.copyToFile(new ByteArrayInputStream(imageBytes), imgById);
                files.put(imgSize, imgById);
            }
        }

        return files;
    }

    private File getImageFile(String destinationDir, ImageSize imgSize, String imgId) {
        return new File(
                destinationDir +
                        File.separator +
                        (imgSize == O ? String.format("%s.jpg", imgId) : String.format("%s-%s.jpg", imgId, imgSize))
        );
    }

    public boolean filesExist(String destinationDir, String imgId, List<ImageSize> sizes) {

        AtomicBoolean allExist = new AtomicBoolean(true);

        Optional.of(sizes).orElse(List.of(ImageSize.values()))
                .stream()
                .distinct()
                .forEach(size -> {
                    boolean exists = getImageFile(destinationDir, size, imgId).exists();
                    allExist.set(allExist.get() && exists);
                    if(!exists) return;
                });

        return allExist.get();
    }

    public Set<ImageSize> downloadImageToBinary(
            String imgId, String imgTemplateUrl, DefaultImageSupport imgSupport, Map<ImageSize, File> cache) throws IOException {

        Set<ImageSize> downloadStatus = new HashSet<>();

        if(BooleanUtils.isFalse(downloadImages)) {
            return downloadStatus;
        }

        boolean smallExistedB4 = imgSupport.hasImage(S);

        if(!smallExistedB4) {
            ImageSize size = S;
            byte[] image = isImageInCache(cache, size) ?
                    FileUtils.readFileToByteArray(cache.get(size)) :
                    downloadImage(String.format(imgTemplateUrl, imgId, size));
            imgSupport.setImage(buildImage(imgId, image), size);
            downloadStatus.add(size);
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, S);
        }

        boolean mediumExistedB4 = imgSupport.hasImage(M);

        if(!mediumExistedB4) {
            ImageSize size = M;
            byte[] image = isImageInCache(cache, size) ?
                    FileUtils.readFileToByteArray(cache.get(size)) :
                    downloadImage(String.format(imgTemplateUrl, imgId, size));
            imgSupport.setImage(buildImage(imgId, image), size);
            downloadStatus.add(size);
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, M);
        }

        boolean largeExistedB4 = imgSupport.hasImage(ImageSize.L);

        if(!largeExistedB4) {
            ImageSize size = ImageSize.L;
            byte[] image = isImageInCache(cache, size) ?
                    FileUtils.readFileToByteArray(cache.get(size)) :
                    downloadImage(String.format(imgTemplateUrl, imgId, size));
            imgSupport.setImage(buildImage(imgId, image), size);
            downloadStatus.add(size);
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, ImageSize.L);
        }

        boolean originalExistedB4 = imgSupport.hasImage(O);

        if(BooleanUtils.isTrue(fetchOriginalImages)) {

            if (!originalExistedB4) {
                ImageSize size = O;
                byte[] image = isImageInCache(cache, size) ?
                        FileUtils.readFileToByteArray(cache.get(size)) :
                        downloadImage(String.format(imgTemplateUrl, imgId, size));
                imgSupport.setImage(buildImage(imgId, image), size);
                downloadStatus.add(size);
            } else {
                log.debug("skipping binary image[{}]-{}; already exists", imgId, O);
            }
        }

        if(log.isInfoEnabled()) {
            log.info("--- binary ? S({}) M({}) L({}) O({})",
                    smallExistedB4 ? MSG_EXISTS : (imgSupport.hasImage(S) ? MSG_DOWNLOADED : MSG_FAILURE),
                    mediumExistedB4 ? MSG_EXISTS : (imgSupport.hasImage(M) ? MSG_DOWNLOADED : MSG_FAILURE),
                    largeExistedB4 ? MSG_EXISTS : (imgSupport.hasImage(L) ? MSG_DOWNLOADED : MSG_FAILURE),
                    originalExistedB4 ? MSG_EXISTS : (fetchOriginalImages ? (imgSupport.hasImage(O) ? MSG_DOWNLOADED : MSG_FAILURE) : MSG_BLOCKED)
            );
        }

        return downloadStatus;
    }

    /**
     * @return image sizes which were downloaded successfully; never null but set can be empty if no download
     *  succeeded
     */
    public Set<ImageSize> downloadImageToBinary(Long imgId, DefaultImageSupport imgSupport) throws Exception {

        Set<ImageSize> downloadStatus = new HashSet<>();

        if(BooleanUtils.isFalse(downloadImages)) {
            return downloadStatus;
        }

        boolean smallExistedB4 = imgSupport.hasImage(S);

        if(!smallExistedB4) {
            ImageSize size = S;
            byte[] imageBytes = downloadImage(imgId, size, imageDirectory);
            if(imageBytes != null) {
                imgSupport.setImage(buildImage(Long.toString(imgId), imageBytes), size);
                downloadStatus.add(size);
            }
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, S);
        }

        boolean mediumExistedB4 = imgSupport.hasImage(M);

        if(!mediumExistedB4) {
            ImageSize size = M;
            byte[] imageBytes = downloadImage(imgId, size, imageDirectory);
            if(imageBytes != null) {
                imgSupport.setImage(buildImage(Long.toString(imgId), imageBytes), size);
                downloadStatus.add(size);
            }
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, M);
        }

        boolean largeExistedB4 = imgSupport.hasImage(ImageSize.L);

        if(!largeExistedB4) {
            ImageSize size = ImageSize.L;
            byte[] imageBytes = downloadImage(imgId, size, imageDirectory);
            if(imageBytes != null) {
                imgSupport.setImage(buildImage(Long.toString(imgId), imageBytes), size);
                downloadStatus.add(size);
            }
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, ImageSize.L);
        }

        boolean originalExistedB4 = imgSupport.hasImage(O);

        if(BooleanUtils.isTrue(fetchOriginalImages)) {

            if (!originalExistedB4) {
                ImageSize size = O;
                byte[] imageBytes = downloadImage(imgId, size, imageDirectory);
                if(imageBytes != null) {
                    imgSupport.setImage(buildImage(Long.toString(imgId), imageBytes), size);
                    downloadStatus.add(size);
                }
            } else {
                log.debug("skipping binary image[{}]-{}; already exists", imgId, O);
            }
        }

        if(log.isInfoEnabled()) {
            log.info("--- binary ? S({}) M({}) L({}) O({})",
                    smallExistedB4 ? MSG_EXISTS : (imgSupport.hasImage(S) ? MSG_DOWNLOADED : MSG_FAILURE),
                    mediumExistedB4 ? MSG_EXISTS : (imgSupport.hasImage(M) ? MSG_DOWNLOADED : MSG_FAILURE),
                    largeExistedB4 ? MSG_EXISTS : (imgSupport.hasImage(L) ? MSG_DOWNLOADED : MSG_FAILURE),
                    originalExistedB4 ? MSG_EXISTS : (fetchOriginalImages ? (imgSupport.hasImage(O) ? MSG_DOWNLOADED : MSG_FAILURE) : MSG_BLOCKED)
            );
        }

        return downloadStatus;
    }

    private boolean isImageInCache(Map<ImageSize, File> cache, ImageSize size) {
        return cache != null && cache.containsKey(size);
    }

    private CoverImage buildImage(String id, byte[] image) {
        return CoverImage.builder()
                .id(id)
                .image(new Binary(BsonBinarySubType.BINARY, image))
                .sizeBytes(image.length)
                .sizeText(FileUtils.byteCountToDisplaySize(image.length))
                .build();
    }

    public byte[] downloadImage(Long imgId, ImageSize size, File imageDir) {

        if(imageDir == null) {
            return null;
        }

        if(imgId < 100000) {
            log.warn("wrong image id [{}]", imgId);
            return new byte[]{};
        }

        String imgIdStr = StringUtils.leftPad(Long.toString(imgId), 7, "0");
        byte[] imageBytes = null;

        char idx1 = imgIdStr.charAt(0);
        String idx2 = imgIdStr.substring(1,3);

        String tarFileName = size.name().toLowerCase() + "_covers_000" + idx1 + "_" + idx2 + ".tar";
        String tarFullPath = imageDir.getPath() + File.separator + tarFileName.substring(0, 13) + File.separator + tarFileName;

        log.debug("tar source: {}", tarFullPath);

        try(TarArchiveInputStream tais = new TarArchiveInputStream(new FileInputStream(tarFullPath))) {

            while(tais.getNextEntry() != null) {
                TarArchiveEntry entry = tais.getCurrentEntry();
                String imgFileName = "000" + imgIdStr + "-" + size.name() + ".jpg";
                if(imgFileName.equals(entry.getName())) {
                    log.info("{} - {} | {}", entry.getName(), entry.getSize(), entry.getLastModifiedDate());
                    imageBytes = tais.readNBytes((int)entry.getSize());
                    break;
                }
            }
        }
        catch(Exception e) {
            log.warn("problem fetching image: {}", e.getMessage());
            failedImageDownloads.add(imgIdStr);
        }

        log.debug("image:\n{}", imageBytes);

        return imageBytes;
    }

    private byte[] downloadImage(String imgUrl) throws IOException {

        URL remoteImg = new URL(imgUrl);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try(
                ReadableByteChannel inChannel = Channels.newChannel(remoteImg.openStream());
                WritableByteChannel outChannel = Channels.newChannel(baos);
        ) {
            // only 100 requests/IP are allowed for every 5 minutes
            // see RATE LIMITING: https://openlibrary.org/dev/docs/api/covers
            //
            // 5 min * 60 secs = 300s / 100 requests = 3 sec per request
            //
            // randomize sleep value within 1 second of a defined throttle value
            long sleep = RandomUtils.nextLong(OPENLIB_IMG_THROTTLE_MS -500, OPENLIB_IMG_THROTTLE_MS +500);
            log.debug("sleeping {}ms", sleep);
            Thread.sleep(sleep); // throttle to ensure no more than 100 requests per 5 min

            int read;
            while((read = inChannel.read(byteBuffer)) > 0) {
                byteBuffer.rewind();
                byteBuffer.limit(read);
                while(read > 0) {
                    read = outChannel.write(byteBuffer);
                }
                byteBuffer.clear();
            }

            int size = baos.size();
            if(size > MINIMUM_VALID_IMAGE_BYTE_SIZE) {
                log.info("OK! ({} bytes) {}", size, imgUrl);
            }
            else {
                log.warn("corrupted download! [size={}]: {}", size, imgUrl);
                failedImageDownloads.add(imgUrl);
            }
        }
        catch(InterruptedException e) {
            log.error("invalid url[{}]: {}", imgUrl, e.getMessage());
        }
        catch(FileNotFoundException e) {
            log.warn("image does not exist: {}", e.getMessage());
            failedImageDownloads.add(imgUrl);
        }

        return baos.toByteArray();
    }

    public Set<String> getFailedImageDownloads() {
        return Collections.unmodifiableSet(failedImageDownloads);
    }
}
