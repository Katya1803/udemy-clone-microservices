package com.app.blog.dto.post;

public record PostDetailDto(
        String id,
        String title,
        String slug,
        String excerpt,
        String content,    // markdown
        String status,
        String createdAt,
        String updatedAt,
        AuthorDto author,
        SeriesInfoDto series
) {}
