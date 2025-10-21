/**
 * <p>Part of the Fitness Management Service.</p>
 *
 * <p>This utility class manages thread-local storage for client authentication context,
 * allowing each request thread to store and retrieve its own {@code clientId} safely.</p>
 *
 * <p>Used by interceptors to validate client identity for each API request.</p>
 *
 * @checkstyle 2025-10-21 by alice
 * @version 1.0
 */

package com.teamx.fitness.security;

/**
 * Thread-local storage for client authentication context.
 * Stores and validates client IDs for the current request thread.
 */
public final class ClientContext {

  /** Prefix for mobile clients' IDs. */
  public static final String MOBILE_PREFIX = "mobile-";

  /** Prefix for research clients' IDs. */
  public static final String RESEARCH_PREFIX = "research-";

  /** Thread-local holder for the current thread's client ID. */
  private static final ThreadLocal<String> CLIENT_ID_HOLDER = new ThreadLocal<>();

  // Private constructor to prevent instantiation of utility class
  private ClientContext() { }

  /**
   * Sets the client ID for the current thread.
   *
   * @param clientId the client ID to set
   */
  public static void setClientId(String clientId) {
    CLIENT_ID_HOLDER.set(clientId);
  }

  /**
   * Gets the client ID for the current thread.
   *
   * @return the client ID, or null if not set
   */
  public static String getClientId() {
    return CLIENT_ID_HOLDER.get();
  }

  /**
   * Clears the client ID for the current thread.
   * Should be called after request processing is complete.
   */
  public static void clear() {
    CLIENT_ID_HOLDER.remove();
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
