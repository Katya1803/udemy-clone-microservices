package com.app.blog.controller;

import com.app.blog.dto.ebook.*;
import com.app.blog.service.EbookService;
import com.app.common.dto.response.ApiResponse;
import com.app.common.dto.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/blogs/ebooks")
@RequiredArgsConstructor
public class EbookController {

    private final EbookService ebookService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EbookListItemDto>>> getEbooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<EbookListItemDto> ebooks = ebookService.getEbooks(keyword, pageable);
        PageResponse<EbookListItemDto> response = PageResponse.of(ebooks);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{ebookId}")
    public ResponseEntity<ApiResponse<EbookDetailDto>> getEbookById(@PathVariable String ebookId) {
        EbookDetailDto response = ebookService.getEbookById(ebookId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EbookDetailDto>> createEbook(
            @Valid @RequestBody EbookCreateRequest request) {

        log.info("Admin creating ebook: {}", request.title());
        EbookDetailDto response = ebookService.createEbook(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Ebook created successfully"));
    }

    @PutMapping("/{ebookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EbookDetailDto>> updateEbook(
            @PathVariable String ebookId,
            @Valid @RequestBody EbookUpdateRequest request) {

        log.info("Admin updating ebook: {}", ebookId);
        EbookDetailDto response = ebookService.updateEbook(ebookId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Ebook updated successfully"));
    }

    @DeleteMapping("/{ebookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEbook(@PathVariable String ebookId) {

        log.info("Admin deleting ebook: {}", ebookId);
        ebookService.deleteEbook(ebookId);

        return ResponseEntity.ok(ApiResponse.success(null, "Ebook deleted successfully"));
    }
}