package com.example.comiconline.controller;

import com.example.comiconline.service.ComicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@Controller
public class ComicViewController {
	private final ComicService comicService;

	public ComicViewController(ComicService comicService) { this.comicService = comicService; }

	@GetMapping("/comics")
	public String list(Model model, @AuthenticationPrincipal UserDetails ud) {
		model.addAttribute("comics", comicService.findAll());
		if(ud != null) model.addAttribute("authUser", ud.getUsername());
		return "comics";
	}

	@GetMapping("/comics/{id}")
	public String detail(@PathVariable Long id, Model model) {
		model.addAttribute("comic", comicService.findById(id).orElse(null));
		return "comic-detail";
	}

	@GetMapping("/comics/{id}/read")
	public String read(@PathVariable Long id, Model model){
		model.addAttribute("comicId", id);
		return "reader";
	}
}
