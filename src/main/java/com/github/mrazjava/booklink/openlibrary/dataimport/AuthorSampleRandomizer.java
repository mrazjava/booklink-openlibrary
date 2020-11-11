package com.github.mrazjava.booklink.openlibrary.dataimport;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@ToString
public class AuthorSampleRandomizer {

    private long sampleCount;
    private long totalCount;

    public AuthorSampleRandomizer(long sampleCount, long totalCount) {
        this.sampleCount = sampleCount;
        this.totalCount = totalCount;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public Set<Long> randomize() {
        Set<Long> randomIndexes = new HashSet<>();
        for(long x = 0; x < sampleCount; x++) {
            randomIndexes.add(1L + (long)(Math.random()*(totalCount-1L)));
        }
        log.info("generated {} random indexes:\n{}", randomIndexes.size(), randomIndexes);
        return randomIndexes;
    }
}
