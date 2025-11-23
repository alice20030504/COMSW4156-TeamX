package com.teamx.fitness.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-level health endpoint that does not require authentication.
 * Returns a simple JSON payload indicating service status.
 */
@RestController
@CrossOrigin(origins = "*")
public class HealthController {

  /**
   * Returns service health information.
   *
   * @return JSON like {"status":"UP","service":"Personal Fitness Management Service","version":"1.0.0"}
   */
  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> health() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put("service", "Personal Fitness Management Service");
    response.put("version", "1.0.0");
    return ResponseEntity.ok(response);
  }
}

