package com.teamx.fitness.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Supported gender values for person profiles.
 */
public enum Gender {
  MALE,
  FEMALE;

  /**
   * Parses a case-insensitive value into a {@link Gender}.
   *
   * @param rawValue incoming raw value
   * @return parsed gender
   */
  @JsonCreator
  public static Gender fromValue(String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gender is required");
    }
    for (Gender gender : values()) {
      if (gender.name().equalsIgnoreCase(rawValue.trim())) {
        return gender;
      }
    }
    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST, "gender must be either MALE or FEMALE");
  }

  @JsonValue
  public String toJson() {
    return name();
  }
}
