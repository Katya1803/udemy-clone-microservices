package com.app.blog.service;

import com.app.blog.client.UserDto;
import com.app.blog.client.UserServiceClient;
import com.app.blog.dto.post.*;
import com.app.blog.entity.Post;
import com.app.blog.entity.Series;
import com.app.blog.repository.PostRepository;
import com.app.blog.repository.SeriesRepository;
import com.app.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final SeriesRepository seriesRepository;
    private final UserServiceClient userServiceClient;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional(readOnly = true)
    public Page<PostListItemDto> getPublishedPosts(Pageable pageable) {
        log.debug("Fetching published posts");

        Page<Post> posts = postRepository.findByStatus(Post.Status.PUBLISHED, pageable);

        return posts.map(post -> new PostListItemDto(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                extractExcerpt(post.getContent()),
                formatInstant(post.getCreatedAt()),
                post.getAuthor()
        ));
    }

    @Transactional(readOnly = true)
    public Page<PostListItemDto> getMyPosts(String authorId, Pageable pageable) {
        log.debug("Fetching posts by author: {}", authorId);

        Page<Post> posts = postRepository.findByAuthor(authorId, pageable);

        return posts.map(post -> new PostListItemDto(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                extractExcerpt(post.getContent()),
                formatInstant(post.getCreatedAt()),
                post.getAuthor()
        ));
    }

    @Transactional(readOnly = true)
    public PostDetailDto getPostById(String postId) {
        log.debug("Fetching post by id: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        return mapToDetailDto(post);
    }

    @Transactional(readOnly = true)
    public PostDetailDto getPostBySlug(String slug) {
        log.debug("Fetching post by slug: {}", slug);

        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!Post.Status.PUBLISHED.equals(post.getStatus())) {
            throw new ResourceNotFoundException("Post not found");
        }

        return mapToDetailDto(post);
    }

    @Transactional
    public PostDetailDto createPost(PostCreateRequest request, String authorId) {
        log.info("Creating post: {} by author: {}", request.title(), authorId);

        // Fetch username from user-service
        String authorName = authorId; // fallback
        try {
            UserDto user = userServiceClient.getUserByAccountId(authorId).getData();
            if (user != null && user.getUsername() != null) {
                authorName = user.getUsername();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch username for accountId: {}, using ID as fallback", authorId);
        }

        Post post = new Post();
        post.setTitle(request.title());
        post.setSlug(request.slug());
        post.setContent(request.content());
        post.setAuthor(authorId);
        post.setAuthorName(authorName);  // Save username here
        post.setStatus(Post.Status.DRAFT);

        if (request.seriesId() != null) {
            Series series = seriesRepository.findById(request.seriesId())
                    .orElseThrow(() -> new ResourceNotFoundException("Series not found"));
            post.setSeries(series);
            post.setOrderInSeries(request.orderInSeries());
        }

        post = postRepository.save(post);

        return mapToDetailDto(post);
    }
    @Transactional
    public PostDetailDto updatePost(String postId, PostUpdateRequest request, String authorId) {
        log.info("Updating post: {} by author: {}", postId, authorId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getAuthor().equals(authorId)) {
            throw new IllegalStateException("You can only update your own posts");
        }

        if (request.title() != null) {
            post.setTitle(request.title());
        }
        if (request.slug() != null) {
            post.setSlug(request.slug());
        }
        if (request.content() != null) {
            post.setContent(request.content());
        }
        if (request.seriesId() != null) {
            Series series = seriesRepository.findById(request.seriesId())
                    .orElseThrow(() -> new ResourceNotFoundException("Series not found"));
            post.setSeries(series);
        }
        if (request.orderInSeries() != null) {
            post.setOrderInSeries(request.orderInSeries());
        }

        post = postRepository.save(post);

        return mapToDetailDto(post);
    }

    @Transactional
    public PostDetailDto submitForReview(String postId, String authorId) {
        log.info("Submitting post: {} for review by author: {}", postId, authorId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getAuthor().equals(authorId)) {
            throw new IllegalStateException("You can only submit your own posts");
        }

        if (!Post.Status.DRAFT.equals(post.getStatus())) {
            throw new IllegalStateException("Only draft posts can be submitted for review");
        }

        post.setStatus(Post.Status.PENDING);
        post = postRepository.save(post);

        return mapToDetailDto(post);
    }

    @Transactional
    public PostDetailDto approvePost(String postId, String adminId) {
        log.info("Approving post: {} by admin: {}", postId, adminId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!Post.Status.PENDING.equals(post.getStatus())) {
            throw new IllegalStateException("Only pending posts can be approved");
        }

        post.setStatus(Post.Status.PUBLISHED);
        post = postRepository.save(post);

        return mapToDetailDto(post);
    }

    @Transactional
    public PostDetailDto rejectPost(String postId, String adminId) {
        log.info("Rejecting post: {} by admin: {}", postId, adminId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!Post.Status.PENDING.equals(post.getStatus())) {
            throw new IllegalStateException("Only pending posts can be rejected");
        }

        post.setStatus(Post.Status.DRAFT);
        post = postRepository.save(post);

        return mapToDetailDto(post);
    }

    @Transactional
    public void deletePost(String postId, String authorId) {
        log.info("Deleting post: {} by author: {}", postId, authorId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getAuthor().equals(authorId)) {
            throw new IllegalStateException("You can only delete your own posts");
        }

        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Page<PostListItemDto> getPendingPosts(Pageable pageable) {
        log.debug("Fetching pending posts for admin review");

        Page<Post> posts = postRepository.findByStatus(Post.Status.PENDING, pageable);

        return posts.map(post -> new PostListItemDto(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                extractExcerpt(post.getContent()),
                formatInstant(post.getCreatedAt()),
                post.getAuthor()
        ));
    }

    private PostDetailDto mapToDetailDto(Post post) {
        AuthorDto author = new AuthorDto(
                post.getAuthor(),
                post.getAuthorName() != null ? post.getAuthorName() : post.getAuthor(),
                null
        );

        SeriesInfoDto series = null;
        if (post.getSeries() != null) {
            series = new SeriesInfoDto(
                    post.getSeries().getId(),
                    post.getSeries().getTitle(),
                    post.getSeries().getSlug(),
                    post.getOrderInSeries()
            );
        }

        return new PostDetailDto(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                extractExcerpt(post.getContent()),
                post.getContent(),
                post.getStatus().name(),
                formatInstant(post.getCreatedAt()),
                formatInstant(post.getUpdatedAt()),
                author,
                series
        );
    }

    private String extractExcerpt(String content) {
        if (content == null) {
            return "";
        }
        return content.length() > 200
                ? content.substring(0, 200) + "..."
                : content;
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return dateTime.format(FORMATTER);
    }
}