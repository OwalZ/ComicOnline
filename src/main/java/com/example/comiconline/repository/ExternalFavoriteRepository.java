package com.example.comiconline.repository;

import com.example.comiconline.model.ExternalFavorite;
import com.example.comiconline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExternalFavoriteRepository extends JpaRepository<ExternalFavorite, Long> {
    List<ExternalFavorite> findByUser(User user);
    Optional<ExternalFavorite> findByUserAndExternalId(User user, String externalId);
    void deleteByUserAndExternalId(User user, String externalId);
}