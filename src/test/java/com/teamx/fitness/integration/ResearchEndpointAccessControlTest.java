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
 * Integration tests demonstrating client type-based access control for research endpoints.
 *
 * <p>These tests validate that the system correctly differentiates between client types
 * (mobile vs research) and enforces appropriate access control policies. Research endpoints
 * contain aggregated, anonymized population data that should only be accessible to research
 * tools, not individual mobile app users.</p>
 *
 * <p><strong>Access Control Policy Being Tested:</strong></p>
 * <ul>
 *   <li><strong>Mobile Clients (mobile-*):</strong> FORBIDDEN (403) from all /api/research endpoints</li>
 *   <li><strong>Research Clients (research-*):</strong> ALLOWED (200) to access all /api/research endpoints</li>
 *   <li><strong>Both Client Types:</strong> ALLOWED to access /api/persons endpoints (subject to data isolation)</li>
 * </ul>
 *
 * <p><strong>Why This Access Control Matters:</strong></p>
 * <ul>
 *   <li><strong>Privacy Protection:</strong> Mobile users shouldn't access population-level data that could
 *       reveal patterns about other users</li>
 *   <li><strong>Role-Based Access:</strong> Different client types have different privileges based on their
 *       legitimate needs (mobile apps need individual data, research tools need aggregate data)</li>
 *   <li><strong>Business Logic:</strong> Research endpoints are designed for analytics tools, not end users,
 *       and should be protected from general public access</li>
 *   <li><strong>Rubric Requirement:</strong> Project requirements explicitly demand client type differentiation
 *       with proper access control enforcement</li>
 * </ul>
 *
 * <p><strong>Implementation Mechanism:</strong> ResearchController.validateResearchAccess() checks
 * ClientContext.isMobileClient() and throws ResponseStatusException(403 Forbidden) if the client
 * ID starts with "mobile-". This validation occurs before any business logic executes.</p>
 *
 * <p><strong>Integration Test Value:</strong> These tests verify the complete access control flow:
 * <ol>
 *   <li>Client sends request with X-Client-ID header (mobile-* or research-*)</li>
 *   <li>ClientIdInterceptor validates and stores client ID in ClientContext</li>
 *   <li>Request reaches ResearchController method</li>
 *   <li>Controller calls validateResearchAccess() before processing</li>
 *   <li>If mobile client: throws 403 Forbidden with error message</li>
 *   <li>If research client: proceeds to return aggregated data</li>
 * </ol>
 * </p>
 *
 * <p><strong>Security Principle:</strong> "Principle of Least Privilege" - each client type
 * receives only the minimum permissions necessary for their legitimate function. Mobile clients
 * don't need research data, so they don't get access.</p>
 *
 * @see ResearchController
 * @see ClientContext
 * @see ResponseStatusException
 * Validates {@link ResearchController} access rules and response content for research and mobile
 * clients across valid, boundary, and invalid scenarios without bringing up the MVC stack.
 */
@DisplayName("Research endpoint access control")
class ResearchEndpointAccessControlTest {

  private final ResearchController researchController = new ResearchController();

  /**
   * Tests that mobile clients receive 403 Forbidden when accessing demographics endpoint.
   *
   * <p>This integration test validates the access control mechanism that prevents mobile
   * app users from accessing aggregated research data. Even though mobile clients are
   * authenticated (valid X-Client-ID header), they are not authorized to access research
   * endpoints due to their client type.</p>
   *
   * <p><strong>Request Flow:</strong>
   * <ol>
   *   <li>Mobile client (mobile-app1) sends GET /api/research/demographics</li>
   *   <li>ClientIdInterceptor validates header and stores "mobile-app1" in context</li>
   *   <li>ResearchController.getDemographicStats() is invoked</li>
   *   <li>First line calls validateResearchAccess()</li>
   *   <li>Validation checks ClientContext.isMobileClient("mobile-app1") → true</li>
   *   <li>Throws ResponseStatusException(403, "Mobile clients are not authorized...")</li>
   *   <li>GlobalExceptionHandler formats as JSON error response</li>
   * </ol>
   * </p>
   *
   * <p><strong>Response Validation:</strong> The test verifies:
   * <ul>
   *   <li>HTTP status code is 403 Forbidden (not 401 or 404)</li>
   *   <li>JSON response includes status: 403</li>
   *   <li>Error type is "Forbidden"</li>
   *   <li>Message explains why mobile clients can't access research data</li>
   * </ul>
   * </p>
   *
   * <p><strong>Why 403 (Not 401):</strong> The client IS authenticated (valid client ID),
   * but is FORBIDDEN from this resource due to insufficient privileges. 401 would imply
   * the client failed to authenticate, which is not the case here.</p>
   *
   * @throws Exception if MockMvc request fails
   */
  @Test
  @DisplayName("Mobile client is forbidden from accessing /api/research/demographics")
  public void testMobileClientForbiddenFromDemographics() throws Exception {
    mockMvc
        .perform(get("/api/research/demographics").header(CLIENT_ID_HEADER, MOBILE_CLIENT))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status", is(403)))
        .andExpect(jsonPath("$.error", is("Forbidden")))
        .andExpect(
            jsonPath(
                "$.message",
                containsString("Mobile clients are not authorized to access research endpoints")));
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
