package com.example.comiconline.repository;

import com.example.comiconline.model.Favorite;
import com.example.comiconline.model.User;
import com.example.comiconline.model.Comic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(User user);
    Optional<Favorite> findByUserAndComic(User user, Comic comic);
    void deleteByUserAndComic(User user, Comic comic);
}
