package com.app.blog.dto.post;

public record AuthorDto(
        String id,
        String displayName,
        String avatarUrl
) {}