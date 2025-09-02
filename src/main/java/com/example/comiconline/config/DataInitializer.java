package com.example.comiconline.config;

import com.example.comiconline.model.Comic;
import com.example.comiconline.model.Role;
import com.example.comiconline.model.User;
import com.example.comiconline.repository.ComicRepository;
import com.example.comiconline.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner initData(ComicRepository comicRepository, UserRepository userRepository, PasswordEncoder encoder) {
		return args -> {
			// Eliminado seeding de cómics de ejemplo (The Dawn, Night Watch)
			if (userRepository.count() == 0) {
				userRepository.save(User.builder()
						.username("admin")
						.password(encoder.encode("admin"))
						.roles(Set.of(Role.ADMIN, Role.USER))
						.build());
			}
		};
	}
}
