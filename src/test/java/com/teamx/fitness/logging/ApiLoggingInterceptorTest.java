package com.teamx.fitness.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamx.fitness.security.ClientContext;
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

  /** Logger name used by the interceptor. */
  private static final String LOGGER_NAME = "API_LOG";
  /** HTTP 200 status code. */
  private static final int STATUS_OK = 200;
  /** HTTP 400 status code. */
  private static final int STATUS_BAD_REQUEST = 400;
  /** Maximum allowed user-agent length. */
  private static final int UA_MAX_LENGTH = 200;
  /** Length for the oversized user-agent sample. */
  private static final int UA_SAMPLE_LENGTH = 500;
  /** Sentinel duration value when start time missing. */
  private static final int NEGATIVE_DURATION = -1;

  /** JSON mapper used to inspect structured log output. */
  private final ObjectMapper mapper = new ObjectMapper();

  @AfterEach
  void cleanup() {
    ClientContext.clear();
  }

  @Test
  void validRequestIsLogged() throws Exception {
    ApiLoggingInterceptor interceptor = new ApiLoggingInterceptor();
    Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/persons/1");
    req.addHeader("User-Agent", "JUnit");
    req.setRemoteAddr("127.0.0.1");
    MockHttpServletResponse res = new MockHttpServletResponse();
    res.setStatus(STATUS_OK);
    ClientContext.setClientId("mobile-app1");

    interceptor.preHandle(req, res, new Object());
    interceptor.afterCompletion(req, res, new Object(), null);

    List<ILoggingEvent> events = appender.list;
    Assertions.assertFalse(events.isEmpty(), "No log events captured");
    String msg = events.get(0).getFormattedMessage();

    Map<String, Object> json =
        mapper.readValue(msg, new TypeReference<Map<String, Object>>() { });
    Assertions.assertEquals("mobile-app1", json.get("clientId"));
    Assertions.assertEquals("GET", json.get("method"));
    Assertions.assertEquals(STATUS_OK, json.get("status"));
  }

  @Test
  void invalidRequestIsLoggedWith400AndError() throws Exception {
    ApiLoggingInterceptor interceptor = new ApiLoggingInterceptor();
    Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/persons");
    req.addHeader("User-Agent", "JUnit");
    MockHttpServletResponse res = new MockHttpServletResponse();
    res.setStatus(STATUS_BAD_REQUEST);
    ClientContext.setClientId("mobile-app1");

    interceptor.preHandle(req, res, new Object());
    interceptor.afterCompletion(req, res, new Object(), new RuntimeException("Invalid input"));

    String msg = appender.list.get(0).getFormattedMessage();
    Map<String, Object> json =
        mapper.readValue(msg, new TypeReference<Map<String, Object>>() { });
    Assertions.assertEquals(STATUS_BAD_REQUEST, json.get("status"));
    Assertions.assertTrue(msg.contains("error"));
  }

  @Test
  void atypicalUserAgentIsTrimmed() throws Exception {
    ApiLoggingInterceptor interceptor = new ApiLoggingInterceptor();
    Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    String longUa = "A".repeat(UA_SAMPLE_LENGTH);
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/persons");
    req.addHeader("User-Agent", longUa);
    MockHttpServletResponse res = new MockHttpServletResponse();
    res.setStatus(STATUS_OK);
    ClientContext.setClientId("mobile-app1");

    interceptor.preHandle(req, res, new Object());
    interceptor.afterCompletion(req, res, new Object(), null);

    String msg = appender.list.get(0).getFormattedMessage();
    Map<String, Object> json =
        mapper.readValue(msg, new TypeReference<Map<String, Object>>() { });
    String ua = (String) json.get("ua");
    Assertions.assertNotNull(ua);
    Assertions.assertTrue(
        ua.length() <= UA_MAX_LENGTH, "User-Agent should be trimmed to <= 200 characters");
  }

  @Test
  void forwardedForHeaderIsPreferredForIp() throws Exception {
    ApiLoggingInterceptor interceptor = new ApiLoggingInterceptor();
    Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/persons");
    req.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
    MockHttpServletResponse res = new MockHttpServletResponse();
    res.setStatus(STATUS_OK);

    interceptor.preHandle(req, res, new Object());
    interceptor.afterCompletion(req, res, new Object(), null);

    Map<String, Object> json =
        mapper.readValue(appender.list.get(0).getFormattedMessage(), new TypeReference<>() { });
    Assertions.assertEquals("10.0.0.1", json.get("ip"));
  }

  @Test
  void missingStartTimeRecordsNegativeDuration() throws Exception {
    ApiLoggingInterceptor interceptor = new ApiLoggingInterceptor();
    Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/persons");
    MockHttpServletResponse res = new MockHttpServletResponse();
    res.setStatus(STATUS_OK);

    interceptor.afterCompletion(req, res, new Object(), null);

    Map<String, Object> json =
        mapper.readValue(appender.list.get(0).getFormattedMessage(), new TypeReference<>() { });
    Assertions.assertEquals(NEGATIVE_DURATION, json.get("durationMs"));
  }
}

