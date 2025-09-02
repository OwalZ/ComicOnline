package com.example.comiconline.controller;

import com.example.comiconline.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
	private final UserService userService;

	public AuthController(UserService userService) { this.userService = userService; }

	@GetMapping("/login")
	public String login() { return "login"; }

	@GetMapping("/register")
	public String registerForm() { return "register"; }

	@PostMapping("/register")
	public String register(@RequestParam @NotBlank String username,
						   @RequestParam @NotBlank String password,
						   Model model) {
		try {
			userService.registerUser(username, password);
			return "redirect:/login?registered";
		} catch (IllegalArgumentException ex) {
			model.addAttribute("error", ex.getMessage());
			return "register";
		}
	}
}
