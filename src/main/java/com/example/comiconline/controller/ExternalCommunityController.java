package com.example.comiconline.controller;

import com.example.comiconline.community.MangaDexClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ext")
public class ExternalCommunityController {
    private final MangaDexClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    public ExternalCommunityController(MangaDexClient client){ this.client = client; }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false, defaultValue = "") String q,
                                    @RequestParam(defaultValue = "es") String lang,
                                    @RequestParam(defaultValue="12") int limit,
                                    @RequestParam(defaultValue="1") int page,
                                    @RequestParam(name="nsfw", defaultValue = "false") boolean nsfw) throws Exception {
        String query = q.isBlank()? "a" : q; // fallback simple
        List<Map<String,Object>> list = new ArrayList<>();
        int p = Math.max(page,1);
        int offset = (p-1)*limit;
        try {
            java.util.List<String> ratings = nsfw ? java.util.List.of("safe","suggestive","erotica","pornographic") : java.util.List.of("safe","suggestive");
            String json = client.search(query, lang, limit, offset, ratings);
            JsonNode root = mapper.readTree(json);
            for(JsonNode item: root.path("data")){
                String id = item.path("id").asText();
                JsonNode attrs = item.path("attributes");
                String title = attrs.path("title").fields().hasNext()? attrs.path("title").elements().next().asText():"";
                String contentRating = attrs.path("contentRating").asText("");
                String coverFile = null;
                for(JsonNode rel: item.path("relationships")){
                    if("cover_art".equals(rel.path("type").asText())){
                        coverFile = rel.path("attributes").path("fileName").asText(null); break; }
                }
                if(coverFile == null){
                    try {
                        JsonNode single = mapper.readTree(client.manga(id));
                        for(JsonNode rel: single.path("data").path("relationships")){
                            if("cover_art".equals(rel.path("type").asText())){
                                coverFile = rel.path("attributes").path("fileName").asText(null); break; }
                        }
                    } catch (Exception ignored) {}
                }
                String coverUrl = coverFile==null? null : "https://uploads.mangadex.org/covers/"+id+"/"+coverFile; // fix domain mangadex
                list.add(Map.of("id", id, "title", title, "coverUrl", coverUrl, "contentRating", contentRating));
            }
        } catch (Exception e){
            // devolvemos lista vacía sin propagar para no romper la página
        }
    int total = 0;
    try { total = mapper.readTree(client.search(query, lang, 1, 0, java.util.List.of("safe"))).path("total").asInt(0); } catch (Exception ignored) {}
    return ResponseEntity.ok(Map.of("results", list, "page", p, "limit", limit, "hasMore", list.size()==limit));
    }

    @GetMapping("/manga/{mangaId}/chapters")
    public ResponseEntity<?> chapters(@PathVariable String mangaId, @RequestParam(defaultValue="es") String lang, @RequestParam(defaultValue="100") int limit) throws Exception {
        String json = client.chapters(mangaId, lang, limit);
        JsonNode root = mapper.readTree(json);
        // Si error 400 o sin data y lang != en, intentar fallback a en
        if((!root.has("data") || root.path("data").isEmpty()) && !"en".equals(lang)){
            String jsonEn = client.chapters(mangaId, "en", limit);
            JsonNode rootEn = mapper.readTree(jsonEn);
            if(rootEn.has("data") && !rootEn.path("data").isEmpty()){
                root = rootEn; // usar fallback
            }
        }
        List<Map<String,Object>> chapters = new ArrayList<>();
        for(JsonNode ch : root.path("data")){
            JsonNode attrs = ch.path("attributes");
            chapters.add(Map.of(
                    "id", ch.path("id").asText(),
                    "chapter", attrs.path("chapter").asText("?"),
                    "title", attrs.path("title").asText("")
            ));
        }
        return ResponseEntity.ok(Map.of("chapters", chapters));
    }

    @GetMapping("/chapter/{chapterId}/pages")
    public ResponseEntity<?> pages(@PathVariable String chapterId) throws Exception {
        String json = client.atHome(chapterId);
        JsonNode root = mapper.readTree(json);
        String baseUrl = root.path("baseUrl").asText();
        JsonNode chapter = root.path("chapter");
        String hash = chapter.path("hash").asText();
        List<String> data = new ArrayList<>();
        for(JsonNode f : chapter.path("data")){
            data.add(baseUrl + "/data/" + hash + "/" + f.asText());
        }
        return ResponseEntity.ok(Map.of("pages", data));
    }
}
