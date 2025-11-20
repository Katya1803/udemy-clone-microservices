package com.app.blog.controller;

import com.app.blog.dto.series.*;
import com.app.blog.service.SeriesService;
import com.app.common.dto.response.ApiResponse;
import com.app.common.util.CurrentAccount;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/blogs/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SeriesListItemDto>>> getAllSeries() {
        List<SeriesListItemDto> response = seriesService.getSeriesList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{seriesId}")
    public ResponseEntity<ApiResponse<SeriesDetailDto>> getSeriesById(@PathVariable String seriesId) {
        SeriesDetailDto response = seriesService.getSeriesById(seriesId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SeriesDetailDto>> createSeries(
            @Valid @RequestBody SeriesCreateRequest request,
            @CurrentAccount String authorId) {

        log.info("Creating series by author: {}", authorId);
        SeriesDetailDto response = seriesService.createSeries(request, authorId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Series created successfully"));
    }

    @PutMapping("/{seriesId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SeriesDetailDto>> updateSeries(
            @PathVariable String seriesId,
            @Valid @RequestBody SeriesUpdateRequest request,
            @CurrentAccount String authorId) {

        log.info("Updating series: {} by author: {}", seriesId, authorId);
        SeriesDetailDto response = seriesService.updateSeries(seriesId, request, authorId);

        return ResponseEntity.ok(ApiResponse.success(response, "Series updated successfully"));
    }

    @DeleteMapping("/{seriesId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteSeries(
            @PathVariable String seriesId,
            @CurrentAccount String authorId) {

        log.info("Deleting series: {} by author: {}", seriesId, authorId);
        seriesService.deleteSeries(seriesId, authorId);

        return ResponseEntity.ok(ApiResponse.success(null, "Series deleted successfully"));
    }
}