package com.app.blog.service.impl;

import com.app.blog.dto.series.SeriesCreateRequest;
import com.app.blog.dto.series.SeriesDetailDto;
import com.app.blog.dto.series.SeriesListItemDto;
import com.app.blog.dto.series.SeriesUpdateRequest;
import com.app.blog.entity.Series;
import com.app.blog.repository.SeriesRepository;
import com.app.blog.service.SeriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeriesServiceImpl implements SeriesService {

    private final SeriesRepository seriesRepository;

    @Override
    public List<SeriesListItemDto> getSeriesList() {
        return seriesRepository.findAll()
                .stream()
                .map(series -> new SeriesListItemDto(
                        series.getId(),
                        series.getTitle(),
                        series.getSlug()
                ))
                .toList();
    }


    @Override
    public SeriesDetailDto getSeriesDetail(String slug) {
        return null;
    }

    @Override
    public void createSeries(SeriesCreateRequest seriesCreateRequest) {

    }

    @Override
    public void updateSeries(SeriesUpdateRequest seriesUpdateRequest) {

    }
}
