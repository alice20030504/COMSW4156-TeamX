package com.teamx.fitness.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("ClientIdInterceptor header validation")
class ClientIdInterceptorTest {

  private final ClientIdInterceptor interceptor = new ClientIdInterceptor();

  @AfterEach
  void clearContext() {
    ClientContext.clear();
  }

  @Test
  @DisplayName("Requests for swagger docs bypass validation")
  void preHandle_SkipsSwaggerRoutes() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean allowed = interceptor.preHandle(request, response, new Object());

    assertTrue(allowed);
    assertEquals(200, response.getStatus());
  }

  @Test
  @DisplayName("Missing client header returns 400 and blocks request")
  void preHandle_MissingHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/persons");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean allowed = interceptor.preHandle(request, response, new Object());

    assertFalse(allowed);
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
  }

  @Test
  @DisplayName("Invalid client format returns 400 and blocks request")
  void preHandle_InvalidFormat() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/persons");
    request.addHeader(ClientIdInterceptor.CLIENT_ID_HEADER, "invalid-client");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean allowed = interceptor.preHandle(request, response, new Object());

    assertFalse(allowed);
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
  }

  @Test
  @DisplayName("Valid client ID is stored and allows request to proceed")
  void preHandle_ValidClient() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/persons");
    request.addHeader(ClientIdInterceptor.CLIENT_ID_HEADER, "mobile-app1");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean allowed = interceptor.preHandle(request, response, new Object());

    assertTrue(allowed);
    assertEquals("mobile-app1", ClientContext.getClientId());

    interceptor.afterCompletion(request, response, new Object(), null);
    assertEquals(null, ClientContext.getClientId());
  }
}
