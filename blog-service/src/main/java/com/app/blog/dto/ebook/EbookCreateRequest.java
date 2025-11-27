package com.app.blog.dto.ebook;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EbookCreateRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Author is required")
        @Size(max = 150, message = "Author must not exceed 150 characters")
        String author,

        Integer publishedYear,

        String description,

        @Size(max = 500, message = "Cover URL must not exceed 500 characters")
        String coverUrl,

        @NotBlank(message = "Download URL is required")
        @Size(max = 500, message = "Download URL must not exceed 500 characters")
        String downloadUrl
) {}