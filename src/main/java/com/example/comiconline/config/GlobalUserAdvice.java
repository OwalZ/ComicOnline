package com.example.comiconline.config;

import com.example.comiconline.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
@Component
public class GlobalUserAdvice {
    private final UserRepository userRepository;
    public GlobalUserAdvice(UserRepository userRepository){this.userRepository=userRepository;}

    @ModelAttribute
    public void addUser(Model model){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth!=null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())){
            userRepository.findByUsername(auth.getName()).ifPresent(u-> model.addAttribute("userEntity", u));
        }
    }
}
