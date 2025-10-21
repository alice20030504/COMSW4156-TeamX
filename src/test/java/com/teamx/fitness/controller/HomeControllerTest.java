package com.teamx.fitness.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Simple smoke test ensuring the root endpoint redirects to the documented Swagger page.
 */
@DisplayName("HomeController redirect")
class HomeControllerTest {

  private final HomeController controller = new HomeController();

  @Test
  @DisplayName("home redirects to swagger UI path")
  void homeRedirectsToSwagger() {
    RedirectView redirect = controller.home();

    assertEquals("/swagger-ui.html", redirect.getUrl());
  }
}
