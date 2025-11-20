package com.app.blog.service;

import com.app.blog.dto.series.SeriesCreateRequest;
import com.app.blog.dto.series.SeriesDetailDto;
import com.app.blog.dto.series.SeriesListItemDto;
import com.app.blog.dto.series.SeriesUpdateRequest;

import java.util.List;

public interface SeriesService {

    List<SeriesListItemDto> getSeriesList();

    SeriesDetailDto getSeriesDetail(String slug);

    void createSeries(SeriesCreateRequest seriesCreateRequest);

    void updateSeries(SeriesUpdateRequest seriesUpdateRequest);
}
