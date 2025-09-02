package com.example.comiconline.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comic_pages", uniqueConstraints = @UniqueConstraint(columnNames = {"comic_id","page_number"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties("comic")
public class ComicPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comic_id")
    private Comic comic;

    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    // Ruta relativa servida bajo /uploads/...
    @Column(nullable = false)
    private String imageUrl;
}
