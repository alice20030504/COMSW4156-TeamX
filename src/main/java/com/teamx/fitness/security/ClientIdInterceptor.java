package com.teamx.fitness.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for validating client authentication via X-Client-ID header.
 * Extracts and validates the client ID, storing it in thread-local context.
 */
@Component
public class ClientIdInterceptor implements HandlerInterceptor {
  /**
   * HTTP header name used to pass the client ID in requests.
   */
  public static final String CLIENT_ID_HEADER = "X-Client-ID";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // Skip validation for health check, Swagger UI, and actuator endpoints
    String requestUri = request.getRequestURI();
    if (requestUri.startsWith("/swagger-ui")
        || requestUri.startsWith("/v3/api-docs")
        || requestUri.startsWith("/actuator")
        || requestUri.equals("/")) {
      return true;
    }

    String clientId = request.getHeader(CLIENT_ID_HEADER);

    if (clientId == null || clientId.isBlank()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType("application/json");
      response
          .getWriter()
          .write(
              "{\"error\":\"Bad Request\",\"message\":\"X-Client-ID header is required\","
                  + "\"status\":400}");
      return false;
    }

    if (!ClientContext.isValidClientId(clientId)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType("application/json");
      response
          .getWriter()
          .write(
              "{\"error\":\"Bad Request\",\"message\":\"Invalid client ID format. Must start with"
                  + " 'mobile-' or 'research-'\",\"status\":400}");
      return false;
    }

    ClientContext.setClientId(clientId);
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    ClientContext.clear();
  }
}
