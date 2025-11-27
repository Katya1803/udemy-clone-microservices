package com.app.blog.dto.ebook;

public record EbookListItemDto(
        String id,
        String title,
        String author,
        Integer publishedYear,
        String coverUrl
) {}