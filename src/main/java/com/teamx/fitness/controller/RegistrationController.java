package com.teamx.fitness.controller;

import com.teamx.fitness.model.RegistrationRequest;
import com.teamx.fitness.model.Role;
import com.teamx.fitness.model.UserAccount;
import com.teamx.fitness.repository.UserAccountRepository;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RegistrationController {

  @Autowired
  private UserAccountRepository userAccountRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest request) {
    if (!request.getPassword().equals(request.getConfirmPassword())) {
      return badRequest("Passwords do not match");
    }

    String username = request.getUsername().trim();
    String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

    if (userAccountRepository.existsByUsername(username)) {
      return badRequest("Username already exists");
    }
    if (userAccountRepository.existsByEmail(email)) {
      return badRequest("Email already exists");
    }

    Role role;
    try {
      role = Role.valueOf(request.getRole().toUpperCase(Locale.ROOT));
    } catch (Exception ex) {
      return badRequest("Invalid role. Must be USER or RESEARCHER");
    }

    UserAccount account = new UserAccount();
    account.setUsername(username);
    account.setEmail(email);
    account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    account.setRole(role);

    userAccountRepository.save(account);

    Map<String, Object> response = new HashMap<>();
    response.put("message", "Registration successful");
    response.put("role", role.name());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  private ResponseEntity<Map<String, Object>> badRequest(String message) {
    Map<String, Object> error = new HashMap<>();
    error.put("message", message);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }
}


