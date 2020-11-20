package com.github.mrazjava.booklink.openlibrary.depot.rest;

import com.github.mrazjava.booklink.openlibrary.depot.DepotWork;
import com.github.mrazjava.booklink.openlibrary.depot.service.WorkService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@Api(
        tags = {"Work"}
)
@RestController
@RequestMapping("/rest/v1/depot/work")
@Slf4j
public class WorkRestController implements DepotSearch<DepotWork> {

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
    public ResponseEntity<List<DepotWork>> randomWithImage(Integer sampleCount) {
        return ResponseEntity.ok(workService.random(
                Optional.ofNullable(sampleCount).orElse(1),
                false, false, true, null
            )
        );
    }
}