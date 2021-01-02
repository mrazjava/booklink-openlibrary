package com.github.mrazjava.booklink.openlibrary.depot;

import com.github.mrazjava.booklink.openlibrary.depot.service.StatsService;

import lombok.ToString;

import java.util.Optional;

@ToString
public class DepotStats {

    private StatsService.CountResults authorCounts;
    private StatsService.CountResults workCounts;
    private StatsService.CountResults editionCounts;

    public DepotStats(StatsService.CountResults authorCounts, StatsService.CountResults workCounts, StatsService.CountResults editionCounts) {
        this.authorCounts = authorCounts;
        this.workCounts = workCounts;
        this.editionCounts = editionCounts;
    }

    void setAuthorCounts(StatsService.CountResults authorCounts) {
        this.authorCounts = authorCounts;
    }

    void setWorkCounts(StatsService.CountResults workCounts) {
        this.workCounts = workCounts;
    }

    void setEditionCounts(StatsService.CountResults editionCounts) {
        this.editionCounts = editionCounts;
    }

    public int getAuthorTotalCount() {
        return Optional.ofNullable(authorCounts).map(c -> c.getTotalCount()).orElse(0);
    }

    public int getAuthorSmallImageCount() {
        return Optional.ofNullable(authorCounts).map(c -> c.getSmallImgCount()).orElse(0);
    }

    public int getAuthorMediumImageCount() {
        return Optional.ofNullable(authorCounts).map(c -> c.getMediumImgCount()).orElse(0);
    }

    public int getAuthorLargeImageCount() {
        return Optional.ofNullable(authorCounts).map(c -> c.getLargeImgCount()).orElse(0);
    }

    public int getWorksTotalCount() {
        return Optional.ofNullable(workCounts).map(c -> c.getTotalCount()).orElse(0);
    }

    public int getWorksSmallImageCount() {
        return Optional.ofNullable(workCounts).map(c -> c.getSmallImgCount()).orElse(0);
    }

    public int getWorksMediumImageCount() {
        return Optional.ofNullable(workCounts).map(c -> c.getMediumImgCount()).orElse(0);
    }

    public int getWorksLargeImageCount() {
        return Optional.ofNullable(workCounts).map(c -> c.getLargeImgCount()).orElse(0);
    }

    public int getEditionTotalCount() {
        return Optional.ofNullable(editionCounts).map(c -> c.getTotalCount()).orElse(0);
    }

    public int getEditionSmallImageCount() {
        return Optional.ofNullable(editionCounts).map(c -> c.getSmallImgCount()).orElse(0);
    }

    public int getEditionMediumImageCount() {
        return Optional.ofNullable(editionCounts).map(c -> c.getMediumImgCount()).orElse(0);
    }

    public int getEditionLargeImageCount() {
        return Optional.ofNullable(editionCounts).map(c -> c.getLargeImgCount()).orElse(0);
    }
}
