package com.app.blog.service;

import com.app.blog.dto.ebook.*;
import com.app.blog.entity.Ebook;
import com.app.blog.repository.EbookRepository;
import com.app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EbookService {

    private final EbookRepository ebookRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional(readOnly = true)
    public Page<EbookListItemDto> getEbooks(String keyword, Pageable pageable) {
        log.debug("Fetching ebooks with keyword: {}", keyword);

        Page<Ebook> ebooks;
        if (StringUtils.hasText(keyword)) {
            ebooks = ebookRepository.search(keyword.trim(), pageable);
        } else {
            ebooks = ebookRepository.findAll(pageable);
        }

        return ebooks.map(this::mapToListItem);
    }

    @Transactional(readOnly = true)
    public EbookDetailDto getEbookById(String ebookId) {
        log.debug("Fetching ebook by id: {}", ebookId);

        Ebook ebook = ebookRepository.findById(ebookId)
                .orElseThrow(() -> new ResourceNotFoundException("Ebook not found"));

        return mapToDetail(ebook);
    }

    @Transactional
    public EbookDetailDto createEbook(EbookCreateRequest request) {
        log.info("Creating ebook: {}", request.title());

        Ebook ebook = Ebook.builder()
                .title(request.title())
                .author(request.author())
                .publishedYear(request.publishedYear())
                .description(request.description())
                .coverUrl(request.coverUrl())
                .downloadUrl(request.downloadUrl())
                .build();

        ebook = ebookRepository.save(ebook);

        return mapToDetail(ebook);
    }

    @Transactional
    public EbookDetailDto updateEbook(String ebookId, EbookUpdateRequest request) {
        log.info("Updating ebook: {}", ebookId);

        Ebook ebook = ebookRepository.findById(ebookId)
                .orElseThrow(() -> new ResourceNotFoundException("Ebook not found"));

        if (StringUtils.hasText(request.title())) {
            ebook.setTitle(request.title());
        }
        if (StringUtils.hasText(request.author())) {
            ebook.setAuthor(request.author());
        }
        if (request.publishedYear() != null) {
            ebook.setPublishedYear(request.publishedYear());
        }
        if (request.description() != null) {
            ebook.setDescription(request.description());
        }
        if (request.coverUrl() != null) {
            ebook.setCoverUrl(request.coverUrl());
        }
        if (StringUtils.hasText(request.downloadUrl())) {
            ebook.setDownloadUrl(request.downloadUrl());
        }

        ebook = ebookRepository.save(ebook);

        return mapToDetail(ebook);
    }

    @Transactional
    public void deleteEbook(String ebookId) {
        log.info("Deleting ebook: {}", ebookId);

        Ebook ebook = ebookRepository.findById(ebookId)
                .orElseThrow(() -> new ResourceNotFoundException("Ebook not found"));

        ebookRepository.delete(ebook);
    }

    private EbookListItemDto mapToListItem(Ebook ebook) {
        return new EbookListItemDto(
                ebook.getId(),
                ebook.getTitle(),
                ebook.getAuthor(),
                ebook.getPublishedYear(),
                ebook.getCoverUrl()
        );
    }

    private EbookDetailDto mapToDetail(Ebook ebook) {
        return new EbookDetailDto(
                ebook.getId(),
                ebook.getTitle(),
                ebook.getAuthor(),
                ebook.getPublishedYear(),
                ebook.getDescription(),
                ebook.getCoverUrl(),
                ebook.getDownloadUrl(),
                formatInstant(ebook.getCreatedAt())
        );
    }

    private String formatInstant(Instant instant) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(FORMATTER);
    }
}