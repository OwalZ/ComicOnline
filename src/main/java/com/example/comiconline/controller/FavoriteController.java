package com.example.comiconline.controller;

import com.example.comiconline.model.Comic;
import com.example.comiconline.model.User;
import com.example.comiconline.service.ComicService;
import com.example.comiconline.service.FavoriteService;
import com.example.comiconline.service.UserService;
import com.example.comiconline.service.ExternalFavoriteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class FavoriteController {
	private final FavoriteService favoriteService;
	private final UserService userService;
	private final ComicService comicService;
	private final ExternalFavoriteService externalFavoriteService;

	public FavoriteController(FavoriteService favoriteService, UserService userService, ComicService comicService, ExternalFavoriteService externalFavoriteService) {
		this.favoriteService = favoriteService;
		this.userService = userService;
		this.comicService = comicService;
		this.externalFavoriteService = externalFavoriteService;
	}

	private User currentUser(UserDetails ud) { return userService.findByUsername(ud.getUsername()).orElseThrow(); }

	@PostMapping("/favorites/{comicId}")
	public String addFavorite(@PathVariable Long comicId, @AuthenticationPrincipal UserDetails ud) {
		Comic comic = comicService.findById(comicId).orElseThrow();
		favoriteService.addFavorite(currentUser(ud), comic);
		return "redirect:/comics/" + comicId;
	}

	@PostMapping("/favorites/{comicId}/remove")
	public String removeFavorite(@PathVariable Long comicId, @AuthenticationPrincipal UserDetails ud) {
		Comic comic = comicService.findById(comicId).orElseThrow();
		favoriteService.removeFavorite(currentUser(ud), comic);
		return "redirect:/comics/" + comicId;
	}

	@GetMapping("/favorites")
	public String listFavorites(@AuthenticationPrincipal UserDetails ud, Model model) {
		User user = currentUser(ud);
		model.addAttribute("favorites", favoriteService.findByUser(user));
		model.addAttribute("extFavorites", externalFavoriteService.findByUser(user));
		return "favorites";
	}
}
