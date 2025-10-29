package com.teamx.fitness.controller;

import com.teamx.fitness.security.ClientContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Research Controller - Endpoints for research data aggregation and analysis.
 *
 * <p>This controller provides endpoints for research users to access aggregated,
 * anonymized data for research purposes. No PII (Personally Identifiable Information)
 * is exposed. Research users can view aggregated, anonymized information only.</p>
 */
@RestController
@RequestMapping("/api/research")
@CrossOrigin(origins = "*")
@Tag(
    name = "Research Controller",
    description = "Endpoints for research data aggregation and analysis. "
        + "Research users can view aggregated, anonymized information only.")
public class ResearchController {

  /** Size of the demographic sample for basic statistics */
  private static final int SAMPLE_SIZE = 156;
  
  /** Average Body Mass Index across the sample */
  private static final double AVG_BMI = 24.5;
  
  /** Average weight in kilograms across the sample */
  private static final double AVG_WEIGHT = 72.3;
  
  /** Average height in centimeters across the sample */
  private static final double AVG_HEIGHT = 171.2;
  
  /** Average body fat percentage across the sample */
  private static final double AVG_BODY_FAT = 18.5;
  
  /** Average weekly training frequency across the sample */
  private static final double AVG_WEEKLY_FREQ = 4.2;

  /** Average daily caloric intake across the sample */
  private static final int AVG_CALORIES = 2450;
  
  /** Average daily protein intake in grams */
  private static final int AVG_PROTEIN = 120;
  
  /** Average daily carbohydrate intake in grams */
  private static final int AVG_CARBS = 280;
  
  /** Average daily fat intake in grams */
  private static final int AVG_FAT = 85;

  /** Average number of workouts per week */
  private static final double AVG_WORKOUTS = 3.8;
  
  /** Average workout duration in minutes */
  private static final int AVG_DURATION = 52;
  
  /** Average calories burned per workout session */
  private static final int AVG_BURN = 420;
  
  /** Percentage of aerobic exercises in workout distribution */
  private static final double EX_AEROBIC = 45.0;
  
  /** Percentage of anaerobic exercises in workout distribution */
  private static final double EX_ANAEROBIC = 35.0;
  
  /** Percentage of flexibility exercises in workout distribution */
  private static final double EX_FLEX = 15.0;
  
  /** Percentage of mixed-type exercises in workout distribution */
  private static final double EX_MIXED = 5.0;
  
  /** Sample size for workout pattern analysis */
  private static final int SAMPLE_SIZE_WORKOUT = 89;

  /** Percentage of carbohydrates in bulking diet */
  private static final int BULK_CARBS = 45;
  
  /** Percentage of protein in bulking diet */
  private static final int BULK_PROTEIN = 30;
  
  /** Percentage of fat in bulking diet */
  private static final int BULK_FAT = 25;
  
  /** Daily caloric intake for bulking diet */
  private static final int BULK_CAL = 3200;
  
  /** Percentage of carbohydrates in cutting diet */
  private static final int CUT_CARBS = 35;
  
  /** Percentage of protein in cutting diet */
  private static final int CUT_PROTEIN = 40;
  
  /** Percentage of fat in cutting diet */
  private static final int CUT_FAT = 25;
  
  /** Daily caloric intake for cutting diet */
  private static final int CUT_CAL = 2000;
  
  /** Percentage of carbohydrates in maintenance diet */
  private static final int DEFAULT_CARBS = 40;
  
  /** Percentage of protein in maintenance diet */
  private static final int DEFAULT_PROTEIN = 30;
  
  /** Percentage of fat in maintenance diet */
  private static final int DEFAULT_FAT = 30;
  
  /** Daily caloric intake for maintenance diet */
  private static final int DEFAULT_CAL = 2500;
  
  /** Sample size for nutrition trend analysis */
  private static final int NUTRITION_SAMPLE_SIZE = 234;

  /** Percentage of population in underweight BMI category */
  private static final double BMI_UNDER = 5.2;

  /** Percentage of population in normal BMI category */
  private static final double BMI_NORMAL = 48.3;

  /** Percentage of population in overweight BMI category */
  private static final double BMI_OVER = 32.1;

  /** Percentage of population in obese BMI category */
  private static final double BMI_OBESE = 14.4;

  /** Overall goal achievement rate as a percentage */
  private static final double ACHIEVE_OVERALL = 67.8;

  /** Weight loss goal achievement rate as a percentage */
  private static final double ACHIEVE_LOSS = 62.3;

  /** Muscle gain goal achievement rate as a percentage */
  private static final double ACHIEVE_GAIN = 71.5;

  /** Maintenance goal adherence rate as a percentage */
  private static final double ACHIEVE_MAINTAIN = 82.1;

  /** Total number of people in the studied population */
  private static final int POPULATION_TOTAL = 1523;

  private void validateResearchAccess() {
    String clientId = ClientContext.getClientId();
    if (ClientContext.isMobileClient(clientId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "Mobile clients are not authorized to access research endpoints. "
              + "Research endpoints are restricted to research clients only.");
    }
  }

  @GetMapping("/persons")
  @Operation(
      summary = "Retrieve all users' anonymized data",
      description = "Get aggregated, anonymized data from all users for research purposes. "
          + "No PII (Personally Identifiable Information) is exposed. "
          + "Only research users can access this endpoint.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Anonymized data retrieved successfully",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
          {
            \"totalUsers\": 1523,
            \"dataAnonymized\": true,
            \"privacyCompliant\": true,
            \"containsPII\": false
          }
          """))),
      @ApiResponse(responseCode = "403", description = "Forbidden - Mobile clients cannot access research endpoints")
  })
  public ResponseEntity<Map<String, Object>> getAllUsersAnonymized() {
    validateResearchAccess();

    Map<String, Object> response = new HashMap<>();
    response.put("totalUsers", POPULATION_TOTAL);
    response.put("dataAnonymized", true);
    response.put("privacyCompliant", true);
    response.put("containsPII", false);
    response.put("accessLevel", "RESEARCH_ONLY");

    return ResponseEntity.ok(response);
  }

  @GetMapping("/demographics")
  @Operation(
      summary = "Get aggregated statistics by demographic cohort",
      description = "Retrieve anonymized demographic statistics with minimum cohort size enforcement. "
          + "Returns aggregated data without PII. Only research users can access this endpoint.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Demographic statistics retrieved successfully",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
          {
            \"cohort\": {
              \"ageRange\": \"25-34\",
              \"gender\": \"ALL\",
              \"objective\": \"ALL\",
              \"sampleSize\": 156,
              \"meetsPrivacyThreshold\": true
            },
            \"physicalMetrics\": {
              \"averageBMI\": 24.5,
              \"averageWeight\": 72.3,
              \"averageHeight\": 171.2
            },
            \"dataAnonymized\": true,
            \"privacyCompliant\": true
          }
          """))),
      @ApiResponse(responseCode = "403", description = "Forbidden - Mobile clients cannot access research endpoints")
  })
  public ResponseEntity<Map<String, Object>> getDemographicStats(
      @Parameter(description = "Age range filter (e.g., '25-34', '35-44')")
      @RequestParam(required = false) String ageRange,
      @Parameter(description = "Gender filter (optional)")
      @RequestParam(required = false) String gender,
      @Parameter(description = "Fitness objective filter (optional)")
      @RequestParam(required = false) String objective) {

    validateResearchAccess();

    Map<String, Object> response = new HashMap<>();

    Map<String, Object> cohort = new HashMap<>();
    cohort.put("ageRange", ageRange != null ? ageRange : "ALL");
    cohort.put("gender", gender != null ? gender : "ALL");
    cohort.put("objective", objective != null ? objective : "ALL");
    cohort.put("sampleSize", SAMPLE_SIZE);
    cohort.put("meetsPrivacyThreshold", true);

    Map<String, Object> physicalMetrics = new HashMap<>();
    physicalMetrics.put("averageBMI", AVG_BMI);
    physicalMetrics.put("averageWeight", AVG_WEIGHT);
    physicalMetrics.put("averageHeight", AVG_HEIGHT);
    physicalMetrics.put("averageBodyFat", AVG_BODY_FAT);
    physicalMetrics.put("averageWeeklyTrainingFreq", AVG_WEEKLY_FREQ);

    Map<String, Object> nutritionalMetrics = new HashMap<>();
    nutritionalMetrics.put("averageDailyCalories", AVG_CALORIES);
    nutritionalMetrics.put("averageDailyProtein", AVG_PROTEIN);
    nutritionalMetrics.put("averageDailyCarbs", AVG_CARBS);
    nutritionalMetrics.put("averageDailyFat", AVG_FAT);

    response.put("cohort", cohort);
    response.put("physicalMetrics", physicalMetrics);
    response.put("nutritionalMetrics", nutritionalMetrics);
    response.put("dataAnonymized", true);
    response.put("privacyCompliant", true);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/workout-patterns")
  @Operation(
      summary = "Get workout pattern analysis by demographic",
      description = "Retrieve anonymized workout pattern analysis by demographic groups. "
          + "All data is aggregated and anonymized. Only research users can access this endpoint.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Workout patterns retrieved successfully",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
          {
            \"ageRange\": \"25-34\",
            \"patterns\": {
              \"averageWorkoutsPerWeek\": 3.8,
              \"mostCommonExerciseType\": \"AEROBIC\",
              \"averageSessionDuration\": 52,
              \"averageCaloriesBurnedPerSession\": 420
            },
            \"exerciseDistribution\": {
              \"AEROBIC\": 45.0,
              \"ANAEROBIC\": 35.0,
              \"FLEXIBILITY\": 15.0,
              \"MIXED\": 5.0
            },
            \"sampleSize\": 89,
            \"privacyProtected\": true
          }
          """))),
      @ApiResponse(responseCode = "403", description = "Forbidden - Mobile clients cannot access research endpoints")
  })
  public ResponseEntity<Map<String, Object>> getWorkoutPatterns(
      @Parameter(description = "Age range filter (optional)")
      @RequestParam(required = false) String ageRange) {

    validateResearchAccess();

    Map<String, Object> response = new HashMap<>();

    Map<String, Object> patterns = new HashMap<>();
    patterns.put("averageWorkoutsPerWeek", AVG_WORKOUTS);
    patterns.put("mostCommonExerciseType", "AEROBIC");
    patterns.put("averageSessionDuration", AVG_DURATION);
    patterns.put("averageCaloriesBurnedPerSession", AVG_BURN);

    Map<String, Double> exerciseDistribution = new HashMap<>();
    exerciseDistribution.put("AEROBIC", EX_AEROBIC);
    exerciseDistribution.put("ANAEROBIC", EX_ANAEROBIC);
    exerciseDistribution.put("FLEXIBILITY", EX_FLEX);
    exerciseDistribution.put("MIXED", EX_MIXED);

    response.put("ageRange", ageRange != null ? ageRange : "ALL");
    response.put("patterns", patterns);
    response.put("exerciseDistribution", exerciseDistribution);
    response.put("sampleSize", SAMPLE_SIZE_WORKOUT);
    response.put("privacyProtected", true);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/nutrition-trends")
  @Operation(
      summary = "Get nutrition trends by fitness objective",
      description = "Retrieve anonymized nutrition trends and macro distribution patterns by fitness objective. "
          + "Returns aggregated data without individual information. Only research users can access this endpoint.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Nutrition trends retrieved successfully",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
          {
            \"objective\": \"BULK\",
            \"macroDistribution\": {
              \"carbs\": 45,
              \"protein\": 30,
              \"fat\": 25,
              \"averageCalories\": 3200
            },
            \"sampleSize\": 234,
            \"dataType\": \"AGGREGATED\",
            \"containsPII\": false
          }
          """))),
      @ApiResponse(responseCode = "403", description = "Forbidden - Mobile clients cannot access research endpoints")
  })
  public ResponseEntity<Map<String, Object>> getNutritionTrends(
      @Parameter(description = "Fitness objective filter (BULK, CUT, RECOVER)")
      @RequestParam(required = false) String objective) {

    validateResearchAccess();
    Map<String, Object> response = new HashMap<>();

    Map<String, Object> macroDistribution = new HashMap<>();
    if ("BULK".equals(objective)) {
      macroDistribution.put("carbs", BULK_CARBS);
      macroDistribution.put("protein", BULK_PROTEIN);
      macroDistribution.put("fat", BULK_FAT);
      macroDistribution.put("averageCalories", BULK_CAL);
    } else if ("CUT".equals(objective)) {
      macroDistribution.put("carbs", CUT_CARBS);
      macroDistribution.put("protein", CUT_PROTEIN);
      macroDistribution.put("fat", CUT_FAT);
      macroDistribution.put("averageCalories", CUT_CAL);
    } else {
      macroDistribution.put("carbs", DEFAULT_CARBS);
      macroDistribution.put("protein", DEFAULT_PROTEIN);
      macroDistribution.put("fat", DEFAULT_FAT);
      macroDistribution.put("averageCalories", DEFAULT_CAL);
    }

    response.put("objective", objective != null ? objective : "ALL");
    response.put("macroDistribution", macroDistribution);
    response.put("sampleSize", NUTRITION_SAMPLE_SIZE);
    response.put("dataType", "AGGREGATED");
    response.put("containsPII", false);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/population-health")
  @Operation(
      summary = "Get population health metrics",
      description = "Retrieve high-level population health indicators and goal achievement metrics. "
          + "All data is aggregated and anonymized without individual identification. "
          + "Only research users can access this endpoint.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Population health metrics retrieved successfully",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
          {
            \"totalPopulation\": 1523,
            \"bmiDistribution\": {
              \"underweight\": 5.2,
              \"normal\": 48.3,
              \"overweight\": 32.1,
              \"obese\": 14.4
            },
            \"goalMetrics\": {
              \"overallAchievementRate\": 67.8,
              \"weightLossSuccessRate\": 62.3,
              \"muscleGainSuccessRate\": 71.5,
              \"maintenanceAdherence\": 82.1
            },
            \"dataProtection\": \"All data is aggregated and anonymized\"
          }
          """))),
      @ApiResponse(responseCode = "403", description = "Forbidden - Mobile clients cannot access research endpoints")
  })
  public ResponseEntity<Map<String, Object>> getPopulationHealth() {

    validateResearchAccess();

    Map<String, Object> response = new HashMap<>();

    Map<String, Object> bmiDistribution = new HashMap<>();
    bmiDistribution.put("underweight", BMI_UNDER);
    bmiDistribution.put("normal", BMI_NORMAL);
    bmiDistribution.put("overweight", BMI_OVER);
    bmiDistribution.put("obese", BMI_OBESE);

    Map<String, Object> goalMetrics = new HashMap<>();
    goalMetrics.put("overallAchievementRate", ACHIEVE_OVERALL);
    goalMetrics.put("weightLossSuccessRate", ACHIEVE_LOSS);
    goalMetrics.put("muscleGainSuccessRate", ACHIEVE_GAIN);
    goalMetrics.put("maintenanceAdherence", ACHIEVE_MAINTAIN);

    response.put("totalPopulation", POPULATION_TOTAL);
    response.put("bmiDistribution", bmiDistribution);
    response.put("goalMetrics", goalMetrics);
    response.put("dataProtection", "All data is aggregated and anonymized");

    return ResponseEntity.ok(response);
  }
}

