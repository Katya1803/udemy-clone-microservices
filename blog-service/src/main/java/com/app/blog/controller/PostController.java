package com.app.blog.controller;

import com.app.blog.dto.post.PostCreateRequest;
import com.app.blog.dto.post.PostDetailDto;
import com.app.blog.dto.post.PostListItemDto;
import com.app.blog.dto.post.PostUpdateRequest;
import com.app.blog.dto.post.*;
import com.app.blog.service.PostService;
import com.app.common.dto.response.ApiResponse;
import com.app.common.dto.response.PageResponse;
import com.app.common.util.CurrentAccount;
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
@RequestMapping("/blogs/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/my-posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<PostListItemDto>>> getMyPosts(
            @CurrentAccount String authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Fetching posts for author: {}", authorId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostListItemDto> posts = postService.getMyPosts(authorId, pageable);
        PageResponse<PostListItemDto> response = PageResponse.of(posts);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailDto>> getPostById(@PathVariable String postId) {
        PostDetailDto response = postService.getPostById(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<PostDetailDto>> getPostBySlug(@PathVariable String slug) {
        PostDetailDto response = postService.getPostBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostDetailDto>> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @CurrentAccount String authorId) {

        log.info("Creating post by author: {}", authorId);
        PostDetailDto response = postService.createPost(request, authorId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Post created successfully"));
    }

    @PutMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostDetailDto>> updatePost(
            @PathVariable String postId,
            @Valid @RequestBody PostUpdateRequest request,
            @CurrentAccount String authorId) {

        log.info("Updating post: {} by author: {}", postId, authorId);
        PostDetailDto response = postService.updatePost(postId, request, authorId);

        return ResponseEntity.ok(ApiResponse.success(response, "Post updated successfully"));
    }

    @PostMapping("/{postId}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostDetailDto>> submitForReview(
            @PathVariable String postId,
            @CurrentAccount String authorId) {

        log.info("Submitting post: {} for review by author: {}", postId, authorId);
        PostDetailDto response = postService.submitForReview(postId, authorId);

        return ResponseEntity.ok(ApiResponse.success(response, "Post submitted for review"));
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable String postId,
            @CurrentAccount String authorId) {

        log.info("Deleting post: {} by author: {}", postId, authorId);
        postService.deletePost(postId, authorId);

        return ResponseEntity.ok(ApiResponse.success(null, "Post deleted successfully"));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<PostListItemDto>>> getPendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Fetching pending posts for admin review");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostListItemDto> posts = postService.getPendingPosts(pageable);
        PageResponse<PostListItemDto> response = PageResponse.of(posts);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{postId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PostDetailDto>> approvePost(
            @PathVariable String postId,
            @CurrentAccount String adminId) {

        log.info("Approving post: {} by admin: {}", postId, adminId);
        PostDetailDto response = postService.approvePost(postId, adminId);

        return ResponseEntity.ok(ApiResponse.success(response, "Post approved successfully"));
    }

    @PostMapping("/{postId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PostDetailDto>> rejectPost(
            @PathVariable String postId,
            @CurrentAccount String adminId) {

        log.info("Rejecting post: {} by admin: {}", postId, adminId);
        PostDetailDto response = postService.rejectPost(postId, adminId);

        return ResponseEntity.ok(ApiResponse.success(response, "Post rejected"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostListItemDto>>> getPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // ADD đoạn này
        Page<PostListItemDto> posts;
        if (keyword != null && !keyword.trim().isEmpty()) {
            posts = postService.searchPublishedPosts(keyword.trim(), pageable);
        } else {
            posts = postService.getPublishedPosts(pageable);
        }

        PageResponse<PostListItemDto> response = PageResponse.of(posts);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}