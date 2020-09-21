package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.OpenLibraryIntegrationException;
import com.github.mrazjava.booklink.openlibrary.schema.DefaultImageSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;

import static com.github.mrazjava.booklink.openlibrary.BooklinkUtils.buildImage;
import static com.github.mrazjava.booklink.openlibrary.dataimport.ImageSize.*;

@Slf4j
@Component
public class ImageDownloader {

    /**
     * default throttling time; maybe overriden by calling component
     */
    public static final long OPENLIB_IMG_THROTTLE_MS = 2000;

    /**
     * When downloading images individually from Openlibrary.org, slow down the
     * frequency of image requests to avoid blokade from openlibrary which limits
     * the number of requests allowed per a specific time period.
     *
     * Number of milliseconds to wait after downloading an image.
     */
    private long throttleMs = OPENLIB_IMG_THROTTLE_MS;

    static final String MSG_EXISTS = "exists";

    static final String MSG_IGNORED = "ignored";

    static final String MSG_DOWNLOADED = "downloaded";

    static final String MSG_FETCHED = "cache-hit";

    static final String MSG_FAILURE = "failed";

    @Value("${booklink.di.image-pull}")
    private Boolean imagePull;

    @Value("${booklink.di.fetch-original-images}")
    private Boolean fetchOriginalImages;

    private File imageDirectory;

    private Set<String> failedImageDownloads = new HashSet<>();

    final static int MINIMUM_VALID_IMAGE_BYTE_SIZE = 850;

    private IdFilter idFilter;


    public ImageDownloader(@Value("${booklink.di.image-dir}") String imgDir) {
        if(StringUtils.isNotBlank(imgDir)) {
            imageDirectory = new File(imgDir);
        }
    }

    public long getThrottleMs() {
        return throttleMs;
    }

    public void setThrottleMs(long throttleMs) {
        this.throttleMs = throttleMs;
    }

    public void setIdFilter(IdFilter idFilter) {
        this.idFilter = idFilter;
    }

    /**
     * Downloads all available sizes of an image from openlibrary.org to files on a disk. In addition
     * to saving downloaded images into files, returns key-value lookup of the downloaded images
     * for subsequent usage. Original sized image is only downloaded if {@code booklink.di.fetch-original-images} is
     * enabled.
     *
     * @param destinationDir where files should be saved
     * @param imgId to identify image for download
     * @param urlTemplate of openlibary.org source to use for a download
     * @return key = size of image successfully downloaded, value = binary content of an image
     */
    public Map<ImageSize, byte[]> downloadImageFiles(String destinationDir, Long imgId, String urlTemplate) throws IOException {

        Map<ImageSize, byte[]> files = new HashMap<>();

        if(BooleanUtils.isFalse(imagePull)) {
            return files;
        }

        if(idFilter.exists(Long.toString(imgId))) {
            log.info("filter matched; ignoring all sizes! {}", imgId);
            return files;
        }

        for(ImageSize imgSize : ImageSize.values()) {

            if(imgSize == O && BooleanUtils.isFalse(fetchOriginalImages)) continue;

            byte[] imageBytes = downloadImageToFile(destinationDir, imgId, imgSize, urlTemplate);
            if(imageBytes != null) {
                files.put(imgSize, imageBytes);
            }
        }

        return files;
    }

    /**
     * Download image from openlibrary.org and saves it to a file in the destination directory.
     *
     * @return content of a downloaded file
     */
    public byte[] downloadImageToFile(String destinationDir, Long imgId, ImageSize size, String urlTemplate) throws IOException {

        if(BooleanUtils.isFalse(imagePull)) {
            return null;
        }

        File imageFile = getImageFile(destinationDir, size, imgId);

        if(!imageFile.getParentFile().exists()) {
            throw new OpenLibraryIntegrationException(String.format("destination directory does not exist: %s", imageFile.getParentFile()));
        }

        if(imageFile.exists()) {
            log.debug("file exists [{}]; skipping", imageFile);
            return FileUtils.readFileToByteArray(imageFile);
        }

        if(idFilter.exists(FilenameUtils.getBaseName(imageFile.getAbsolutePath()))) {
            log.info("filter matched; ignoring! {}", imageFile.getName());
            return null;
        }

        String imgUrl = String.format(urlTemplate, imgId, size);

        log.info("downloading.... {}", imgUrl);

        byte[] imageBytes = downloadImage(imgUrl);
        if(imageBytes != null && imageBytes.length > MINIMUM_VALID_IMAGE_BYTE_SIZE) {
            FileUtils.copyToFile(new ByteArrayInputStream(imageBytes), imageFile);
        }

        return imageBytes;
    }

    private File getImageFile(String destinationDir, ImageSize imgSize, Long imgId) {
        return new File(
                destinationDir +
                        File.separator +
                        (imgSize == O ? String.format("%d.jpg", imgId) : String.format("%d-%s.jpg", imgId, imgSize))
        );
    }

    public boolean filesExist(String destinationDir, Long imgId, List<ImageSize> sizes) {

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
            Long imgId, String imgTemplateUrl, DefaultImageSupport imgSupport, Map<ImageSize, byte[]> cache) throws IOException {

        Map<ImageSize, String> statusMsgs = new HashMap<>();

        if(BooleanUtils.isFalse(imagePull)) {
            return Set.of();
        }

        if(!imgSupport.hasImage(S)) {
            ImageSize size = S;
            boolean fetch = isImageInCache(cache, size);
            byte[] image = fetch ?
                    cache.get(size) :
                    downloadImage(String.format(imgTemplateUrl, imgId, size));
            imgSupport.setImage(buildImage(Long.toString(imgId), image), size);
            statusMsgs.put(size, fetch ? MSG_FETCHED : MSG_DOWNLOADED);
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, S);
            statusMsgs.put(ImageSize.S, MSG_EXISTS);
        }

        if(!imgSupport.hasImage(M)) {
            ImageSize size = M;
            boolean fetch = isImageInCache(cache, size);
            byte[] image = fetch ?
                    cache.get(size) :
                    downloadImage(String.format(imgTemplateUrl, imgId, size));
            imgSupport.setImage(buildImage(Long.toString(imgId), image), size);
            statusMsgs.put(size, fetch ? MSG_FETCHED : MSG_DOWNLOADED);
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, M);
            statusMsgs.put(ImageSize.M, MSG_EXISTS);
        }

        if(!imgSupport.hasImage(ImageSize.L)) {
            ImageSize size = ImageSize.L;
            boolean fetch = isImageInCache(cache, size);
            byte[] image = fetch ?
                    cache.get(size) :
                    downloadImage(String.format(imgTemplateUrl, imgId, size));
            imgSupport.setImage(buildImage(Long.toString(imgId), image), size);
            statusMsgs.put(size, fetch ? MSG_FETCHED : MSG_DOWNLOADED);
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, ImageSize.L);
            statusMsgs.put(ImageSize.L, MSG_EXISTS);
        }

        if(BooleanUtils.isTrue(fetchOriginalImages)) {
            if (!imgSupport.hasImage(O)) {
                ImageSize size = O;
                boolean fetch = isImageInCache(cache, size);
                byte[] image = fetch ?
                        cache.get(size) :
                        downloadImage(String.format(imgTemplateUrl, imgId, size));
                imgSupport.setImage(buildImage(Long.toString(imgId), image), size);
                statusMsgs.put(size, fetch ? MSG_FETCHED : MSG_DOWNLOADED);
            } else {
                log.debug("skipping binary image[{}]-{}; already exists", imgId, O);
                statusMsgs.put(ImageSize.O, MSG_EXISTS);
            }
        }
        else {
            statusMsgs.put(ImageSize.O, MSG_IGNORED);
        }

        if(log.isInfoEnabled()) {
            log.info("--- mongo img[{}] ? S({}) M({}) L({}) O({})",
                    imgId,
                    statusMsgs.get(ImageSize.S),
                    statusMsgs.get(ImageSize.M),
                    statusMsgs.get(ImageSize.L),
                    statusMsgs.get(ImageSize.O)
            );
        }

        return statusMsgs.keySet().stream().filter(k -> {
            String msg = statusMsgs.get(k);
            return MSG_DOWNLOADED.equals(msg) || MSG_FETCHED.equals(msg);
        }).collect(Collectors.toSet());
    }

    /**
     * Assuming cover archives exist locally (have been downloaded manually), search the *.tar
     * archives and fetch image if available. No internet connection is used for this operation.
     *
     * @param imgId an ID (not OLID) of an image to fetch
     * @param imgSupport to populate with fetched images
     * @param nonBulkImages alternate location where covers may reside as individual images; if available
     *          fetch is attempted from this location as well
     * @return image sizes which failed to fetch
     */
    public Set<ImageSize> fetchImageToBinary(Long imgId, DefaultImageSupport imgSupport, File nonBulkImages) {

        Map<ImageSize, String> statusMsgs = new HashMap<>();

        if(BooleanUtils.isFalse(imagePull)) {
            return Set.of();
        }

        if(!imgSupport.hasImage(S)) {
            ImageSize size = S;
            byte[] imageBytes = fetchImage(imgId, size, imageDirectory, nonBulkImages);
            if(imageBytes != null) {
                imgSupport.setImage(buildImage(Long.toString(imgId), imageBytes), size);
                statusMsgs.put(size, MSG_FETCHED);
            }
            else {
                statusMsgs.put(size, MSG_FAILURE);
            }
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, S);
            statusMsgs.put(ImageSize.S, MSG_EXISTS);
        }

        if(!imgSupport.hasImage(M)) {
            ImageSize size = M;
            byte[] imageBytes = fetchImage(imgId, size, imageDirectory, nonBulkImages);
            if(imageBytes != null) {
                imgSupport.setImage(buildImage(Long.toString(imgId), imageBytes), size);
                statusMsgs.put(size, MSG_FETCHED);
            }
            else {
                statusMsgs.put(size, MSG_FAILURE);
            }
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, M);
            statusMsgs.put(ImageSize.M, MSG_EXISTS);
        }

        if(!imgSupport.hasImage(ImageSize.L)) {
            ImageSize size = ImageSize.L;
            byte[] imageBytes = fetchImage(imgId, size, imageDirectory, nonBulkImages);
            if(imageBytes != null) {
                imgSupport.setImage(buildImage(Long.toString(imgId), imageBytes), size);
                statusMsgs.put(size, MSG_FETCHED);
            }
            else {
                statusMsgs.put(size, MSG_FAILURE);
            }
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, ImageSize.L);
            statusMsgs.put(ImageSize.L, MSG_EXISTS);
        }

        if(BooleanUtils.isTrue(fetchOriginalImages)) {
            if (!imgSupport.hasImage(O)) {
                ImageSize size = O;
                byte[] imageBytes = fetchImage(imgId, size, imageDirectory, nonBulkImages);
                if(imageBytes != null) {
                    imgSupport.setImage(buildImage(Long.toString(imgId), imageBytes), size);
                    statusMsgs.put(size, MSG_FETCHED);
                }
                else {
                    statusMsgs.put(size, MSG_FAILURE);
                }
            } else {
                log.debug("skipping binary image[{}]-{}; already exists", imgId, O);
                statusMsgs.put(ImageSize.O, MSG_FETCHED);
            }
        }
        else {
            statusMsgs.put(ImageSize.O, MSG_IGNORED);
        }

        if(log.isInfoEnabled()) {
            log.info("--- binary ? S({}) M({}) L({}) O({})",
                    statusMsgs.get(ImageSize.S),
                    statusMsgs.get(ImageSize.M),
                    statusMsgs.get(ImageSize.L),
                    statusMsgs.get(ImageSize.O)
            );
        }

        return statusMsgs.keySet().stream()
                .filter(k -> MSG_FAILURE.equals(statusMsgs.get(k)))
                .collect(Collectors.toSet());
    }

    private boolean isImageInCache(Map<ImageSize, byte[]> cache, ImageSize size) {
        return cache != null && cache.containsKey(size);
    }

    /**
     * Attempts to fetch already downloaded image. No internet connection is required or used
     * for this operation. First checks if image is available as an individual file by checking
     * directory where non-bulk images are stored. If file does not exist, then bulk directory
     * is checked and a relevant TAR archive inspected. If file is found in either location its
     * content is returned.
     *
     * @return content of an image if found, {@code null} otherwise
     */
    public byte[] fetchImage(Long imgId, ImageSize size, File bulkImagesDir, File nonBulkImagesDir) {

        if(nonBulkImagesDir != null && nonBulkImagesDir.exists()) {
            File individualImage = getImageFile(nonBulkImagesDir.getAbsolutePath(), size, imgId);
            if(individualImage.exists()) {
                try {
                    return FileUtils.readFileToByteArray(individualImage);
                } catch (IOException e) {
                    log.warn("cannot read non-bulk image: {}", e.getMessage());
                }
            }
        }

        if(bulkImagesDir == null) {
            return null;
        }

        if(imgId < 100000) {
            log.warn("wrong image id [{}]", imgId);
            return null;
        }

        String imgIdStr = StringUtils.leftPad(Long.toString(imgId), 7, "0");
        byte[] imageBytes = null;

        char idx1 = imgIdStr.charAt(0);
        String idx2 = imgIdStr.substring(1,3);

        String tarFileName = size.name().toLowerCase() + "_covers_000" + idx1 + "_" + idx2 + ".tar";
        String tarFullPath = bulkImagesDir.getPath() + File.separator + tarFileName.substring(0, 13) + File.separator + tarFileName;

        log.debug("tar source: {}", tarFullPath);

        final String imgFileName = "000" + imgIdStr + "-" + size.name() + ".jpg";
        try(TarArchiveInputStream tais = new TarArchiveInputStream(new FileInputStream(tarFullPath))) {

            while(tais.getNextEntry() != null) {
                TarArchiveEntry entry = tais.getCurrentEntry();
                if(imgFileName.equals(entry.getName())) {
                    log.info("{} - {} | {}", entry.getName(), entry.getSize(), entry.getLastModifiedDate());
                    imageBytes = tais.readNBytes((int)entry.getSize());
                    break;
                }
            }
        }
        catch(FileNotFoundException e) {
            log.warn("{}: {}", imgFileName, e.getMessage());
        }
        catch(IOException e) {
            throw new OpenLibraryIntegrationException("unexpected error fetching file", e);
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
            long sleep = RandomUtils.nextLong(throttleMs-500, throttleMs+500);
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
