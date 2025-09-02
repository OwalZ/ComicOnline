package com.example.comiconline.service;

import com.example.comiconline.model.ExternalFavorite;
import com.example.comiconline.model.User;
import com.example.comiconline.repository.ExternalFavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ExternalFavoriteService {
    private final ExternalFavoriteRepository repo;
    public ExternalFavoriteService(ExternalFavoriteRepository repo){ this.repo = repo; }

    public List<ExternalFavorite> findByUser(User u){ return repo.findByUser(u); }
    public Optional<ExternalFavorite> find(User u, String externalId){ return repo.findByUserAndExternalId(u, externalId); }
    public ExternalFavorite save(ExternalFavorite f){ return repo.save(f); }
    @Transactional
    public ExternalFavorite addOrUpdate(User u, String externalId, String title, String coverUrl, String rating){
        return find(u, externalId).map(ex -> {
            ex.setTitle(title); ex.setCoverUrl(coverUrl); ex.setContentRating(rating); return repo.save(ex);
        }).orElseGet(() -> repo.save(ExternalFavorite.builder()
                .user(u).externalId(externalId).title(title).coverUrl(coverUrl).contentRating(rating).build()));
    }
    @Transactional
    public void remove(User u, String externalId){ repo.deleteByUserAndExternalId(u, externalId); }
    @Transactional
    public void updateProgress(User u, String externalId, String chapterId, String chapterLabel, int pageIndex){
        ExternalFavorite f = find(u, externalId).orElse(null);
        if(f == null) return; f.setLastChapterId(chapterId); f.setLastChapterLabel(chapterLabel); f.setLastPageIndex(pageIndex); repo.save(f);
    }
}