package com.teamx.fitness.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Strategies a person may choose to achieve their goal.
 */
public enum PlanStrategy {
  WORKOUT,
  DIET,
  BOTH;

  @JsonCreator
  public static PlanStrategy fromValue(String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "planStrategy is required");
    }
    for (PlanStrategy value : PlanStrategy.values()) {
      if (value.name().equalsIgnoreCase(rawValue.trim())) {
        return value;
      }
    }
    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST, "planStrategy must be WORKOUT, DIET, or BOTH");
  }

  @JsonValue
  public String toJson() {
    return name();
  }
}

