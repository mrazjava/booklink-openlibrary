package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.schema.CoverImage;
import com.github.mrazjava.booklink.openlibrary.schema.DefaultImageSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomUtils;
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

    static final String MSG_EXISTS = "\u2612";

    static final String MSG_BLOCKED = "blocked";

    static final String MSG_DOWNLOADED = "\u2611";

    static final String MSG_FAILURE = "failed";

    @Value("${booklink.di.image-download}")
    private Boolean downloadImages;

    @Value("${booklink.di.fetch-original-images}")
    private Boolean fetchOriginalImages;

    private Set<String> failedImageDownloads = new HashSet<>();

    private final int MINIMUM_VALID_IMAGE_BYTE_SIZE = 850;


    public Map<ImageSize, File> downloadImageToFile(String destinationDir, String imgId, String imgTemplate) throws IOException {

        Map<ImageSize, File> files = new HashMap<>();

        if(BooleanUtils.isFalse(downloadImages)) {
            return files;
        }

        for(ImageSize imgSize : ImageSize.values()) {

            if(imgSize == O && BooleanUtils.isFalse(fetchOriginalImages)) continue;

            File imgById = getImageFile(destinationDir, imgSize, imgId);

            files.put(imgSize, imgById);

            if(imgById.exists()) {
                log.debug("file exists, skipping: {}", imgById);
                continue;
            }
            String imgUrl = String.format(imgTemplate, imgId, imgSize);

            log.info("downloading.... {}", imgUrl);

            byte[] imageBytes = downloadImage(imgUrl);
            if(imageBytes != null && imageBytes.length > MINIMUM_VALID_IMAGE_BYTE_SIZE) {
                FileUtils.copyToFile(new ByteArrayInputStream(imageBytes), imgById);
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

    public void downloadImageToBinary(
            String imgId, String imgTemplate, DefaultImageSupport imgSupport, Map<ImageSize, File> cache) throws IOException {

        if(BooleanUtils.isFalse(downloadImages)) {
            return;
        }

        boolean smallExistedB4 = imgSupport.hasImage(S);

        if(!smallExistedB4) {
            ImageSize size = S;
            byte[] image = cache.containsKey(size) ?
                    FileUtils.readFileToByteArray(cache.get(size)) :
                    downloadImage(String.format(imgTemplate, imgId, size));
            imgSupport.setImage(buildImage(imgId, image), size);
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, S);
        }

        boolean mediumExistedB4 = imgSupport.hasImage(M);

        if(!mediumExistedB4) {
            ImageSize size = M;
            byte[] image = cache.containsKey(size) ?
                    FileUtils.readFileToByteArray(cache.get(size)) :
                    downloadImage(String.format(imgTemplate, imgId, size));
            imgSupport.setImage(buildImage(imgId, image), size);
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, M);
        }

        boolean largeExistedB4 = imgSupport.hasImage(ImageSize.L);

        if(!largeExistedB4) {
            ImageSize size = ImageSize.L;
            byte[] image = cache.containsKey(size) ?
                    FileUtils.readFileToByteArray(cache.get(size)) :
                    downloadImage(String.format(imgTemplate, imgId, size));
            imgSupport.setImage(buildImage(imgId, image), size);
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, ImageSize.L);
        }

        boolean originalExistedB4 = imgSupport.hasImage(O);

        if(BooleanUtils.isTrue(fetchOriginalImages)) {

            if (!originalExistedB4) {
                ImageSize size = O;
                byte[] image = cache.containsKey(size) ?
                        FileUtils.readFileToByteArray(cache.get(size)) :
                        downloadImage(String.format(imgTemplate, imgId, size));
                imgSupport.setImage(buildImage(imgId, image), size);
            } else {
                log.debug("skipping binary image[{}]-{}; already exists", imgId, O);
            }
        }

        if(log.isInfoEnabled()) {
            log.info("--- binary ? S{} M{} L{} O{}",
                    smallExistedB4 ? MSG_EXISTS : (imgSupport.hasImage(S) ? MSG_DOWNLOADED : MSG_FAILURE),
                    mediumExistedB4 ? MSG_EXISTS : (imgSupport.hasImage(M) ? MSG_DOWNLOADED : MSG_FAILURE),
                    largeExistedB4 ? MSG_EXISTS : (imgSupport.hasImage(L) ? MSG_DOWNLOADED : MSG_FAILURE),
                    originalExistedB4 ? MSG_EXISTS : (fetchOriginalImages ? (imgSupport.hasImage(O) ? MSG_DOWNLOADED : MSG_FAILURE) : MSG_BLOCKED)
            );
        }
    }

    private CoverImage buildImage(String id, byte[] image) {
        return CoverImage.builder()
                .id(id)
                .image(new Binary(BsonBinarySubType.BINARY, image))
                .sizeBytes(image.length)
                .sizeText(FileUtils.byteCountToDisplaySize(image.length))
                .build();
    }

    private byte[] downloadImage(String imgUrl) throws IOException {

        URL remoteImg = new URL(imgUrl);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
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
            long sleep = RandomUtils.nextLong(OPENLIB_IMG_THROTTLE_MS -500, OPENLIB_IMG_THROTTLE_MS +500);

            log.info("OK! {} | sleeping {}ms", imgUrl, sleep);
            Thread.sleep(sleep); // throttle to ensure no more than 100 requests per 5 min

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
