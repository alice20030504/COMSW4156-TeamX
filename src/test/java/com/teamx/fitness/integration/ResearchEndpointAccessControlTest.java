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

/**
 * Validates {@link ResearchController} access rules and response content for research and mobile
 * clients across valid, boundary, and invalid scenarios without bringing up the MVC stack.
 */
@DisplayName("Research endpoint access control")
class ResearchEndpointAccessControlTest {

  private final ResearchController researchController = new ResearchController();

  @AfterEach
  void resetContext() {
    ClientContext.clear();
  }

  /**
   * Valid scenario: a research client retrieves anonymized demographics successfully.
   */
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

  /**
   * Invalid scenario: mobile clients are forbidden from research endpoints.
   */
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

  /**
   * Boundary scenario: missing objective parameter defaults to aggregate results.
   */
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

  /**
   * Valid scenario: BULK objective returns the expected macro mix.
   */
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

  /**
   * Valid scenario: CUT objective highlights higher protein ratios.
   */
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

  /**
   * Valid scenario: research client can access population health metrics.
   */
  @Test
  @DisplayName("Population health metrics returned for research client")
  void populationHealth_AllowsResearchClient() {
    ClientContext.setClientId("research-tool5");

    ResponseEntity<Map<String, Object>> response = researchController.getPopulationHealth();

    assertEquals(200, response.getStatusCode().value());
    assertEquals("All data is aggregated and anonymized", response.getBody().get("dataProtection"));
  }

  /**
   * Invalid scenario: mobile clients are forbidden from population health data.
   */
  @Test
  @DisplayName("Mobile client blocked from population health")
  void populationHealth_BlocksMobileClient() {
    ClientContext.setClientId("mobile-app2");

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> researchController.getPopulationHealth());

    assertEquals(403, exception.getStatusCode().value());
  }
}
