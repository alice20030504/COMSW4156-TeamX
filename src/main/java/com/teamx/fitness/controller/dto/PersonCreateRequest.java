package com.teamx.fitness.controller.dto;

import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

/**
 * Request payload for creating a new person profile.
 */
public class PersonCreateRequest {

  @NotBlank(message = "name is required")
  private String name;

  @NotNull(message = "weight is required")
  private Double weight;

  @NotNull(message = "height is required")
  private Double height;

  @NotNull(message = "birthDate is required")
  @Past(message = "birthDate must be in the past")
  private LocalDate birthDate;

  @NotNull(message = "goal is required")
  private FitnessGoal goal;

  /** Gender for the person profile. */
  @NotNull(message = "gender is required")
  private Gender gender;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getWeight() {
    return weight;
  }

  public void setWeight(Double weight) {
    this.weight = weight;
  }

  public Double getHeight() {
    return height;
  }

  public void setHeight(Double height) {
    this.height = height;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public FitnessGoal getGoal() {
    return goal;
  }

  public void setGoal(FitnessGoal goal) {
    this.goal = goal;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }
}
