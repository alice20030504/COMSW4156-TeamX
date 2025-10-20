package com.teamx.fitness.context;

public class ClientContext {

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
}
