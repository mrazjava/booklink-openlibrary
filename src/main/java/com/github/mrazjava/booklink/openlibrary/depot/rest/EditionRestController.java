package com.github.mrazjava.booklink.openlibrary.depot.rest;

import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.mrazjava.booklink.openlibrary.depot.DepotEdition;
import com.github.mrazjava.booklink.openlibrary.depot.service.AbstractDepotService;
import com.github.mrazjava.booklink.openlibrary.depot.service.EditionService;
import com.github.mrazjava.booklink.openlibrary.depot.service.SearchOperator;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Api(
        tags = {"Edition"}
)
@RestController
@RequestMapping("/rest/v1/depot/edition")
@Slf4j
public class EditionRestController extends AbstractRestController<DepotEdition> implements DepotPageable<DepotEdition> {

    @Autowired
    private EditionService editionService;

    @ApiOperation(value = "Find edition matching a specific ID")
    @GetMapping(path = "/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(
            {
                    @ApiResponse(
                            message = "ok",
                            code = 200
                    )
            }
    )
    public ResponseEntity<DepotEdition> findById(@ApiParam(value = "unique work ID", required = true) @PathVariable("id") String editionId) {
        log.info("id: {}", editionId);
        return ResponseEntity.ok(editionService.findById(editionId));
    }

    @Override
	public ResponseEntity<DepotEdition> findById(String id, Boolean imgS, Boolean imgM, Boolean imgL) {
		return flexById(id, imgS, imgM, imgL);
	}

	@ApiOperation(value = "Find editions matching one or more keys")
    @GetMapping
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(
            {
                    @ApiResponse(
                            message = "ok",
                            code = 200
                    )
            }
    )
    public ResponseEntity<List<DepotEdition>> findByKey(
            @ApiParam(value = "unique author ID for which to fetch edition")
            @RequestParam(value = "authorId", required = false) String authorId) {

        List<DepotEdition> results = null;

        if(StringUtils.isNotBlank(authorId)) {
            log.debug("search editions by authorId[{}]", authorId);
            results = editionService.findByAuthorId(authorId);
            log.info("found {} results", results.size());
        }

        return ResponseEntity.ok(results = ListUtils.emptyIfNull(results));
    }

    @Override
    public ResponseEntity<List<DepotEdition>> searchText(String searchQuery, Boolean caseSensitive, String languageCode) {

        caseSensitive = BooleanUtils.toBoolean(caseSensitive);
        log.info("searchText[{}], caseSensitive[{}], languageCode[{}]", searchQuery, caseSensitive, languageCode);
        return ResponseEntity.ok(editionService.searchText(searchQuery, languageCode, caseSensitive));
    }

    @Override
    public ResponseEntity<List<DepotEdition>> randomRecord(
            Integer sampleCount, Boolean imgS, Boolean imgM, Boolean imgL, SearchOperator operator
    ) {
        return getRandomRecords(sampleCount, imgS, imgM, imgL, operator);
    }

    @Override
	public ResponseEntity<List<DepotEdition>> getAll(
			Integer pageNo, Integer pageSize, Boolean imgS, Boolean imgM, Boolean imgL) {

    	List<DepotEdition> results = getAll(
    			pageNo, 
    			pageSize == null ? DEFAULT_PAGE_SIZE : pageSize, 
    			Sort.by(Order.asc("titleSample")), 
    			imgS, 
    			imgM, 
    			imgL
    	);
    	return ResponseEntity.ok(results);    
	}

	@Override
    AbstractDepotService<DepotEdition, EditionSchema> getService() {
        return editionService;
    }
}
