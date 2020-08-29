package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.schema.DefaultImageSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;

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

    static final String MSG_DOWNLOADED = "\u2611";

    public Map<ImageSize, File> downloadImageToFile(String destinationDir, String imgId, String imgTemplate) throws IOException {

        Map<ImageSize, File> files = new HashMap<>();

        for(ImageSize imgSize : ImageSize.values()) {
            File imgById = new File(
                    destinationDir +
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

    public void downloadImageToBinary(
            String imgId, String imgTemplate, DefaultImageSupport imgSupport, Map<ImageSize, File> cache) throws IOException {

        boolean smallExists = imgSupport.hasSmallImage();

        if(!smallExists) {
            byte[] image = cache.containsKey(ImageSize.S) ?
                    FileUtils.readFileToByteArray(cache.get(ImageSize.S)) :
                    downloadImage(String.format(imgTemplate, imgId, ImageSize.S));
            imgSupport.setSmallImage(image);
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, ImageSize.S);
        }

        boolean mediumExists = imgSupport.hasMediumImage();

        if(!mediumExists) {
            byte[] image = cache.containsKey(ImageSize.M) ?
                    FileUtils.readFileToByteArray(cache.get(ImageSize.M)) :
                    downloadImage(String.format(imgTemplate, imgId, ImageSize.M));
            imgSupport.setMediumImage(image);
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, ImageSize.M);
        }

        boolean largeExists = imgSupport.hasLargeImage();

        if(!largeExists) {
            byte[] image = cache.containsKey(ImageSize.L) ?
                    FileUtils.readFileToByteArray(cache.get(ImageSize.L)) :
                    downloadImage(String.format(imgTemplate, imgId, ImageSize.L));
            imgSupport.setLargeImage(image);
        }
        else {
            log.debug("skipping binary image[{}]-{}; already exists", imgId, ImageSize.L);
        }

        if(log.isInfoEnabled()) {
            log.info("--- binary ? S{} M{} L{}",
                    smallExists ? MSG_EXISTS : MSG_DOWNLOADED,
                    mediumExists ? MSG_EXISTS : MSG_DOWNLOADED,
                    largeExists ? MSG_EXISTS : MSG_DOWNLOADED
            );
        }
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

        } catch (InterruptedException e) {
            log.error("invalid url[{}]: {}", imgUrl, e.getMessage());
        }

        return baos.toByteArray();
    }
}
