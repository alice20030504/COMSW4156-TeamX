package com.teamx.fitness.controller.dto;

/**
 * Response payload returned after creating a person profile.
 */
public class PersonCreatedResponse {

  /** Identifier mobile clients use for subsequent API calls. */
  private final String clientId;

  public PersonCreatedResponse(String clientId) {
    this.clientId = clientId;
  }

  public String getClientId() {
    return clientId;
  }
}
