package com.example.comiconline.controller;

import com.example.comiconline.service.ComicService;
import com.example.comiconline.community.MangaDexClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	private final ComicService comicService;
	private final MangaDexClient mangaDexClient;
	private final ObjectMapper mapper = new ObjectMapper();

	public HomeController(ComicService comicService, MangaDexClient mangaDexClient) {
		this.comicService = comicService;
		this.mangaDexClient = mangaDexClient;
	}

	@GetMapping("/")
	public String index(Model model) {
		var locals = comicService.findAll();
		model.addAttribute("comics", locals);
		if(locals.isEmpty()){
			try {
				String json = mangaDexClient.topRated(5, java.util.List.of("safe","suggestive"));
				JsonNode root = mapper.readTree(json);
				java.util.List<java.util.Map<String,Object>> featured = new java.util.ArrayList<>();
				for(JsonNode item: root.path("data")){
					String id = item.path("id").asText();
					JsonNode attrs = item.path("attributes");
					String title = attrs.path("title").fields().hasNext()? attrs.path("title").elements().next().asText():"";
					String coverFile = null;
					for(JsonNode rel: item.path("relationships")){
						if("cover_art".equals(rel.path("type").asText())){
							coverFile = rel.path("attributes").path("fileName").asText(null); break; }
					}
					String coverUrl = coverFile==null? null : "https://uploads.mangadex.org/covers/"+id+"/"+coverFile+".256.jpg";
					featured.add(java.util.Map.of("id", id, "title", title, "coverUrl", coverUrl));
				}
				model.addAttribute("extFeatured", featured);
			} catch (Exception ignored) {}
		}
		return "index";
	}
}
