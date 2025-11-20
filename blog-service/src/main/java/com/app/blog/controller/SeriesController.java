package com.app.blog.controller;

import com.app.blog.dto.series.SeriesListItemDto;
import com.app.blog.service.SeriesService;
import com.app.common.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
