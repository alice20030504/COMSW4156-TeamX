package com.teamx.fitness.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Exercises {@link ClientIdInterceptor} to cover bypass routes, invalid requests, and valid header
 * processing using mock servlet infrastructure.
 */
@DisplayName("ClientIdInterceptor header validation")
class ClientIdInterceptorTest {

  /** HTTP 200 status used when requests are allowed through. */
  private static final int STATUS_OK = 200;
  /** HTTP 400 status returned for rejected requests. */
  private static final int STATUS_BAD_REQUEST = 400;

  /** Interceptor under test. */
  private final ClientIdInterceptor interceptor = new ClientIdInterceptor();

  @AfterEach
  void clearContext() {
    ClientContext.clear();
  }

  /**
   * Boundary scenario: swagger routes should bypass validation entirely.
   */
  @Test
  @DisplayName("Requests for swagger docs bypass validation")
  void preHandleSkipsSwaggerRoutes() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean allowed = interceptor.preHandle(request, response, new Object());

    assertTrue(allowed);
    assertEquals(STATUS_OK, response.getStatus());
  }

  @Test
  @DisplayName("OPTIONS preflight bypasses validation")
  void preHandleAllowsOptionsRequests() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/persons");
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertTrue(interceptor.preHandle(request, response, new Object()));
  }

  @Test
  @DisplayName("Health and root endpoints bypass validation")
  void preHandleAllowsHealthAndRoot() throws Exception {
    MockHttpServletRequest health = new MockHttpServletRequest("GET", "/health");
    MockHttpServletRequest root = new MockHttpServletRequest("GET", "/");
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertTrue(interceptor.preHandle(health, response, new Object()));
    assertTrue(interceptor.preHandle(root, response, new Object()));
  }

  @Test
  @DisplayName("Open POST endpoints bypass validation")
  void preHandleAllowsOpenPostEndpoints() throws Exception {
    MockHttpServletRequest createPerson = new MockHttpServletRequest("POST", "/api/persons");
    MockHttpServletRequest createResearch = new MockHttpServletRequest("POST", "/api/research");
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertTrue(interceptor.preHandle(createPerson, response, new Object()));
    assertTrue(interceptor.preHandle(createResearch, response, new Object()));
  }

  @Test
  @DisplayName("POST endpoints with trailing slashes bypass validation")
  void preHandleAllowsTrailingSlashEndpoints() throws Exception {
    MockHttpServletRequest createPerson = new MockHttpServletRequest("POST", "/api/persons/");
    MockHttpServletRequest createResearch = new MockHttpServletRequest("POST", "/api/research/");
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertTrue(interceptor.preHandle(createPerson, response, new Object()));
    assertTrue(interceptor.preHandle(createResearch, response, new Object()));
  }

  /**
   * Invalid scenario: missing header triggers a 400 and stops the handler chain.
   */
  @Test
  @DisplayName("Missing client header returns 400 and blocks request")
  void preHandleMissingHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/persons");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean allowed = interceptor.preHandle(request, response, new Object());

    assertFalse(allowed);
    assertEquals(STATUS_BAD_REQUEST, response.getStatus());
  }

  /**
   * Invalid scenario: malformed client IDs are rejected with a 400 status.
   */
  @Test
  @DisplayName("Invalid client format returns 400 and blocks request")
  void preHandleInvalidFormat() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/persons");
    request.addHeader(ClientIdInterceptor.CLIENT_ID_HEADER, "invalid-client");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean allowed = interceptor.preHandle(request, response, new Object());

    assertFalse(allowed);
    assertEquals(STATUS_BAD_REQUEST, response.getStatus());
  }

  /**
   * Valid scenario: well-formed client headers are stored and allow the request to proceed.
   */
  @Test
  @DisplayName("Valid client ID is stored and allows request to proceed")
  void preHandleValidClient() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/persons");
    request.addHeader(ClientIdInterceptor.CLIENT_ID_HEADER, "mobile-app1");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean allowed = interceptor.preHandle(request, response, new Object());

    assertTrue(allowed);
    assertEquals("mobile-app1", ClientContext.getClientId());

    interceptor.afterCompletion(request, response, new Object(), null);
    assertEquals(null, ClientContext.getClientId());
  }

  @Test
  @DisplayName("Research client IDs are accepted and stored")
  void preHandleValidResearchClient() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/research/data");
    request.addHeader(ClientIdInterceptor.CLIENT_ID_HEADER, "research-analyst1");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean allowed = interceptor.preHandle(request, response, new Object());

    assertTrue(allowed);
    assertEquals("research-analyst1", ClientContext.getClientId());
  }
}
