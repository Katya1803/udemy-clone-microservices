package com.app.blog.service;

import com.app.blog.dto.series.*;
import com.app.blog.entity.Post;
import com.app.blog.entity.Series;
import com.app.blog.repository.SeriesRepository;
import com.app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;

    @Transactional(readOnly = true)
    public List<SeriesListItemDto> getSeriesList() {
        log.debug("Fetching all series");
        return seriesRepository.findAll().stream()
                .map(series -> new SeriesListItemDto(
                        series.getId(),
                        series.getTitle(),
                        series.getSlug()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SeriesDetailDto getSeriesById(String seriesId) {
        log.debug("Fetching series by id: {}", seriesId);

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found"));

        List<SeriesPostItemDto> posts = series.getPosts() != null
                ? series.getPosts().stream()
                .filter(post -> Post.Status.PUBLISHED.equals(post.getStatus()))
                .map(post -> new SeriesPostItemDto(
                        post.getId(),
                        post.getTitle(),
                        post.getSlug(),
                        post.getOrderInSeries()
                ))
                .collect(Collectors.toList())
                : List.of();

        return new SeriesDetailDto(
                series.getId(),
                series.getTitle(),
                series.getSlug(),
                series.getDescription(),
                series.getThumbnail(),
                posts
        );
    }

    @Transactional
    public SeriesDetailDto createSeries(SeriesCreateRequest request, String authorId) {
        log.info("Creating series: {} by author: {}", request.title(), authorId);

        Series series = Series.builder()
                .title(request.title())
                .slug(request.slug())
                .description(request.description())
                .thumbnail(request.thumbnail())
                .build();

        series = seriesRepository.save(series);

        return new SeriesDetailDto(
                series.getId(),
                series.getTitle(),
                series.getSlug(),
                series.getDescription(),
                series.getThumbnail(),
                List.of()
        );
    }

    @Transactional
    public SeriesDetailDto updateSeries(String seriesId, SeriesUpdateRequest request, String authorId) {
        log.info("Updating series: {} by author: {}", seriesId, authorId);

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found"));

        if (request.title() != null) {
            series.setTitle(request.title());
        }
        if (request.slug() != null) {
            series.setSlug(request.slug());
        }
        if (request.description() != null) {
            series.setDescription(request.description());
        }
        if (request.thumbnail() != null) {
            series.setThumbnail(request.thumbnail());
        }

        series = seriesRepository.save(series);

        return getSeriesById(series.getId());
    }

    @Transactional
    public void deleteSeries(String seriesId, String authorId) {
        log.info("Deleting series: {} by author: {}", seriesId, authorId);

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found"));

        seriesRepository.delete(series);
    }
}