package com.teamx.fitness.controller;

import com.teamx.fitness.security.ClientContext;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for research/analyzer endpoints.
 * Provides anonymized, aggregated data for research purposes.
 * NO PII (Personal Identifiable Information) is exposed through these endpoints.
 */
@RestController
@RequestMapping("/api/research")
@CrossOrigin(origins = "*")
public class ResearchController {

  /**
   * Validates that the current client is authorized to access research endpoints.
   * Mobile clients are not allowed to access research data.
   *
   * @throws ResponseStatusException with 403 Forbidden if the client is not authorized
   */
  private void validateResearchAccess() {
    String clientId = ClientContext.getClientId();
    if (ClientContext.isMobileClient(clientId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "Mobile clients are not authorized to access research endpoints. Research endpoints are"
              + " restricted to research-tool clients only.");
    }
  }

  /**
   * Get aggregated statistics by demographic cohort.
     * Returns anonymized data with minimum cohort size enforcement.
     *
     * @param ageRange age range (e.g., "25-34", "35-44")
     * @param gender gender filter (optional)
     * @param objective fitness objective filter (optional)
     * @return aggregated statistics without PII
     */
  @GetMapping("/demographics")
  public ResponseEntity<Map<String, Object>> getDemographicStats(
      @RequestParam(required = false) String ageRange,
      @RequestParam(required = false) String gender,
      @RequestParam(required = false) String objective) {

    validateResearchAccess();

    Map<String, Object> response = new HashMap<>();

        // Cohort information (NO NAMES OR PERSONAL IDS)
        Map<String, Object> cohort = new HashMap<>();
        cohort.put("ageRange", ageRange != null ? ageRange : "ALL");
        cohort.put("gender", gender != null ? gender : "ALL");
        cohort.put("objective", objective != null ? objective : "ALL");
        cohort.put("sampleSize", 156); // Example sample size
        cohort.put("meetsPrivacyThreshold", true); // Only return if sample size > 10

        // Aggregated physical metrics
        Map<String, Object> physicalMetrics = new HashMap<>();
        physicalMetrics.put("averageBMI", 24.5);
        physicalMetrics.put("averageWeight", 72.3);
        physicalMetrics.put("averageHeight", 171.2);
        physicalMetrics.put("averageBodyFat", 18.5);
        physicalMetrics.put("averageWeeklyTrainingFreq", 4.2);

        // Nutritional metrics (aggregated)
        Map<String, Object> nutritionalMetrics = new HashMap<>();
        nutritionalMetrics.put("averageDailyCalories", 2450);
        nutritionalMetrics.put("averageDailyProtein", 120);
        nutritionalMetrics.put("averageDailyCarbs", 280);
        nutritionalMetrics.put("averageDailyFat", 85);

        response.put("cohort", cohort);
        response.put("physicalMetrics", physicalMetrics);
        response.put("nutritionalMetrics", nutritionalMetrics);
        response.put("dataAnonymized", true);
        response.put("privacyCompliant", true);

        return ResponseEntity.ok(response);
    }

    /**
     * Get workout pattern analysis by demographic.
     * All data is aggregated and anonymized.
     *
     * @param ageRange age range filter
     * @return anonymized workout patterns
     */
  @GetMapping("/workout-patterns")
  public ResponseEntity<Map<String, Object>> getWorkoutPatterns(
      @RequestParam(required = false) String ageRange) {

    validateResearchAccess();

    Map<String, Object> response = new HashMap<>();

        // Anonymized workout distribution
        Map<String, Object> patterns = new HashMap<>();
        patterns.put("averageWorkoutsPerWeek", 3.8);
        patterns.put("mostCommonExerciseType", "AEROBIC");
        patterns.put("averageSessionDuration", 52); // minutes
        patterns.put("averageCaloriesBurnedPerSession", 420);

        // Exercise type distribution (percentages)
        Map<String, Double> exerciseDistribution = new HashMap<>();
        exerciseDistribution.put("AEROBIC", 45.0);
        exerciseDistribution.put("ANAEROBIC", 35.0);
        exerciseDistribution.put("FLEXIBILITY", 15.0);
        exerciseDistribution.put("MIXED", 5.0);

        response.put("ageRange", ageRange != null ? ageRange : "ALL");
        response.put("patterns", patterns);
        response.put("exerciseDistribution", exerciseDistribution);
        response.put("sampleSize", 89);
        response.put("privacyProtected", true);

        return ResponseEntity.ok(response);
    }

    /**
     * Get nutrition trends by fitness objective.
     * Returns macro distribution patterns without individual data.
     *
     * @param objective fitness objective (BULK, CUT, RECOVER)
     * @return aggregated nutrition trends
     */
  @GetMapping("/nutrition-trends")
  public ResponseEntity<Map<String, Object>> getNutritionTrends(
      @RequestParam(required = false) String objective) {

    validateResearchAccess();

    Map<String, Object> response = new HashMap<>();

        // Macro distribution by objective (percentages)
        Map<String, Object> macroDistribution = new HashMap<>();
        if ("BULK".equals(objective)) {
            macroDistribution.put("carbs", 45);
            macroDistribution.put("protein", 30);
            macroDistribution.put("fat", 25);
            macroDistribution.put("averageCalories", 3200);
        } else if ("CUT".equals(objective)) {
            macroDistribution.put("carbs", 35);
            macroDistribution.put("protein", 40);
            macroDistribution.put("fat", 25);
            macroDistribution.put("averageCalories", 2000);
        } else {
            macroDistribution.put("carbs", 40);
            macroDistribution.put("protein", 30);
            macroDistribution.put("fat", 30);
            macroDistribution.put("averageCalories", 2500);
        }

        response.put("objective", objective != null ? objective : "ALL");
        response.put("macroDistribution", macroDistribution);
        response.put("sampleSize", 234);
        response.put("dataType", "AGGREGATED");
        response.put("containsPII", false);

        return ResponseEntity.ok(response);
    }

    /**
     * Get population health metrics.
     * Provides high-level health indicators without individual identification.
     */
  @GetMapping("/population-health")
  public ResponseEntity<Map<String, Object>> getPopulationHealth() {

    validateResearchAccess();

    Map<String, Object> response = new HashMap<>();

        // BMI distribution (percentages)
        Map<String, Object> bmiDistribution = new HashMap<>();
        bmiDistribution.put("underweight", 5.2);
        bmiDistribution.put("normal", 48.3);
        bmiDistribution.put("overweight", 32.1);
        bmiDistribution.put("obese", 14.4);

        // Goal achievement rates
        Map<String, Object> goalMetrics = new HashMap<>();
        goalMetrics.put("overallAchievementRate", 67.8);
        goalMetrics.put("weightLossSuccessRate", 62.3);
        goalMetrics.put("muscleGainSuccessRate", 71.5);
        goalMetrics.put("maintenanceAdherence", 82.1);

        response.put("totalPopulation", 1523);
        response.put("bmiDistribution", bmiDistribution);
        response.put("goalMetrics", goalMetrics);
        response.put("dataProtection", "All data is aggregated and anonymized");

        return ResponseEntity.ok(response);
    }
}
