package com.teamx.fitness.controller.dto;

import com.teamx.fitness.model.PlanStrategy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload for capturing goal-specific planning details.
 */
public class GoalPlanRequest {

  /** Target weight in kilograms for the plan. */
  @NotNull(message = "targetChangeKg is required")
  @Positive(message = "targetChangeKg must be greater than 0")
  private Double targetChangeKg;

  /** Number of weeks allotted to reach the target. */
  @NotNull(message = "durationWeeks is required")
  @Positive(message = "durationWeeks must be greater than 0")
  private Integer durationWeeks;

  /** Weekly training sessions supporting the plan. */
  @NotNull(message = "trainingFrequencyPerWeek is required")
  @Min(value = 1, message = "trainingFrequencyPerWeek must be at least 1")
  @Max(value = 14, message = "trainingFrequencyPerWeek must not exceed 14")
  private Integer trainingFrequencyPerWeek;

  /** Strategy (workout, diet, or both) used to execute the plan. */
  @NotNull(message = "planStrategy is required")
  private PlanStrategy planStrategy;

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
