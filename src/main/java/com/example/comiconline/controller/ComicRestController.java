package com.example.comiconline.controller;

import com.example.comiconline.model.Comic;
import com.example.comiconline.service.ComicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comics")
public class ComicRestController {
	private final ComicService comicService;

	public ComicRestController(ComicService comicService) { this.comicService = comicService; }

	@GetMapping
	public List<Comic> all() { return comicService.findAll(); }

	@GetMapping("/{id}")
	public ResponseEntity<Comic> get(@PathVariable Long id) {
		return comicService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}
}
