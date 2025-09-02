package com.example.comiconline.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "external_favorites", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","external_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExternalFavorite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "external_id", nullable = false, length = 64)
    private String externalId;

    @Column(length = 400)
    private String title;

    private String coverUrl;
    private String contentRating;

    // progreso de lectura
    private String lastChapterId;
    private String lastChapterLabel;
    private Integer lastPageIndex; // 0-based
}