package com.example.comiconline.controller;

import com.example.comiconline.model.User;
import com.example.comiconline.service.ExternalFavoriteService;
import com.example.comiconline.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ext/fav")
public class ExternalFavoriteController {
    private final ExternalFavoriteService service;
    private final UserService userService;
    public ExternalFavoriteController(ExternalFavoriteService service, UserService userService){ this.service = service; this.userService = userService; }
    private User current(UserDetails ud){ return userService.findByUsername(ud.getUsername()).orElseThrow(); }

    @PostMapping("/toggle")
    public ResponseEntity<?> toggle(@AuthenticationPrincipal UserDetails ud, @RequestBody Map<String,String> body){
        if(ud == null) return ResponseEntity.status(401).build();
        String id = body.getOrDefault("id", "");
        if(id.isBlank()) return ResponseEntity.badRequest().body(Map.of("error","missing id"));
        String title = body.getOrDefault("title", "");
        String cover = body.get("coverUrl");
        String rating = body.get("contentRating");
        var user = current(ud);
        var existing = service.find(user, id);
        if(existing.isPresent()){
            service.remove(user, id);
            return ResponseEntity.ok(Map.of("status","removed"));
        } else {
            service.addOrUpdate(user, id, title, cover, rating);
            return ResponseEntity.ok(Map.of("status","added"));
        }
    }

    @PostMapping("/progress")
    public ResponseEntity<?> progress(@AuthenticationPrincipal UserDetails ud, @RequestBody Map<String,Object> body){
        if(ud == null) return ResponseEntity.status(401).build();
        String id = (String) body.getOrDefault("id", "");
        if(id.isBlank()) return ResponseEntity.badRequest().build();
        String chId = (String) body.getOrDefault("chapterId", "");
        String chLabel = (String) body.getOrDefault("chapterLabel", "");
        int page = ((Number) body.getOrDefault("pageIndex", 0)).intValue();
        service.updateProgress(current(ud), id, chId, chLabel, page);
        return ResponseEntity.ok(Map.of("status","ok"));
    }

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal UserDetails ud){
        if(ud == null) return ResponseEntity.status(401).build();
        var user = current(ud);
        return ResponseEntity.ok(Map.of("favorites", service.findByUser(user)));
    }
}