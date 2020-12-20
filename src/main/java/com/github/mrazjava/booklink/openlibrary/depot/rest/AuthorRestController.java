package com.github.mrazjava.booklink.openlibrary.depot.rest;

import com.github.mrazjava.booklink.openlibrary.depot.DepotAuthor;
import com.github.mrazjava.booklink.openlibrary.depot.service.AbstractDepotService;
import com.github.mrazjava.booklink.openlibrary.depot.service.AuthorService;
import com.github.mrazjava.booklink.openlibrary.depot.service.SearchOperator;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

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
    
    @ApiOperation(value = "Find author matching a specific ID optionally exlude image(s)")
    @GetMapping(path = "/flex/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(
            {
                    @ApiResponse(
                            message = "ok",
                            code = 200
                    )
            }
    )
    public ResponseEntity<DepotAuthor> findById(
    		@ApiParam(value = "unique author ID", required = true) @PathVariable("id") String authorId,
    		@ApiParam(value = "fetch small img?") @QueryParam(value = "imgS") Boolean imgS,
    		@ApiParam(value = "fetch medium img?") @QueryParam(value = "imgM") Boolean imgM,
    		@ApiParam(value = "fetch large img?") @QueryParam(value = "imgL") Boolean imgL) {

    	log.info("id: {}", authorId);
    	return ResponseEntity.ok(authorService.findById(
    			authorId, 
    			BooleanUtils.toBoolean(imgS), 
    			BooleanUtils.toBoolean(imgM), 
    			BooleanUtils.toBoolean(imgL))
    			);
    }

    @Override
    public ResponseEntity<List<DepotAuthor>> searchText(String searchQuery, Boolean caseSensitive, String languageCode) {

        caseSensitive = BooleanUtils.toBoolean(caseSensitive);
        log.info("searchText[{}], caseSensitive[{}], languageCode[{}]", searchQuery, caseSensitive, languageCode);
        return ResponseEntity.ok(authorService.searchText(searchQuery, languageCode, caseSensitive));
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
