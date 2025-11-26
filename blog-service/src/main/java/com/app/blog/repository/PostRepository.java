package com.app.blog.repository;

import com.app.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {

    Page<Post> findByStatus(Post.Status status, Pageable pageable);

    Page<Post> findByAuthor(String authorId, Pageable pageable);

    Optional<Post> findBySlug(String slug);

    @Query("SELECT p FROM Post p WHERE p.status = :status " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchByStatusAndKeyword(
            @Param("status") Post.Status status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}