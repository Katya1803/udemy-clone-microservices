package com.app.blog.repository;

import com.app.blog.entity.Ebook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EbookRepository extends JpaRepository<Ebook, String> {

    @Query("SELECT e FROM Ebook e WHERE " +
            "LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Ebook> search(@Param("keyword") String keyword, Pageable pageable);
}