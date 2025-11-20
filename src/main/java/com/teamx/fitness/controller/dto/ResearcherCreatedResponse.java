package com.teamx.fitness.controller.dto;

/**
 * Response payload for researcher registration.
 */
public class ResearcherCreatedResponse {

  /** Generated client ID for the researcher. */
  private String clientId;

  public ResearcherCreatedResponse(String clientId) {
    this.clientId = clientId;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }
}

