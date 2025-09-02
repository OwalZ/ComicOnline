package com.example.comiconline.controller;

import com.example.comiconline.model.Comic;
import com.example.comiconline.model.ComicPage;
import com.example.comiconline.repository.ComicRepository;
import com.example.comiconline.service.ComicPageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/comics/{comicId}/pages")
public class ComicPageRestController {
    private final ComicRepository comicRepository;
    private final ComicPageService pageService;
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public ComicPageRestController(ComicRepository comicRepository, ComicPageService pageService) {
        this.comicRepository = comicRepository; this.pageService = pageService; }

    private Comic requireComic(Long id){ return comicRepository.findById(id).orElseThrow(); }

    @GetMapping
    public ResponseEntity<?> list(@PathVariable Long comicId){
        Comic comic = requireComic(comicId);
        var pages = pageService.pages(comic);
        Map<String,Object> body = new HashMap<>();
        body.put("comicId", comicId);
        body.put("total", pages.size());
        body.put("pages", pages);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{number}")
    public ResponseEntity<?> getOne(@PathVariable Long comicId, @PathVariable int number){
        return pageService.page(comicId, number).<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadPage(@PathVariable Long comicId,
                                        @RequestParam("image") MultipartFile file,
                                        @RequestParam(value = "pageNumber", required = false) Integer pageNumber) throws Exception {
        if(file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error","empty"));
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = original.contains(".")? original.substring(original.lastIndexOf('.')).toLowerCase():"";
        if(!(ext.equals(".png")||ext.equals(".jpg"))) return ResponseEntity.badRequest().body(Map.of("error","type"));
        String ct = file.getContentType()==null?"":file.getContentType().toLowerCase();
        if(!(ct.equals("image/png")||ct.equals("image/jpg")||ct.equals("image/jpeg"))) return ResponseEntity.badRequest().body(Map.of("error","mime"));
        Comic comic = requireComic(comicId);
        if(pageNumber==null){ pageNumber = (int)pageService.count(comic) + 1; }
        // Store
        Path root = Paths.get(uploadDir, "pages", comicId.toString()).toAbsolutePath().normalize();
        Files.createDirectories(root);
        String filename = "p"+pageNumber+"_"+System.currentTimeMillis()+ext;
        Path dest = root.resolve(filename);
        file.transferTo(dest);
        ComicPage page = ComicPage.builder().comic(comic).pageNumber(pageNumber).imageUrl("/uploads/pages/"+comicId+"/"+filename).build();
        pageService.save(page);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{number}")
    public ResponseEntity<?> delete(@PathVariable Long comicId, @PathVariable int number) throws Exception {
        var pageOpt = pageService.page(comicId, number);
        if(pageOpt.isEmpty()) return ResponseEntity.notFound().build();
        // delete file
        Path root = Paths.get(uploadDir, "pages", comicId.toString()).toAbsolutePath().normalize();
        String filePart = pageOpt.get().getImageUrl().replaceFirst("/uploads/pages/"+comicId+"/", "");
        try { Files.deleteIfExists(root.resolve(filePart)); } catch (Exception ignored) {}
        pageService.delete(comicId, number);
        return ResponseEntity.noContent().build();
    }
}
