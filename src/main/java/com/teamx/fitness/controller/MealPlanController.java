package com.teamx.fitness.controller;

import com.teamx.fitness.controller.dto.MealPlanRequest;
import com.teamx.fitness.controller.dto.MealPlanResponse;
import com.teamx.fitness.service.MealPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for meal plan generation endpoints.
 * 
 * <p>This controller provides the POST /api/mealplan endpoint that accepts
 * user profile and goal information, then delegates to the Python mealplan
 * service to generate a personalized meal plan.</p>
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(
    name = "Meal Plan Controller",
    description = "Endpoints for generating personalized meal plans. "
        + "The backend calls the Python mealplan service to generate plans.")
public class MealPlanController {

  /** Service for generating meal plans. */
  @Autowired
  private MealPlanService mealPlanService;

  /**
   * Generate a meal plan based on user profile and goal information.
   * 
   * <p>This endpoint accepts all required user data and goal plan details,
   * then calls the Python mealplan service to generate a personalized 7-day meal plan.</p>
   *
   * @param request The meal plan request containing user profile and goal information
   * @return ResponseEntity containing the generated meal plan
   */
  @PostMapping("/mealplan")
  @Operation(
      summary = "Generate a personalized meal plan",
      description = "Generates a 7-day meal plan based on user profile (name, weight, height, "
          + "birth date, gender) and goal plan details (goal, target change, duration, "
          + "training frequency, plan strategy). The backend calls the Python mealplan service "
          + "to generate the plan.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Meal plan generated successfully",
          content = @Content(schema = @Schema(implementation = MealPlanResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "500", description = "Failed to generate meal plan")
  })
  public ResponseEntity<MealPlanResponse> generateMealPlan(
      @Valid @RequestBody MealPlanRequest request) {
    MealPlanResponse response = mealPlanService.generateMealPlan(request);
    return ResponseEntity.ok(response);
  }
}
