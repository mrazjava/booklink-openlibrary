package com.github.mrazjava.booklink.openlibrary.depot.rest;

import com.github.mrazjava.booklink.openlibrary.depot.DepotRecord;
import com.github.mrazjava.booklink.openlibrary.depot.service.AbstractDepotService;
import com.github.mrazjava.booklink.openlibrary.depot.service.SearchOperator;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Slf4j
abstract class AbstractRestController<T extends DepotRecord> implements DepotSearch<T> {

    protected ResponseEntity<List<T>> getRandomRecords(
            Integer sampleCount, Boolean imgS, Boolean imgM, Boolean imgL, SearchOperator operator
    ) {
        sampleCount = ofNullable(sampleCount).orElse(1);

        log.info("sampleCount[{}], imgS[{}], imgM[{}], imgL[{}], operator[{}]",
                sampleCount, imgS, imgM, imgL, operator);

        List<T> results = getService().random(sampleCount, imgS, imgM, imgL, operator);

        if(log.isInfoEnabled()) {
            log.info("found {} results(s): {}",
                    results.size(),
                    results.stream().map(DepotRecord::getId).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(results);
    }
    
    protected ResponseEntity<T> getById(String id, Boolean imgS, Boolean imgM, Boolean imgL) {
    	
    	imgS = BooleanUtils.toBoolean(imgS);
    	imgM = BooleanUtils.toBoolean(imgM);
    	imgL = BooleanUtils.toBoolean(imgL);
    	
    	log.info("id[{}], imgS[{}], imgM[{}], imgL[{}]", id, imgS, imgM, imgL);
    	
    	T result = getService().findById(id, imgS, imgM, imgL);
    	
    	log.info("result: \n{}", result);
    	
    	return ResponseEntity.ok(result);
    }

    abstract AbstractDepotService<T, ?> getService();
}
