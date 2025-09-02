package com.example.comiconline.service;

import com.example.comiconline.model.Comic;
import com.example.comiconline.model.Favorite;
import com.example.comiconline.model.User;
import com.example.comiconline.repository.FavoriteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public List<Favorite> findByUser(User user) { return favoriteRepository.findByUser(user); }
    public Optional<Favorite> findByUserAndComic(User user, Comic comic) { return favoriteRepository.findByUserAndComic(user, comic); }
    public Favorite addFavorite(User user, Comic comic) {
        return favoriteRepository.findByUserAndComic(user, comic)
                .orElseGet(() -> favoriteRepository.save(Favorite.builder().user(user).comic(comic).build()));
    }
    public void removeFavorite(User user, Comic comic) { favoriteRepository.deleteByUserAndComic(user, comic); }
}
