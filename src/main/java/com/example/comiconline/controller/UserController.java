package com.example.comiconline.controller;

import com.example.comiconline.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UserController {
    private final UserRepository userRepository;
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    public UserController(UserRepository userRepository){this.userRepository = userRepository;}

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails principal, Model model){
        if(principal!=null){
            userRepository.findByUsername(principal.getUsername()).ifPresent(u->model.addAttribute("userEntity", u));
        }
        return "profile";
    }

    @GetMapping("/settings")
    public String settings(){return "settings";}

    @PostMapping("/settings/theme")
    public void setTheme(@RequestParam String theme, HttpServletResponse response){
        if(!("aurora".equals(theme)||"dusk".equals(theme)||"synth".equals(theme))){response.setStatus(400);return;}
        Cookie c = new Cookie("ui_theme", theme);
        c.setHttpOnly(false);c.setPath("/");c.setMaxAge(60*60*24*365);response.addCookie(c);response.setStatus(204);
    }

    @PostMapping("/settings/avatar")
    public String uploadAvatar(@AuthenticationPrincipal UserDetails principal,
                               @RequestParam("avatar") MultipartFile file) throws Exception {
        if (principal == null) return "redirect:/login";
        if (file.isEmpty()) return "redirect:/settings?err=empty";
        // Tamaño máx 2MB (puedes ajustar en properties también)
        long maxBytes = 2 * 1024 * 1024;
        if (file.getSize() > maxBytes) return "redirect:/settings?err=size";
        var userOpt = userRepository.findByUsername(principal.getUsername());
        if (userOpt.isEmpty()) return "redirect:/settings?err=user";
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')).toLowerCase() : "";
        // Solo png / jpg
        if (!(ext.equals(".png") || ext.equals(".jpg") )) {
            return "redirect:/settings?err=type";
        }
        // Validar content-type declarado
        String ct = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!(ct.equals("image/png") || ct.equals("image/jpg") || ct.equals("image/jpeg"))) {
            return "redirect:/settings?err=mime";
        }
        java.nio.file.Path root = java.nio.file.Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();
        java.nio.file.Files.createDirectories(root);
        // Borrar antiguo si existe
        var user = userOpt.get();
        if (user.getAvatarUrl() != null) {
            try {
                String oldName = user.getAvatarUrl().replaceFirst("/uploads/avatars/", "");
                java.nio.file.Path oldPath = root.resolve(oldName);
                java.nio.file.Files.deleteIfExists(oldPath);
            } catch (Exception ignored) {}
        }
        String filename = principal.getUsername()+"_"+System.currentTimeMillis()+ext;
        java.nio.file.Path dest = root.resolve(filename);
        file.transferTo(dest.toFile());
        user.setAvatarUrl("/uploads/avatars/"+filename);
        userRepository.save(user);
        return "redirect:/settings?ok=1";
    }

    @PostMapping("/settings/avatar/delete")
    public String deleteAvatar(@AuthenticationPrincipal UserDetails principal) throws Exception {
        if (principal == null) return "redirect:/login";
        var userOpt = userRepository.findByUsername(principal.getUsername());
        if (userOpt.isEmpty()) return "redirect:/settings?err=user";
        var user = userOpt.get();
        if (user.getAvatarUrl() != null) {
            java.nio.file.Path root = java.nio.file.Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();
            String oldName = user.getAvatarUrl().replaceFirst("/uploads/avatars/", "");
            java.nio.file.Path oldPath = root.resolve(oldName);
            try { java.nio.file.Files.deleteIfExists(oldPath); } catch (Exception ignored) {}
            user.setAvatarUrl(null);
            userRepository.save(user);
        }
        return "redirect:/settings?ok=deleted";
    }
}
