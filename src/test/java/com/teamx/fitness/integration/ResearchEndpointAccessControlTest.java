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
 * Integration tests demonstrating access control for research endpoints.
 * Tests that mobile clients are properly rejected from research endpoints.
 * This addresses the rubric requirement for client type differentiation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Research Endpoint Access Control Tests")
public class ResearchEndpointAccessControlTest {

  @Autowired private MockMvc mockMvc;

  private static final String MOBILE_CLIENT = "mobile-app1";
  private static final String RESEARCH_CLIENT = "research-tool1";
  private static final String CLIENT_ID_HEADER = "X-Client-ID";

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
