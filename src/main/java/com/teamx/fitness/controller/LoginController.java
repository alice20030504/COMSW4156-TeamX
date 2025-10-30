package com.teamx.fitness.controller;

import com.teamx.fitness.model.LoginRequest;
import com.teamx.fitness.model.UserAccount;
import com.teamx.fitness.repository.UserAccountRepository;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
public class LoginController {

  @Autowired
  private UserAccountRepository userAccountRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
    Optional<UserAccount> accountOpt = userAccountRepository.findByUsername(req.getUsername());
    if (accountOpt.isPresent()) {
      UserAccount account = accountOpt.get();
      if (passwordEncoder.matches(req.getPassword(), account.getPasswordHash())) {
        Map<String, Object> ok = new HashMap<>();
        ok.put("message", "Login successful");
        ok.put("role", account.getRole().name());
        return ResponseEntity.ok(ok);
      }
    }
    Map<String, Object> error = new HashMap<>();
    error.put("error", "Invalid credentials");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }
}


