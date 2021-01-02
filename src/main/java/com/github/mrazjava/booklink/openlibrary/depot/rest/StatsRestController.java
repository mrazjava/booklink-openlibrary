package com.github.mrazjava.booklink.openlibrary.depot.rest;

import com.github.mrazjava.booklink.openlibrary.depot.DepotStats;
import com.github.mrazjava.booklink.openlibrary.depot.service.StatsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api(
        tags = {"Statistics"}
)
@RestController
@RequestMapping("/rest/v1/depot/stats")
@Slf4j
public class StatsRestController {

    @Autowired
    private StatsService statsService;

    @ApiOperation(value = "Count information")
    @GetMapping(path = "/counts")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(
            {
                    @ApiResponse(
                            message = "ok",
                            code = 200
                    )
            }
    )
    public ResponseEntity<DepotStats> getCounts() {

        DepotStats depotStats = new DepotStats(
                statsService.countAuthors(), statsService.countWorks(), statsService.countEditions()
        );
        
        log.info("results: {}", depotStats);

        return ResponseEntity.ok(depotStats);
    }
}
