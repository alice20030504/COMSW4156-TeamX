/**
 * <p>Part of the Fitness Management Service.</p>
 *
 * <p>This interceptor validates client authentication through the {@code X-Client-ID} header
 * before processing API requests, ensuring client-specific data isolation.</p>
 *
 * <p>Applies to all API endpoints except Swagger UI and health check paths.</p>
 *
 * @checkstyle 2025-10-21 by alice
 * @version 1.0
 */
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
    // Allow OPTIONS requests for CORS preflight
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    
    // Skip validation for health check, Swagger UI, actuator endpoints, and open person creation
    String requestUri = request.getRequestURI();
    if (requestUri.startsWith("/swagger-ui")
        || requestUri.startsWith("/v3/api-docs")
        || requestUri.startsWith("/actuator")
        || "/health".equals(requestUri)
        || "/".equals(requestUri)) {
      return true;
    }

    if ("POST".equalsIgnoreCase(request.getMethod())
        && ("/api/persons".equals(requestUri) || "/api/persons/".equals(requestUri))) {
      return true;
    }

    if ("POST".equalsIgnoreCase(request.getMethod())
        && ("/api/research".equals(requestUri) || "/api/research/".equals(requestUri))) {
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
