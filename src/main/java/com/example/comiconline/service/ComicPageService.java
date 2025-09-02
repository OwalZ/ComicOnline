package com.example.comiconline.service;

import com.example.comiconline.model.Comic;
import com.example.comiconline.model.ComicPage;
import com.example.comiconline.repository.ComicPageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ComicPageService {
    private final ComicPageRepository repo;

    public ComicPageService(ComicPageRepository repo) { this.repo = repo; }

    public List<ComicPage> pages(Comic comic){ return repo.findByComicOrderByPageNumber(comic); }
    public Optional<ComicPage> page(Long comicId, int number){ return repo.findByComicIdAndPageNumber(comicId, number); }
    public ComicPage save(ComicPage p){ return repo.save(p); }
    public void delete(Long comicId, int number){ repo.deleteByComicIdAndPageNumber(comicId, number); }
    public long count(Comic comic){ return repo.countByComic(comic); }
}
