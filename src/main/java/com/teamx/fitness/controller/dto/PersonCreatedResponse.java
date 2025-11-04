package com.teamx.fitness.controller.dto;

/**
 * Response payload returned after creating a person profile.
 */
public class PersonCreatedResponse {

  private final String clientId;

  public PersonCreatedResponse(String clientId) {
    this.clientId = clientId;
  }

  public String getClientId() {
    return clientId;
  }
}

