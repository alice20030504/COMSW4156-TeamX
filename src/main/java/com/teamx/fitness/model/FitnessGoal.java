package com.teamx.fitness.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Supported high-level fitness goals for a person profile.
 */
public enum FitnessGoal {
  CUT,
  BULK;

  /**
   * Creates an enum value from a case-insensitive string.
   *
   * @param rawValue incoming string value
   * @return matching {@link FitnessGoal}
   */
  @JsonCreator
  public static FitnessGoal fromValue(String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "goal is required");
    }
    for (FitnessGoal value : values()) {
      if (value.name().equalsIgnoreCase(rawValue.trim())) {
        return value;
      }
    }
    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST, "goal must be one of: CUT, BULK");
  }

  @JsonValue
  public String toJson() {
    return name();
  }
}
