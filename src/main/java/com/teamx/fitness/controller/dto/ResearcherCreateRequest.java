package com.teamx.fitness.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating a new researcher profile.
 */
public class ResearcherCreateRequest {

  /** Name of the researcher. */
  @NotBlank(message = "name is required")
  private String name;

  /** Email address of the researcher. */
  @NotBlank(message = "email is required")
  @Email(message = "email must be valid")
  private String email;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}

