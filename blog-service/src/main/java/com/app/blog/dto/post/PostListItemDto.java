package com.app.blog.dto.post;

public record PostListItemDto(
        String id,
        String title,
        String slug,
        String excerpt,
        String createdAt,
        String authorName
) {}
