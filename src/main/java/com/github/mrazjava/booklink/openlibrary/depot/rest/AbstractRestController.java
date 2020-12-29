package com.github.mrazjava.booklink.openlibrary.depot.rest;

import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;

import com.github.mrazjava.booklink.openlibrary.depot.DepotRecord;
import com.github.mrazjava.booklink.openlibrary.depot.service.AbstractDepotService;
import com.github.mrazjava.booklink.openlibrary.depot.service.SearchOperator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractRestController<T extends DepotRecord> implements DepotSearch<T> {

    protected ResponseEntity<List<T>> getRandomRecords(
            Integer sampleCount, Boolean imgS, Boolean imgM, Boolean imgL, SearchOperator operator
    ) {
        sampleCount = ofNullable(sampleCount).orElse(1);

        log.info("sampleCount[{}], imgS[{}], imgM[{}], imgL[{}], operator[{}]",
                sampleCount, imgS, imgM, imgL, operator);

        List<T> results = getService().random(sampleCount, imgS, imgM, imgL, operator, getRandomRecordsBaseCriteria());

        if(log.isInfoEnabled()) {
            log.info("found {} results(s): {}",
                    results.size(),
                    results.stream().map(DepotRecord::getId).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(results);
    }
    
    protected Criteria getRandomRecordsBaseCriteria() {
    	return null;
    }
    
    protected ResponseEntity<T> flexById(String id, Boolean imgS, Boolean imgM, Boolean imgL) {
    	
    	imgS = BooleanUtils.toBoolean(imgS);
    	imgM = BooleanUtils.toBoolean(imgM);
    	imgL = BooleanUtils.toBoolean(imgL);
    	
    	log.info("id[{}], imgS[{}], imgM[{}], imgL[{}]", id, imgS, imgM, imgL);
    	
    	T result = getService().findById(id, imgS, imgM, imgL);
    	
    	log.info("result: {}", result);
    	
    	return ResponseEntity.ok(result);
    }
    
    protected List<T> getAll(int pageNo, int size, Sort sort, Boolean imgS, Boolean imgM, Boolean imgL) {

    	imgS = BooleanUtils.toBoolean(imgS);
    	imgM = BooleanUtils.toBoolean(imgM);
    	imgL = BooleanUtils.toBoolean(imgL);

    	log.info("pageNo[{}], size[{}], sort[{}], imgS[{}], imgM[{}], imgL[{}]", pageNo, size, sort, imgS, imgM, imgL);
    	
    	List<T> results = getService().findAll(pageNo, size, sort, imgS, imgM, imgL);
    	
    	log.info("found {} result(s)", results.size());
    	if(log.isDebugEnabled()) {
    		log.debug("{}", results.stream().map(DepotRecord::getId).collect(Collectors.toList()));
    	}
    	
    	return results;
    }

    abstract AbstractDepotService<T, ?> getService();
}
