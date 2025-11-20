package com.app.blog.entity;

import com.app.common.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper=false)
@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private Series series;

    @Column(name = "order_in_series")
    private Integer orderInSeries;

    @Column(nullable = false)
    private Status status;

    public enum Status {
        PUBLISHED, DRAFT, PENDING
    }

}
