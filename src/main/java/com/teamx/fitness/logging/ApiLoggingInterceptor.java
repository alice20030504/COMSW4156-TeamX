package com.teamx.fitness.logging;

import com.teamx.fitness.security.ClientContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that logs basic API call information to the application log file.
 * Captures: timestamp (via logger pattern), clientId, method, path, status, duration, ip, userAgent.
 */
@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

  /** Logger used for structured API request logging. */
  private static final Logger LOG = LoggerFactory.getLogger("API_LOG");

  /** Request attribute key storing the request start timestamp in milliseconds. */
  private static final String START_TIME_ATTR = "apiLogStartTime";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      Exception ex) throws Exception {

    Long start = (Long) request.getAttribute(START_TIME_ATTR);
    long duration = start != null ? System.currentTimeMillis() - start : -1L;

    String path = request.getRequestURI();
    String method = request.getMethod();
    int status = response.getStatus();
    String clientId = ClientContext.getClientId();
    String ip = getClientIp(request);
    String ua = trimUserAgent(request.getHeader("User-Agent"));
    String err = ex != null ? ex.getMessage() : null;

    // JSON-ish single-line log for easy parsing
    String msg = String.format(
        "{\"clientId\":%s,\"method\":\"%s\",\"path\":\"%s\",\"status\":%d,\"durationMs\":%d,\"ip\":%s,\"ua\":%s%s}",
        jsonQuoteOrNull(clientId), method, path, status, duration,
        jsonQuoteOrNull(ip), jsonQuoteOrNull(ua),
        err != null ? ",\"error\":" + jsonQuote(err) : "");

    LOG.info(msg);
  }

  private static String getClientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      int comma = xff.indexOf(',');
      return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
    }
    return request.getRemoteAddr();
  }

  private static String trimUserAgent(String ua) {
    if (ua == null) {
      return null;
    }
    return ua.length() > 200 ? ua.substring(0, 200) : ua;
  }

  private static String jsonQuote(String s) {
    return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }

  private static String jsonQuoteOrNull(String s) {
    return s == null ? "null" : jsonQuote(s);
  }
}
