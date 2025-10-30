package com.teamx.fitness.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamx.fitness.security.ClientContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for {@link ApiLoggingInterceptor} to ensure logging works for
 * valid, invalid and atypical inputs.
 */
class ApiLoggingInterceptorTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @AfterEach
  void cleanup() {
    ClientContext.clear();
  }

  @Test
  void validRequest_isLogged() throws Exception {
    ApiLoggingInterceptor interceptor = new ApiLoggingInterceptor();
    Logger logger = (Logger) LoggerFactory.getLogger("API_LOG");
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/persons/1");
    req.addHeader("User-Agent", "JUnit");
    req.setRemoteAddr("127.0.0.1");
    MockHttpServletResponse res = new MockHttpServletResponse();
    res.setStatus(200);
    ClientContext.setClientId("mobile-app1");

    interceptor.preHandle(req, res, new Object());
    interceptor.afterCompletion(req, res, new Object(), null);

    List<ILoggingEvent> events = appender.list;
    Assertions.assertFalse(events.isEmpty(), "No log events captured");
    String msg = events.get(0).getFormattedMessage();

    Map<String, Object> json = mapper.readValue(msg, new TypeReference<Map<String, Object>>() {});
    Assertions.assertEquals("mobile-app1", json.get("clientId"));
    Assertions.assertEquals("GET", json.get("method"));
    Assertions.assertEquals(200, json.get("status"));
  }

  @Test
  void invalidRequest_isLoggedWith400AndError() throws Exception {
    ApiLoggingInterceptor interceptor = new ApiLoggingInterceptor();
    Logger logger = (Logger) LoggerFactory.getLogger("API_LOG");
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/persons");
    req.addHeader("User-Agent", "JUnit");
    MockHttpServletResponse res = new MockHttpServletResponse();
    res.setStatus(400);
    ClientContext.setClientId("mobile-app1");

    interceptor.preHandle(req, res, new Object());
    interceptor.afterCompletion(req, res, new Object(), new RuntimeException("Invalid input"));

    String msg = ((ListAppender<ILoggingEvent>) ((Logger) LoggerFactory.getLogger("API_LOG")).getAppender(appender.getName()) == null
        ? appender.list.get(0).getFormattedMessage()
        : appender.list.get(0).getFormattedMessage());
    Map<String, Object> json = mapper.readValue(msg, new TypeReference<Map<String, Object>>() {});
    Assertions.assertEquals(400, json.get("status"));
    Assertions.assertTrue(msg.contains("error"));
  }

  @Test
  void atypicalUserAgent_isTrimmed() throws Exception {
    ApiLoggingInterceptor interceptor = new ApiLoggingInterceptor();
    Logger logger = (Logger) LoggerFactory.getLogger("API_LOG");
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    String longUa = "A".repeat(500);
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/persons");
    req.addHeader("User-Agent", longUa);
    MockHttpServletResponse res = new MockHttpServletResponse();
    res.setStatus(200);
    ClientContext.setClientId("mobile-app1");

    interceptor.preHandle(req, res, new Object());
    interceptor.afterCompletion(req, res, new Object(), null);

    String msg = appender.list.get(0).getFormattedMessage();
    Map<String, Object> json = mapper.readValue(msg, new TypeReference<Map<String, Object>>() {});
    String ua = (String) json.get("ua");
    Assertions.assertNotNull(ua);
    Assertions.assertTrue(ua.length() <= 200, "User-Agent should be trimmed to <= 200 characters");
  }
}

