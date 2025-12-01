package com.teamx.fitness.controller.dto;

/**
 * Response payload for meal plan generation.
 * Matches the JSON structure returned by the Python mealplan service.
 */
public class MealPlanResponse {

  /** The generated meal plan text. */
  private String mealPlan;

  public MealPlanResponse() {
  }

  public MealPlanResponse(String mealPlan) {
    this.mealPlan = mealPlan;
  }

  public String getMealPlan() {
    return mealPlan;
  }

  public void setMealPlan(String mealPlan) {
    this.mealPlan = mealPlan;
  }
}
