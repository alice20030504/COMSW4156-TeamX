package com.teamx.fitness.controller.dto;

import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.Gender;
import com.teamx.fitness.model.PlanStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/**
 * Request payload for generating a meal plan.
 * Contains all user profile and goal plan information needed for meal plan generation.
 */
public class MealPlanRequest {

  /** Display name of the user. */
  @NotBlank(message = "name is required")
  private String name;

  /** Body weight in kilograms. */
  @NotNull(message = "weightKg is required")
  @Positive(message = "weightKg must be greater than 0")
  private Double weightKg;

  /** Height in centimeters. */
  @NotNull(message = "heightCm is required")
  @Positive(message = "heightCm must be greater than 0")
  private Double heightCm;

  /** Date of birth used for age computations. */
  @NotNull(message = "birthDate is required")
  @Past(message = "birthDate must be in the past")
  private LocalDate birthDate;

  /** Gender of the person. */
  @NotNull(message = "gender is required")
  private Gender gender;

  /** Fitness goal (CUT or BULK). */
  @NotNull(message = "goal is required")
  private FitnessGoal goal;

  /** Target change in kilograms for the plan. */
  @NotNull(message = "targetChangeKg is required")
  @Positive(message = "targetChangeKg must be greater than 0")
  private Double targetChangeKg;

  /** Number of weeks allotted to reach the target. */
  @NotNull(message = "durationWeeks is required")
  @Positive(message = "durationWeeks must be greater than 0")
  private Integer durationWeeks;

  /** Weekly training sessions supporting the plan. */
  @NotNull(message = "trainingFrequencyPerWeek is required")
  @Positive(message = "trainingFrequencyPerWeek must be greater than 0")
  private Integer trainingFrequencyPerWeek;

  /** Strategy (workout, diet, or both) used to execute the plan. */
  @NotNull(message = "planStrategy is required")
  private PlanStrategy planStrategy;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getWeightKg() {
    return weightKg;
  }

  public void setWeightKg(Double weightKg) {
    this.weightKg = weightKg;
  }

  public Double getHeightCm() {
    return heightCm;
  }

  public void setHeightCm(Double heightCm) {
    this.heightCm = heightCm;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public FitnessGoal getGoal() {
    return goal;
  }

  public void setGoal(FitnessGoal goal) {
    this.goal = goal;
  }

  public Double getTargetChangeKg() {
    return targetChangeKg;
  }

  public void setTargetChangeKg(Double targetChangeKg) {
    this.targetChangeKg = targetChangeKg;
  }

  public Integer getDurationWeeks() {
    return durationWeeks;
  }

  public void setDurationWeeks(Integer durationWeeks) {
    this.durationWeeks = durationWeeks;
  }

  public Integer getTrainingFrequencyPerWeek() {
    return trainingFrequencyPerWeek;
  }

  public void setTrainingFrequencyPerWeek(Integer trainingFrequencyPerWeek) {
    this.trainingFrequencyPerWeek = trainingFrequencyPerWeek;
  }

  public PlanStrategy getPlanStrategy() {
    return planStrategy;
  }

  public void setPlanStrategy(PlanStrategy planStrategy) {
    this.planStrategy = planStrategy;
  }
}
