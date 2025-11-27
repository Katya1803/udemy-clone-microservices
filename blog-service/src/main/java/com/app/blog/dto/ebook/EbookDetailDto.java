package com.app.blog.dto.ebook;

public record EbookDetailDto(
        String id,
        String title,
        String author,
        Integer publishedYear,
        String description,
        String coverUrl,
        String downloadUrl,
        String createdAt
) {}