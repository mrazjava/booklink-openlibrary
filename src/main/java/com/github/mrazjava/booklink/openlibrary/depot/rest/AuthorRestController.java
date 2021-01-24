package com.github.mrazjava.booklink.openlibrary.depot.rest;

import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.mrazjava.booklink.openlibrary.depot.DepotAuthor;
import com.github.mrazjava.booklink.openlibrary.depot.service.AbstractDepotService;
import com.github.mrazjava.booklink.openlibrary.depot.service.AuthorService;
import com.github.mrazjava.booklink.openlibrary.depot.service.SearchOperator;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Api(
        tags = {"Author"}
)
@RestController
@RequestMapping("/rest/v1/depot/author")
@Slf4j
public class AuthorRestController extends AbstractRestController<DepotAuthor> implements DepotPageable<DepotAuthor> {

    @Autowired
    private AuthorService authorService;

    @ApiOperation(value = "Find author matching a specific ID")
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
    public ResponseEntity<DepotAuthor> findById(@ApiParam(value = "unique author ID", required = true) @PathVariable("id") String authorId) {
        log.info("id: {}", authorId);
        return ResponseEntity.ok(authorService.findById(authorId));
    }

	@Override
	public ResponseEntity<DepotAuthor> findById(String id, Boolean imgS, Boolean imgM, Boolean imgL) {
		return flexById(id, imgS, imgM, imgL);
	}

	@Override
    public ResponseEntity<List<DepotAuthor>> searchText(
            String searchQuery, Boolean caseSensitive, String languageCode, Boolean imgS, Boolean imgM, Boolean imgL) {

	    imgS = BooleanUtils.toBooleanDefaultIfNull(imgS, false);
	    imgM = BooleanUtils.toBooleanDefaultIfNull(imgM, false);
	    imgL = BooleanUtils.toBooleanDefaultIfNull(imgL, false);
	    
        caseSensitive = BooleanUtils.toBoolean(caseSensitive);
        log.info("searchText[{}], caseSensitive[{}], languageCode[{}], imgS[{}], imgM[{}], imgL[{}]", searchQuery, caseSensitive, languageCode, imgS, imgM, imgL);
        return ResponseEntity.ok(authorService.searchText(searchQuery, languageCode, caseSensitive, imgS, imgM, imgL));
    }

    @Override
    public ResponseEntity<List<DepotAuthor>> randomRecord(
            Integer sampleCount, Boolean imgS, Boolean imgM, Boolean imgL, SearchOperator operator) {

        return getRandomRecords(sampleCount, imgS, imgM, imgL, operator);
    }

    @Override
	public ResponseEntity<List<DepotAuthor>> getAll(
			Integer pageNo, Integer pageSize, Boolean imgS, Boolean imgM, Boolean imgL) {

    	List<DepotAuthor> results = getAll(
    			pageNo, 
    			pageSize == null ? DEFAULT_PAGE_SIZE : pageSize, 
    			Sort.by(Order.asc("name")), 
    			imgS, 
    			imgM, 
    			imgL
    	);
    	return ResponseEntity.ok(results); 
	}

	@Override
    AbstractDepotService<DepotAuthor, AuthorSchema> getService() {
        return authorService;
    }
}
