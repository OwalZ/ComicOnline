package com.example.comiconline.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Comic {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	private String author;

	@Column(length = 2000)
	private String description;

	private String coverUrl; // could store relative path or URL
}
