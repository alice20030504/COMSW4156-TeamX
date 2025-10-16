package com.teamx.fitness.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for HomeController navigation and redirect functionality.
 *
 * <p>This test class validates the root URL ("/") redirect behavior, which is the first
 * entry point users encounter when accessing the application. The HomeController serves
 * as a navigation helper that automatically directs users to the Swagger UI documentation
 * interface, improving API discoverability and user experience.</p>
 *
 * <p><strong>Why This Controller Matters:</strong></p>
 * <ul>
 *   <li><strong>First Impression:</strong> The root URL is typically the first thing users
 *       access, and a proper redirect prevents 404 errors</li>
 *   <li><strong>API Documentation Access:</strong> Redirecting to Swagger UI immediately
 *       presents users with interactive API documentation</li>
 *   <li><strong>Developer Experience:</strong> Makes the API self-documenting without
 *       requiring users to remember the Swagger UI path</li>
 *   <li><strong>Production Readiness:</strong> A working root endpoint is often expected
 *       by deployment health checks and monitoring systems</li>
 * </ul>
 *
 * <p><strong>Testing Strategy:</strong></p>
 * <ul>
 *   <li>Verify correct HTTP redirect status code (302 Found)</li>
 *   <li>Validate redirect target URL points to Swagger UI</li>
 *   <li>Ensure no service dependencies or business logic complications</li>
 * </ul>
 *
 * <p><strong>Technical Details:</strong> This is a stateless controller with no dependencies
 * on services or databases, making it ideal for pure controller layer testing without mocks.</p>
 *
 * @see HomeController
 */
@WebMvcTest(HomeController.class)
@DisplayName("HomeController Navigation Tests")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Tests that accessing the root URL ("/") redirects to Swagger UI documentation.
     *
     * <p>This test verifies the primary function of HomeController: providing seamless
     * navigation from the application root to the interactive API documentation. This
     * redirect is crucial for API usability, as it:</p>
     *
     * <ul>
     *   <li><strong>Prevents 404 Errors:</strong> Without this redirect, accessing "/"
     *       would result in a Whitelabel Error Page since no view is mapped to the root</li>
     *   <li><strong>Self-Documenting API:</strong> Users can immediately explore all
     *       endpoints, request/response schemas, and try API calls interactively</li>
     *   <li><strong>Development Productivity:</strong> Developers can quickly access
     *       API documentation during development without memorizing paths</li>
     *   <li><strong>Professional Polish:</strong> A working root endpoint conveys a
     *       well-configured, production-ready service</li>
     * </ul>
     *
     * <p><strong>HTTP Redirect Mechanics:</strong> The test validates:</p>
     * <ul>
     *   <li><strong>Status Code 302:</strong> "Found" (temporary redirect), indicating
     *       the client should follow the redirect but continue using "/" for future requests</li>
     *   <li><strong>Location Header:</strong> Contains "/swagger-ui.html", the Swagger UI entry point</li>
     * </ul>
     *
     * <p><strong>Why Not 301 (Permanent Redirect)?</strong> A 302 is used instead of 301
     * because the redirect target might change in different deployment environments (e.g.,
     * production might redirect to a custom landing page), and clients shouldn't cache
     * the redirect permanently.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("home - Redirects root URL to Swagger UI with 302 status")
    void testHome_RedirectsToSwaggerUI() throws Exception {
        // Given: HomeController is configured to redirect to Swagger UI

        // When: GET request to root URL "/"
        // Then: Returns 302 Found with redirect to /swagger-ui.html
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())  // Verify redirect status (3xx)
                .andExpect(status().isFound())           // Specifically 302 Found
                .andExpect(redirectedUrl("/swagger-ui.html"));  // Verify target URL
    }

}
