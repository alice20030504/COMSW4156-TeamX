package com.teamx.fitness.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies the small utility logic inside {@link ClientContext}.
 */
@DisplayName("ClientContext thread local behaviour")
class ClientContextTest {

  @AfterEach
  void cleanup() {
    ClientContext.clear();
  }

  @Test
  @DisplayName("set/get/clear manage thread local client id")
  void managesThreadLocalClientId() {
    assertNull(ClientContext.getClientId());
    ClientContext.setClientId("mobile-app1");
    assertEquals("mobile-app1", ClientContext.getClientId());
    ClientContext.clear();
    assertNull(ClientContext.getClientId());
  }

  @Test
  @DisplayName("validates acceptable client id prefixes")
  void validatesPrefixes() {
    assertTrue(ClientContext.isValidClientId("mobile-abc"));
    assertTrue(ClientContext.isValidClientId("research-xyz"));
    assertFalse(ClientContext.isValidClientId("unknown"));
    assertFalse(ClientContext.isValidClientId(" "));
    assertFalse(ClientContext.isValidClientId(null));
  }

  @Test
  @DisplayName("detects mobile vs research clients")
  void identifiesClientTypes() {
    assertTrue(ClientContext.isMobileClient("mobile-tester"));
    assertFalse(ClientContext.isMobileClient("research-analyst"));
    assertTrue(ClientContext.isResearchClient("research-analyst"));
    assertFalse(ClientContext.isResearchClient("mobile-tester"));
  }
}
