package com.github.mrazjava.booklink.openlibrary.depot.rest;

import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.mrazjava.booklink.openlibrary.depot.service.SearchOperator;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

interface DepotSearch<D> {

    @ApiOperation(value = "Free style search")
    @GetMapping(path = "/text-search")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(
            {
                    @ApiResponse(
                            message = "ok",
                            code = 200
                    )
            }
    )
    public ResponseEntity<List<D>> searchText(
            @ApiParam(value = "free style text search (eg: 'biology religion')", required = true) @RequestParam("s") String searchQuery,
            @ApiParam(value = "case sensitive?") @RequestParam(value = "case-sensitive", required = false) Boolean caseSensitive,
            @ApiParam(value = "language ISO") @RequestParam(value = "language-iso", required = false) String languageCode,
            @ApiParam(value = "return small image? (default: false)") @RequestParam(value = "img-s", required = false) Boolean imgS,
            @ApiParam(value = "return medium image? (default: false)") @RequestParam(value = "img-m", required = false) Boolean imgM,
            @ApiParam(value = "return large image? (default: false)") @RequestParam(value = "img-l", required = false) Boolean imgL
            );

    @ApiOperation(value = "Random record which has an image")
    @GetMapping(path = "/random-record")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(
            {
                    @ApiResponse(
                            message = "ok",
                            code = 200
                    )
            }
    )
    public ResponseEntity<List<D>> randomRecord(
            @ApiParam(value = "number of samples to return; defaults to 1") @RequestParam(value = "sampleCount", required = false) Integer sampleCount,
            @ApiParam(value = "with SMALL image? (requires operator)") @RequestParam(value = "imgS", required = false) Boolean imgS,
            @ApiParam(value = "with MEDIUM image? (requires operator)") @RequestParam(value = "imgM", required = false) Boolean imgM,
            @ApiParam(value = "with LARGE image? (requires operator)") @RequestParam(value = "imgL", required = false) Boolean imgL,
            @ApiParam(value = "(AND) must have all defined images (OR) at least one (requires at least 1 image param)") SearchOperator imgOperator
    );
    
    @ApiOperation(value = "Find record matching a specific ID optionally exluding image(s)")
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
    public ResponseEntity<D> findById(
    		@ApiParam(value = "unique record ID", required = true) @PathVariable("id") String id,
    		@ApiParam(value = "fetch small img?") @QueryParam(value = "imgS") Boolean imgS,
    		@ApiParam(value = "fetch medium img?") @QueryParam(value = "imgM") Boolean imgM,
    		@ApiParam(value = "fetch large img?") @QueryParam(value = "imgL") Boolean imgL);
}
