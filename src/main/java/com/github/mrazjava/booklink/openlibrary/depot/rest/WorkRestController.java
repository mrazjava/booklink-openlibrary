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

import com.github.mrazjava.booklink.openlibrary.depot.DepotWork;
import com.github.mrazjava.booklink.openlibrary.depot.service.AbstractDepotService;
import com.github.mrazjava.booklink.openlibrary.depot.service.SearchOperator;
import com.github.mrazjava.booklink.openlibrary.depot.service.WorkService;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Api(
        tags = {"Work"}
)
@RestController
@RequestMapping("/rest/v1/depot/work")
@Slf4j
public class WorkRestController extends AbstractRestController<DepotWork> {

    @Autowired
    private WorkService workService;

    @ApiOperation(value = "Find work matching a specific ID")
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
    public ResponseEntity<DepotWork> findById(@ApiParam(value = "unique work ID", required = true) @PathVariable("id") String workId) {
        log.info("id: {}", workId);
        return ResponseEntity.ok(workService.findById(workId));
    }

    @ApiOperation(value = "Find work matching one or more keys")
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
    public ResponseEntity<List<DepotWork>> findByKey(
            @ApiParam(value = "unique author ID for which to fetch works")
            @RequestParam(value = "authorId", required = false) String authorId) {

        List<DepotWork> results = null;

        if(StringUtils.isNotBlank(authorId)) {
            log.debug("search works by authorId[{}]", authorId);
            results = workService.findByAuthorId(authorId);
            log.info("found {} results", results.size());
        }

        return ResponseEntity.ok(results = ListUtils.emptyIfNull(results));
    }

    @Override
    public ResponseEntity<List<DepotWork>> searchText(String searchQuery, Boolean caseSensitive, String languageCode) {

        caseSensitive = BooleanUtils.toBoolean(caseSensitive);
        log.info("searchText[{}], caseSensitive[{}], languageCode[{}]", searchQuery, caseSensitive, languageCode);
        return ResponseEntity.ok(workService.searchText(searchQuery, languageCode, caseSensitive));
    }

    @Override
    public ResponseEntity<List<DepotWork>> randomRecord(
            Integer sampleCount, Boolean imgS, Boolean imgM, Boolean imgL, SearchOperator operator
    ) {
        return getRandomRecords(sampleCount, imgS, imgM, imgL, operator);
    }
    
    @ApiOperation(value = "Get all records, paginated")
    @GetMapping(path = "paged/all")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(
            {
                    @ApiResponse(
                            message = "ok",
                            code = 200
                    )
            }
    )
    public ResponseEntity<List<DepotWork>> getAll(
    		@ApiParam(value = "page number starting with 1", required = true) @RequestParam("pageNo") Integer pageNo, 
    		@ApiParam(value = "number of rows per page") @RequestParam(value = "pageSize", required = false) Integer size, 
    		@ApiParam(value = "include small img?") @RequestParam(value = "imgS", required = false) Boolean imgS, 
    		@ApiParam(value = "include medium img?") @RequestParam(value = "imgM", required = false) Boolean imgM, 
    		@ApiParam(value = "include large img?") @RequestParam(value = "imgL", required = false) Boolean imgL) {

    	return ResponseEntity.ok(
    			getAll(pageNo, size == null ? 5 : size, Sort.by(Order.asc("titleSample")), imgS, imgM, imgL)
    			);
    }

    @Override
    AbstractDepotService<DepotWork, WorkSchema> getService() {
        return workService;
    }
}
