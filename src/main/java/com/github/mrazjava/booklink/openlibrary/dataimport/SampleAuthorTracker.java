package com.github.mrazjava.booklink.openlibrary.dataimport;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.github.mrazjava.booklink.openlibrary.dataimport.AuthorHandler.SampleAuthorIdRecord;

@Component
public class SampleAuthorTracker {

    private List<SampleAuthorIdRecord> sampleIds;    

    private List<SampleAuthorIdRecord> sampleIdsWithImage;

    @PostConstruct
    void initialize() {
        sampleIds = new LinkedList<AuthorHandler.SampleAuthorIdRecord>();
        sampleIdsWithImage = new LinkedList<AuthorHandler.SampleAuthorIdRecord>();
    }
    
    public void addSampleId(SampleAuthorIdRecord sample) {
        sampleIds.add(sample);
    }
    
    public int sampleIdCount() {
        return sampleIds.size();
    }
    
    public void addSampleIdWithImage(SampleAuthorIdRecord sample) {
        if(!sample.hasImage()) {
            throw new IllegalStateException("sample requires an image!");
        }
        sampleIdsWithImage.add(sample);
    }
    
    public List<SampleAuthorIdRecord> buildSample(long desiredSamplesWithImage) {

        List<SampleAuthorIdRecord> result = new LinkedList<>(sampleIds);
        result.addAll(sampleIdsWithImage);
        
        Collections.sort(result);
        
        return result;
    }
    
    public List<SampleAuthorIdRecord> getSampleIdsWithImage() {
        return sampleIdsWithImage;
    }
    
    public int sampleIdWithImageCount() {
        return sampleIdsWithImage.size();
    }
}
