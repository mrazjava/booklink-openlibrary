package com.github.mrazjava.booklink.openlibrary.depot.rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

interface DepotOperations<D> {

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
            @ApiParam(value = "language ISO") @RequestParam(value = "language-iso", required = false) String languageCode);
}
