package com.teamx.fitness.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.teamx.fitness.controller.ResearchController;
import com.teamx.fitness.security.ClientContext;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@DisplayName("Research endpoint access control")
class ResearchEndpointAccessControlTest {

  private final ResearchController researchController = new ResearchController();

  @AfterEach
  void resetContext() {
    ClientContext.clear();
  }

  @Test
  @DisplayName("Research clients receive anonymized demographics")
  void demographics_AllowsResearchClient() {
    ClientContext.setClientId("research-tool1");

    ResponseEntity<Map<String, Object>> response =
        researchController.getDemographicStats("25-34", null, null);

    assertEquals(200, response.getStatusCode().value());
    assertEquals(Boolean.TRUE, response.getBody().get("dataAnonymized"));
    assertEquals(Boolean.TRUE, response.getBody().get("privacyCompliant"));
  }

  @Test
  @DisplayName("Mobile clients are blocked from research data")
  void demographics_BlocksMobileClient() {
    ClientContext.setClientId("mobile-app1");

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> researchController.getDemographicStats(null, null, null));

    assertEquals(403, exception.getStatusCode().value());
  }

  @Test
  @DisplayName("Nutrition trends fall back to default objective")
  void nutritionTrends_DefaultsWhenObjectiveMissing() {
    ClientContext.setClientId("research-tool2");

    ResponseEntity<Map<String, Object>> response =
        researchController.getNutritionTrends(null);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("ALL", response.getBody().get("objective"));
    assertFalse((Boolean) response.getBody().get("containsPII"));
  }

  @Test
  @DisplayName("Nutrition trends for BULK objective return expected macros")
  void nutritionTrends_BulkObjective() {
    ClientContext.setClientId("research-tool3");

    ResponseEntity<Map<String, Object>> response =
        researchController.getNutritionTrends("BULK");

    assertEquals(200, response.getStatusCode().value());
    Map<String, Object> macroDistribution =
        (Map<String, Object>) response.getBody().get("macroDistribution");
    assertEquals(45, macroDistribution.get("carbs"));
    assertEquals(30, macroDistribution.get("protein"));
  }

  @Test
  @DisplayName("Nutrition trends for CUT objective emphasize protein")
  void nutritionTrends_CutObjective() {
    ClientContext.setClientId("research-tool4");

    ResponseEntity<Map<String, Object>> response =
        researchController.getNutritionTrends("CUT");

    assertEquals(200, response.getStatusCode().value());
    Map<String, Object> macroDistribution =
        (Map<String, Object>) response.getBody().get("macroDistribution");
    assertEquals(40, macroDistribution.get("protein"));
    assertEquals(2000, macroDistribution.get("averageCalories"));
  }
}
