package com.teamx.fitness.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

/** Covers the public health endpoint. */
@DisplayName("HealthController")
class HealthControllerTest {

  private final HealthController controller = new HealthController();

  @Test
  @DisplayName("health returns service metadata")
  void healthReturnsPayload() {
    ResponseEntity<Map<String, String>> response = controller.health();

    assertEquals(200, response.getStatusCode().value());
    Map<String, String> body = response.getBody();
    assertNotNull(body);
    assertEquals("UP", body.get("status"));
    assertEquals("Personal Fitness Management Service", body.get("service"));
  }
}
