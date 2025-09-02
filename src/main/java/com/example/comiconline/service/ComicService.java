package com.example.comiconline.service;

import com.example.comiconline.model.Comic;
import com.example.comiconline.repository.ComicRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ComicService {
	private final ComicRepository comicRepository;

	public ComicService(ComicRepository comicRepository) {
		this.comicRepository = comicRepository;
	}

	public List<Comic> findAll() { return comicRepository.findAll(); }
	public Optional<Comic> findById(Long id) { return comicRepository.findById(id); }
	public Comic save(Comic comic) { return comicRepository.save(comic); }
	public void delete(Long id) { comicRepository.deleteById(id); }
}
