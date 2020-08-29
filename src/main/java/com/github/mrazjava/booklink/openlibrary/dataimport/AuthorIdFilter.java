package com.github.mrazjava.booklink.openlibrary.dataimport;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class AuthorIdFilter {

    /**
     * Optional file; if exists, only authors in that list will be persisted.
     * One ID per line. Comments are allowed and start with a #. Empty lines
     * are allowed and ignored.
     */
    public static final String FILENAME_ALLOWED_IDS = "author-ids.txt";

    private Set<String> allowedIds = new HashSet<>();

    void load(File workingDirectory) {

        File allowedIdsFile = new File(workingDirectory.getAbsolutePath() + File.separator + FILENAME_ALLOWED_IDS);
        if(allowedIdsFile.exists()) {
            log.info("detected author IDs file: {}", allowedIdsFile.getAbsoluteFile());
            try {
                LineIterator iterator = FileUtils.lineIterator(allowedIdsFile, "UTF-8");
                while(iterator.hasNext()) {
                    String line = iterator.next();
                    if(StringUtils.isNotBlank(line) && !line.startsWith("#")) {
                        allowedIds.add(line);
                    }
                }
                log.info("loaded {} allowed IDs:\n{}", allowedIds.size(), allowedIds);
            } catch (IOException e) {
                log.error("problem loading author id file: {}", e.getMessage());
                allowedIds.clear();
            }
        }
    }

    boolean isEnabled() {
        return !allowedIds.isEmpty();
    }

    boolean exists(String id) {
        return allowedIds.contains(id);
    }
}
