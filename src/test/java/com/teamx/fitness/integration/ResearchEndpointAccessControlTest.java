package com.teamx.fitness.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

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
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Research Endpoint Access Control Tests")
public class ResearchEndpointAccessControlTest {

  @Autowired private MockMvc mockMvc;

  private static final String MOBILE_CLIENT = "mobile-app1";
  private static final String RESEARCH_CLIENT = "research-tool1";
  private static final String CLIENT_ID_HEADER = "X-Client-ID";

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
  }

  @Test
  @DisplayName("Mobile client is forbidden from accessing /api/research/workout-patterns")
  public void testMobileClientForbiddenFromWorkoutPatterns() throws Exception {
    mockMvc
        .perform(get("/api/research/workout-patterns").header(CLIENT_ID_HEADER, MOBILE_CLIENT))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status", is(403)))
        .andExpect(jsonPath("$.error", is("Forbidden")))
        .andExpect(
            jsonPath(
                "$.message",
                containsString("Mobile clients are not authorized to access research endpoints")));
  }

  @Test
  @DisplayName("Mobile client is forbidden from accessing /api/research/nutrition-trends")
  public void testMobileClientForbiddenFromNutritionTrends() throws Exception {
    mockMvc
        .perform(get("/api/research/nutrition-trends").header(CLIENT_ID_HEADER, MOBILE_CLIENT))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status", is(403)))
        .andExpect(jsonPath("$.error", is("Forbidden")))
        .andExpect(
            jsonPath(
                "$.message",
                containsString("Mobile clients are not authorized to access research endpoints")));
  }

  @Test
  @DisplayName("Mobile client is forbidden from accessing /api/research/population-health")
  public void testMobileClientForbiddenFromPopulationHealth() throws Exception {
    mockMvc
        .perform(get("/api/research/population-health").header(CLIENT_ID_HEADER, MOBILE_CLIENT))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status", is(403)))
        .andExpect(jsonPath("$.error", is("Forbidden")))
        .andExpect(
            jsonPath(
                "$.message",
                containsString("Mobile clients are not authorized to access research endpoints")));
  }

  @Test
  @DisplayName("Research client can access /api/research/demographics")
  public void testResearchClientCanAccessDemographics() throws Exception {
    mockMvc
        .perform(get("/api/research/demographics").header(CLIENT_ID_HEADER, RESEARCH_CLIENT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.dataAnonymized", is(true)))
        .andExpect(jsonPath("$.privacyCompliant", is(true)));
  }

  @Test
  @DisplayName("Research client can access /api/research/workout-patterns")
  public void testResearchClientCanAccessWorkoutPatterns() throws Exception {
    mockMvc
        .perform(get("/api/research/workout-patterns").header(CLIENT_ID_HEADER, RESEARCH_CLIENT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.privacyProtected", is(true)));
  }

  @Test
  @DisplayName("Research client can access /api/research/nutrition-trends")
  public void testResearchClientCanAccessNutritionTrends() throws Exception {
    mockMvc
        .perform(get("/api/research/nutrition-trends").header(CLIENT_ID_HEADER, RESEARCH_CLIENT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.dataType", is("AGGREGATED")))
        .andExpect(jsonPath("$.containsPII", is(false)));
  }

  @Test
  @DisplayName("Research client can access /api/research/population-health")
  public void testResearchClientCanAccessPopulationHealth() throws Exception {
    mockMvc
        .perform(get("/api/research/population-health").header(CLIENT_ID_HEADER, RESEARCH_CLIENT))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.dataProtection", is("All data is aggregated and anonymized")));
  }

  @Test
  @DisplayName("Multiple different mobile clients are all forbidden from research endpoints")
  public void testMultipleMobileClientsForbiddenFromResearch() throws Exception {
    String[] mobileClients = {"mobile-app1", "mobile-app2", "mobile-test", "mobile-beta"};

    for (String clientId : mobileClients) {
      mockMvc
          .perform(get("/api/research/demographics").header(CLIENT_ID_HEADER, clientId))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.status", is(403)));
    }
  }

  @Test
  @DisplayName("Multiple research clients can all access research endpoints")
  public void testMultipleResearchClientsCanAccessResearch() throws Exception {
    String[] researchClients = {"research-tool1", "research-tool2", "research-analytics"};

    for (String clientId : researchClients) {
      mockMvc
          .perform(get("/api/research/demographics").header(CLIENT_ID_HEADER, clientId))
          .andExpect(status().isOk());
    }
  }

  @Test
  @DisplayName("Mobile client can still access /api/persons endpoints")
  public void testMobileClientCanAccessPersonEndpoints() throws Exception {
    mockMvc
        .perform(get("/api/persons").header(CLIENT_ID_HEADER, MOBILE_CLIENT))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Research client can still access /api/persons endpoints")
  public void testResearchClientCanAccessPersonEndpoints() throws Exception {
    mockMvc
        .perform(get("/api/persons").header(CLIENT_ID_HEADER, RESEARCH_CLIENT))
        .andExpect(status().isOk());
  }
}
