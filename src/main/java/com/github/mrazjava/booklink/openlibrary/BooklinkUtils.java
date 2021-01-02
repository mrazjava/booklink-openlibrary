package com.github.mrazjava.booklink.openlibrary;

import com.github.mrazjava.booklink.openlibrary.schema.CoverImage;
import org.apache.commons.io.FileUtils;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;

public final class BooklinkUtils {

    public static CoverImage buildImage(String id, byte[] image) {
        return CoverImage.builder()
                .id(id)
                .image(new Binary(BsonBinarySubType.BINARY, image))
                .sizeBytes(image.length)
                .sizeText(FileUtils.byteCountToDisplaySize(image.length))
                .build();
    }
    
    public static String extractSampleText(String text) {
    	
    	String sampleTxt = text.replaceFirst("[^\\w]*", "");
    	return sampleTxt.length() > 15 ? sampleTxt.substring(0, 10) : sampleTxt;
    }
}
