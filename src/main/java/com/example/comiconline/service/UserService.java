package com.example.comiconline.service;

import com.example.comiconline.model.Role;
import com.example.comiconline.model.User;
import com.example.comiconline.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public Optional<User> findByUsername(String username) { return userRepository.findByUsername(username); }
	public boolean existsByUsername(String username) { return userRepository.existsByUsername(username); }
	public List<User> findAll() { return userRepository.findAll(); }
	public User save(User user) { return userRepository.save(user); }

	public User registerUser(String username, String rawPassword) {
		if (existsByUsername(username)) throw new IllegalArgumentException("Username already exists");
		User user = User.builder()
				.username(username)
				.password(passwordEncoder.encode(rawPassword))
				.roles(Set.of(Role.USER))
				.build();
		return userRepository.save(user);
	}
}
