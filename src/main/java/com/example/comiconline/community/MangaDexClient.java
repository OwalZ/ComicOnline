package com.example.comiconline.community;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** Simple minimal MangaDex API wrapper (blocking) for search and chapter/pages using RestTemplate. */
@Component
public class MangaDexClient {
        private final RestTemplate rest = new RestTemplate();
        private static final String BASE = "https://api.mangadex.org";

                private String get(String url){
                        var headers = new HttpHeaders();
                        headers.add("User-Agent", "ComicOnline/0.1 (+demo)");
                        try {
                                ResponseEntity<String> resp = rest.getForEntity(url, String.class);
                                return resp.getBody();
                        } catch (HttpClientErrorException e){
                                System.out.println("MangaDex API error: "+ e.getStatusCode()+" -> "+ e.getResponseBodyAsString());
                                return "{\"data\":[]}"; // estructura mínima para parse seguro
                        }
        }

        public String search(String title, String lang, int limit){
                return search(title, lang, limit, 0, java.util.List.of("safe"));
        }

        /** Búsqueda con lista de content ratings explícitos (safe, suggestive, erotica, pornographic). */
        public String search(String title, String lang, int limit, int offset, java.util.List<String> ratings){
                // Construimos manualmente para no percent-encode los corchetes (API falla si se codifican)
                String encodedTitle = java.net.URLEncoder.encode(title, java.nio.charset.StandardCharsets.UTF_8);
                StringBuilder url = new StringBuilder(BASE+"/manga?title="+encodedTitle+"&limit="+limit+"&offset="+offset+"&includes[]=cover_art");
                // Filtrado de idioma disponible (si se pasa lang)
                if(lang != null && !lang.isBlank()){
                        url.append("&availableTranslatedLanguage[]=").append(lang);
                }
                if(ratings != null && !ratings.isEmpty()){
                        for(String r: ratings){
                                url.append("&contentRating[]=").append(r);
                        }
                }
                String finalUrl = url.toString();
                System.out.println("MangaDex search URL: "+finalUrl);
                return get(finalUrl);
        }

        /** Top rated mangas (by MangaDex rating desc). */
        public String topRated(int limit, java.util.List<String> ratings){
                if(limit <= 0) limit = 5;
                StringBuilder url = new StringBuilder(BASE+"/manga?limit="+limit+"&order[rating]=desc&includes[]=cover_art");
                if(ratings != null && !ratings.isEmpty()){
                        for(String r: ratings){
                                url.append("&contentRating[]=").append(r);
                        }
                } else {
                        url.append("&contentRating[]=safe");
                }
                String finalUrl = url.toString();
                System.out.println("MangaDex topRated URL: "+finalUrl);
                return get(finalUrl);
        }

                public String manga(String id){
                        String url = BASE+"/manga/"+id+"?includes[]=cover_art";
                        System.out.println("MangaDex manga URL: "+url);
                        return get(url);
                }

        public String chapters(String mangaId, String lang, int limit){
                // Construcción manual para no percent-encode los corchetes (MangaDex exige translatedLanguage[] y order[chapter])
                StringBuilder sb = new StringBuilder(BASE)
                        .append("/chapter?manga=").append(mangaId)
                        .append("&limit=").append(limit)
                        .append("&order[chapter]=asc")
                        // Incluir todos los contentRating para permitir capítulos de material NSFW (por defecto la API excluye pornographic)
                        .append("&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&contentRating[]=pornographic");
                if(lang != null && !lang.isBlank()){
                        sb.append("&translatedLanguage[]=").append(lang);
                }
                String url = sb.toString();
                System.out.println("MangaDex chapters URL: "+url);
                return get(url);
        }

        public String atHome(String chapterId){
                // At-home no requiere contentRating (ya se validó al listar capítulos)
                return get(BASE+"/at-home/server/"+chapterId);
        }
}
