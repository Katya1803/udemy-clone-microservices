package com.app.course.entity;

import com.app.common.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "series")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Series extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 150)
    private String title;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    private String thumbnail;

    @OneToMany(mappedBy = "series", fetch = FetchType.LAZY)
    @OrderBy("orderInSeries ASC")
    private List<Post> posts;
}
