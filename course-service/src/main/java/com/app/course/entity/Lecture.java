package com.app.course.entity;

import com.app.common.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "lectures")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lecture extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LectureType type;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_preview", nullable = false)
    @Builder.Default
    private Boolean isPreview = false;

    public enum LectureType {
        VIDEO,
        ARTICLE
    }
}