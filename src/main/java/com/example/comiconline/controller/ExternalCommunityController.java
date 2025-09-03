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
                                    @RequestParam(name="nsfw", defaultValue = "false") boolean nsfw,
                                    @RequestParam(name="ratings", required = false) String ratingsCsv) throws Exception {
        String query = q.isBlank()? "a" : q; // fallback simple
        List<Map<String,Object>> list = new ArrayList<>();
        int p = Math.max(page,1);
        int offset = (p-1)*limit;
        try {
            List<String> allowed = List.of("safe","suggestive","erotica","pornographic");
            List<String> ratings;
            if(ratingsCsv != null && !ratingsCsv.isBlank()){
                ratings = new ArrayList<>();
                for(String r: ratingsCsv.split(",")){
                    String v = r.trim().toLowerCase();
                    if(allowed.contains(v) && !ratings.contains(v)) ratings.add(v);
                }
                // fallback si quedó vacío
                if(ratings.isEmpty()) ratings = nsfw ? List.of("safe","suggestive","erotica","pornographic") : List.of("safe","suggestive");
            } else {
                ratings = nsfw ? List.of("safe","suggestive","erotica","pornographic") : List.of("safe","suggestive");
            }
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
    public ResponseEntity<?> chapters(@PathVariable String mangaId,
                                      @RequestParam(defaultValue="es") String lang,
                                      @RequestParam(defaultValue="100") int limit,
                                      @RequestParam(name="langs", required = false) String langsCsv) throws Exception {
        List<String> allowed = List.of("en","es","pt-br","fr","it","de","ru","ja","ko","zh");
        List<Map<String,Object>> chapters = new ArrayList<>();
        boolean mixed = false;
        String usedLang = lang;

        if(langsCsv != null && !langsCsv.isBlank()) {
            // Modo multi-idioma explícito
            mixed = true;
            LinkedHashSet<String> langs = new LinkedHashSet<>();
            for(String piece : langsCsv.split(",")) {
                String v = piece.trim().toLowerCase();
                if(allowed.contains(v)) langs.add(v);
            }
            if(langs.isEmpty()) langs.add(lang);
            for(String l : langs) {
                String json = client.chapters(mangaId, l, limit);
                JsonNode root = mapper.readTree(json);
                for(JsonNode ch : root.path("data")){
                    JsonNode attrs = ch.path("attributes");
                    chapters.add(Map.of(
                            "id", ch.path("id").asText(),
                            "chapter", attrs.path("chapter").asText("?"),
                            "title", attrs.path("title").asText(""),
                            "lang", l
                    ));
                }
            }
        } else {
            // Lógica original: intentar idioma y fallbacks SOLO si vacío
            List<String> fallbacks = List.of("en","es","pt-br","ja");
            JsonNode root = null;
            Set<String> tried = new LinkedHashSet<>();
            for(String attempt : new LinkedHashSet<String>() {{ add(lang); fallbacks.forEach(this::add); }}) {
                if(tried.contains(attempt)) continue;
                tried.add(attempt);
                String json = client.chapters(mangaId, attempt, limit);
                JsonNode candidate = mapper.readTree(json);
                if(candidate.has("data") && !candidate.path("data").isEmpty()) {
                    root = candidate;
                    usedLang = attempt;
                    break;
                }
                if(root == null) root = candidate; // guardar último vacío
            }
            if(root == null) root = mapper.readTree("{\"data\":[]}");
            for(JsonNode ch : root.path("data")){
                JsonNode attrs = ch.path("attributes");
                chapters.add(Map.of(
                        "id", ch.path("id").asText(),
                        "chapter", attrs.path("chapter").asText("?"),
                        "title", attrs.path("title").asText(""),
                        "lang", usedLang
                ));
            }
        }
        // Ordenar por número de capítulo asc (cuando sea numérico) manteniendo otros al final
        chapters.sort((a,b)->{
            String ca = Objects.toString(a.get("chapter"), "");
            String cb = Objects.toString(b.get("chapter"), "");
            double da, db;
            try { da = Double.parseDouble(ca); } catch (Exception e){ da = Double.NaN; }
            try { db = Double.parseDouble(cb); } catch (Exception e){ db = Double.NaN; }
            if(!Double.isNaN(da) && !Double.isNaN(db)) return Double.compare(da, db);
            if(!Double.isNaN(da)) return -1;
            if(!Double.isNaN(db)) return 1;
            return ca.compareTo(cb);
        });
        return ResponseEntity.ok(Map.of(
                "chapters", chapters,
                "langUsed", usedLang,
                "mixed", mixed,
                "count", chapters.size()
        ));
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
