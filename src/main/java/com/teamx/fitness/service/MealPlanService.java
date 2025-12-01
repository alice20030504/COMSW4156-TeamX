package com.teamx.fitness.service;

import com.teamx.fitness.controller.dto.MealPlanRequest;
import com.teamx.fitness.controller.dto.MealPlanResponse;
import java.time.LocalDate;
import java.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for generating meal plans by calling the Python mealplan microservice.
 * 
 * <p>This service acts as a bridge between the Java backend and the Python service,
 * handling HTTP communication and error translation.</p>
 */
@Service
public class MealPlanService {

  private static final Logger logger = LoggerFactory.getLogger(MealPlanService.class);

  private final WebClient webClient;
  private final String mealplanServiceBaseUrl;

  /**
   * Constructs the service with WebClient and base URL configuration.
   *
   * @param webClient WebClient instance for HTTP calls
   * @param mealplanServiceBaseUrl Base URL of the Python mealplan service
   */
  public MealPlanService(
      WebClient.Builder webClientBuilder,
      @Value("${mealplan.service.base-url:http://mealplan-service:5001}") String mealplanServiceBaseUrl) {
    this.mealplanServiceBaseUrl = mealplanServiceBaseUrl;
    this.webClient = webClientBuilder
        .baseUrl(mealplanServiceBaseUrl)
        .build();
  }

  /**
   * Generates a meal plan by calling the Python service.
   * 
   * <p>Converts the MealPlanRequest to the format expected by Python service,
   * which requires: id, age, height, weight, gender, goal.</p>
   *
   * @param request The meal plan request with user profile and goal information
   * @return MealPlanResponse containing the generated meal plan
   * @throws ResponseStatusException if the Python service call fails
   */
  public MealPlanResponse generateMealPlan(MealPlanRequest request) {
    logger.info("Generating meal plan for user: {}", request.getName());

    // Calculate age from birth date
    Integer age = calculateAge(request.getBirthDate());
    if (age == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Unable to calculate age from birthDate");
    }

    // Build request body for Python service
    // Python service expects: id, age, height, weight, gender, goal
    // We use a synthetic ID based on name hash for compatibility
    PythonMealPlanRequest pythonRequest = new PythonMealPlanRequest();
    pythonRequest.setId(Math.abs(request.getName().hashCode()));
    pythonRequest.setAge(age);
    pythonRequest.setHeight(request.getHeightCm());
    pythonRequest.setWeight(request.getWeightKg());
    pythonRequest.setGender(request.getGender().name());
    pythonRequest.setGoal(request.getGoal().name());

    try {
      logger.debug("Calling Python service at {}/mealplan", mealplanServiceBaseUrl);
      
      MealPlanResponse response = webClient
          .post()
          .uri("/mealplan")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(pythonRequest)
          .retrieve()
          .bodyToMono(PythonMealPlanResponse.class)
          .map(pythonResponse -> new MealPlanResponse(pythonResponse.getMealPlan()))
          .block();

      if (response == null || response.getMealPlan() == null || response.getMealPlan().trim().isEmpty()) {
        logger.error("Python service returned empty meal plan");
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, "Meal plan service returned empty result");
      }

      logger.info("Successfully generated meal plan (length: {} chars)", 
          response.getMealPlan().length());
      return response;

    } catch (WebClientResponseException e) {
      logger.error("Python service error: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Failed to generate meal plan: " + e.getMessage());
    } catch (Exception e) {
      logger.error("Unexpected error calling meal plan service", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Failed to generate meal plan: " + e.getMessage());
    }
  }

  /**
   * Calculate age from birth date.
   *
   * @param birthDate person's birth date
   * @return age in years
   */
  private Integer calculateAge(LocalDate birthDate) {
    if (birthDate == null) {
      return null;
    }
    return Period.between(birthDate, LocalDate.now()).getYears();
  }

  /**
   * Internal class for Python service request format.
   */
  private static class PythonMealPlanRequest {
    private Integer id;
    private Integer age;
    private Double height;
    private Double weight;
    private String gender;
    private String goal;

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public Integer getAge() {
      return age;
    }

    public void setAge(Integer age) {
      this.age = age;
    }

    public Double getHeight() {
      return height;
    }

    public void setHeight(Double height) {
      this.height = height;
    }

    public Double getWeight() {
      return weight;
    }

    public void setWeight(Double weight) {
      this.weight = weight;
    }

    public String getGender() {
      return gender;
    }

    public void setGender(String gender) {
      this.gender = gender;
    }

    public String getGoal() {
      return goal;
    }

    public void setGoal(String goal) {
      this.goal = goal;
    }
  }

  /**
   * Internal class for Python service response format.
   */
  private static class PythonMealPlanResponse {
    private String mealPlan;

    public String getMealPlan() {
      return mealPlan;
    }

    public void setMealPlan(String mealPlan) {
      this.mealPlan = mealPlan;
    }
  }
}
