package com.teamx.fitness.util;

public class ClientValidator {

  public static final String MOBILE_PREFIX = "mobile-";
  public static final String RESEARCH_PREFIX = "research-";

  /**
   * Validates if a client ID is in the correct format.
   * Valid formats: "mobile-*" or "research-*"
   *
   * @param clientId the client ID to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValidClientId(String clientId) {
    if (clientId == null || clientId.isBlank()) {
      return false;
    }
    return isMobileClient(clientId) || isResearchClient(clientId);
  }

  /**
   * Checks if a client ID represents a mobile client.
   *
   * @param clientId the client ID to check
   * @return true if it's a mobile client, false otherwise
   */
  public static boolean isMobileClient(String clientId) {
    return clientId != null && clientId.startsWith(MOBILE_PREFIX);
  }

  /**
   * Checks if a client ID represents a research client.
   *
   * @param clientId the client ID to check
   * @return true if it's a research client, false otherwise
   */
  public static boolean isResearchClient(String clientId) {
    return clientId != null && clientId.startsWith(RESEARCH_PREFIX);
  }
}
