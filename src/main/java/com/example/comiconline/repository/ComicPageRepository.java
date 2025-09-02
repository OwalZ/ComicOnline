package com.example.comiconline.repository;

import com.example.comiconline.model.Comic;
import com.example.comiconline.model.ComicPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComicPageRepository extends JpaRepository<ComicPage, Long> {
    List<ComicPage> findByComicOrderByPageNumber(Comic comic);
    Optional<ComicPage> findByComicIdAndPageNumber(Long comicId, int pageNumber);
    long countByComic(Comic comic);
    void deleteByComicIdAndPageNumber(Long comicId, int pageNumber);
}
