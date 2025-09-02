package com.example.comiconline.controller;

import com.example.comiconline.model.Comic;
import com.example.comiconline.service.ComicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/comics")
public class AdminController {
	private final ComicService comicService;

	public AdminController(ComicService comicService) { this.comicService = comicService; }

	@GetMapping
	public String adminList(Model model) {
		model.addAttribute("comics", comicService.findAll());
		model.addAttribute("comic", new Comic());
		return "admin";
	}

	@PostMapping
	public String create(@ModelAttribute Comic comic) {
		comicService.save(comic);
		return "redirect:/admin/comics";
	}

	@PostMapping("/{id}")
	public String update(@PathVariable Long id, @ModelAttribute Comic comic) {
		comic.setId(id);
		comicService.save(comic);
		return "redirect:/admin/comics";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id) {
		comicService.delete(id);
		return "redirect:/admin/comics";
	}

	@PostMapping("/bulk-delete")
	public String bulkDelete(@RequestParam(name = "ids", required = false) java.util.List<Long> ids){
		if(ids != null){
			ids.forEach(comicService::delete);
		}
		return "redirect:/admin/comics";
	}
}
