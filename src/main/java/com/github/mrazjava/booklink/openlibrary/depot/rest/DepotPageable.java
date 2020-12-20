package com.github.mrazjava.booklink.openlibrary.depot.rest;

import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public interface DepotPageable<D> {

	static final int DEFAULT_PAGE_SIZE = 5;
	
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
    ResponseEntity<List<D>> getAll(
    		@ApiParam(value = "page number starting with 1", required = true) @RequestParam("pageNo") Integer pageNo, 
    		@ApiParam(value = "number of rows per page") @RequestParam(value = "pageSize", required = false) Integer pageSize, 
    		@ApiParam(value = "include small img?") @RequestParam(value = "imgS", required = false) Boolean imgS, 
    		@ApiParam(value = "include medium img?") @RequestParam(value = "imgM", required = false) Boolean imgM, 
    		@ApiParam(value = "include large img?") @RequestParam(value = "imgL", required = false) Boolean imgL);
}
