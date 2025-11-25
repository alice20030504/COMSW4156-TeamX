package com.teamx.fitness.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Validates the OpenAPI bean wiring. */
@DisplayName("OpenApiConfig bean")
class OpenApiConfigTest {

  /** Configuration under test. */
  private final OpenApiConfig config = new OpenApiConfig();

  @Test
  @DisplayName("customOpenAPI provides service metadata")
  void buildsOpenApiDocument() {
    OpenAPI openAPI = config.customOpenAPI();

    assertNotNull(openAPI.getInfo());
    assertEquals("Fitness Management Service API", openAPI.getInfo().getTitle());
    assertEquals("1.0.0", openAPI.getInfo().getVersion());
  }
}
