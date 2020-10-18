package com.github.mrazjava.booklink.openlibrary.depot.rest;

import com.github.mrazjava.booklink.openlibrary.depot.DepotAuthor;
import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(
        tags = {"Author"}
)
@RestController
@RequestMapping("/rest/v1/depot/author")
@Slf4j
public class AuthorRestController {

    @Autowired
    private AuthorRepository authorRepository;

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
        return ResponseEntity.ok(
                authorRepository.findById(authorId)
                        .map(s -> new DepotAuthor(s))
                        .orElse(new DepotAuthor())
        );
    }
}
