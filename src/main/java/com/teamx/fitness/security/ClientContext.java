package com.teamx.fitness.security;

/**
 * Thread-local storage for client authentication context.
 * Stores and validates client IDs for the current request thread.
 */
public class ClientContext {

  public static final String MOBILE_PREFIX = "mobile-";
  public static final String RESEARCH_PREFIX = "research-";

  private static final ThreadLocal<String> clientIdHolder = new ThreadLocal<>();

  /**
   * Sets the client ID for the current thread.
   *
   * @param clientId the client ID to set
   */
  public static void setClientId(String clientId) {
    clientIdHolder.set(clientId);
  }

  /**
   * Gets the client ID for the current thread.
   *
   * @return the client ID, or null if not set
   */
  public static String getClientId() {
    return clientIdHolder.get();
  }

  /**
   * Clears the client ID for the current thread.
   * Should be called after request processing is complete.
   */
  public static void clear() {
    clientIdHolder.remove();
  }

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
