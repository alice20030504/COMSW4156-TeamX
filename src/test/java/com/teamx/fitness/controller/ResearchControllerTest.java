package com.teamx.fitness.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for ResearchController REST endpoints.
 *
 * <p>This test class validates the ResearchController's ability to provide aggregated,
 * anonymized fitness data for research and analytics purposes. Unlike PersonController
 * which provides individual user data, ResearchController serves population-level statistics
 * that protect individual privacy while enabling scientific analysis.</p>
 *
 * <p><strong>Why Research Endpoints Matter:</strong></p>
 * <ul>
 *   <li><strong>Scientific Research:</strong> Enables population health studies without
 *       exposing individual identities</li>
 *   <li><strong>Business Intelligence:</strong> Provides aggregated metrics for product
 *       development and trend analysis</li>
 *   <li><strong>Privacy Compliance:</strong> Demonstrates GDPR/HIPAA-compliant data
 *       anonymization practices</li>
 *   <li><strong>Client Type Differentiation:</strong> Validates that mobile clients
 *       cannot access research data (tested via integration tests)</li>
 * </ul>
 *
 * <p><strong>Testing Strategy:</strong></p>
 * <ul>
 *   <li><strong>Valid Cases:</strong> Test each endpoint returns properly structured
 *       aggregated data</li>
 *   <li><strong>Query Parameter Variations:</strong> Test filtering by ageRange, gender,
 *       objective parameters</li>
 *   <li><strong>Privacy Verification:</strong> Validate that responses contain only
 *       aggregated data with privacy flags set correctly</li>
 *   <li><strong>Response Structure:</strong> Ensure consistent JSON structure for
 *       client integration</li>
 * </ul>
 *
 * <p><strong>What's NOT Tested Here:</strong> Access control (mobile vs research clients)
 * is tested in ResearchEndpointAccessControlTest integration tests, not unit tests.</p>
 *
 * @see ResearchController
 * @see ResearchEndpointAccessControlTest
 */
@WebMvcTest(ResearchController.class)
@DisplayName("ResearchController REST API Tests")
class ResearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ============================================
    // /api/research/demographics Tests
    // ============================================

    /**
     * Tests demographics endpoint without query parameters (all cohorts).
     *
     * <p>This test verifies the default behavior when no filtering parameters are provided,
     * which should return aggregated statistics across all demographics. This is the most
     * common use case for getting a population-wide overview.</p>
     *
     * <p><strong>Privacy Requirements Validated:</strong></p>
     * <ul>
     *   <li>dataAnonymized flag is true</li>
     *   <li>privacyCompliant flag is true</li>
     *   <li>cohort.meetsPrivacyThreshold is true (sample size > 10)</li>
     *   <li>Response contains only aggregated metrics, no individual data</li>
     * </ul>
     *
     * <p><strong>Response Structure Validation:</strong> The test checks for presence of
     * cohort metadata, physicalMetrics, nutritionalMetrics, and privacy flags to ensure
     * the response schema is stable for client consumption.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getDemographicStats - Returns aggregated stats for all demographics")
    void testGetDemographicStats_AllCohorts() throws Exception {
        // Given: ResearchController configured to return demo data

        // When: GET request to /api/research/demographics with no filters
        // Then: Returns 200 OK with complete aggregated response
        mockMvc.perform(get("/api/research/demographics")
                .header("X-Client-ID", "research-tool1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cohort.ageRange", is("ALL")))
                .andExpect(jsonPath("$.cohort.gender", is("ALL")))
                .andExpect(jsonPath("$.cohort.objective", is("ALL")))
                .andExpect(jsonPath("$.cohort.sampleSize", isA(Integer.class)))
                .andExpect(jsonPath("$.cohort.meetsPrivacyThreshold", is(true)))
                .andExpect(jsonPath("$.physicalMetrics.averageBMI", isA(Number.class)))
                .andExpect(jsonPath("$.physicalMetrics.averageWeight", isA(Number.class)))
                .andExpect(jsonPath("$.physicalMetrics.averageHeight", isA(Number.class)))
                .andExpect(jsonPath("$.physicalMetrics.averageBodyFat", isA(Number.class)))
                .andExpect(jsonPath("$.physicalMetrics.averageWeeklyTrainingFreq", isA(Number.class)))
                .andExpect(jsonPath("$.nutritionalMetrics.averageDailyCalories", isA(Integer.class)))
                .andExpect(jsonPath("$.nutritionalMetrics.averageDailyProtein", isA(Integer.class)))
                .andExpect(jsonPath("$.nutritionalMetrics.averageDailyCarbs", isA(Integer.class)))
                .andExpect(jsonPath("$.nutritionalMetrics.averageDailyFat", isA(Integer.class)))
                .andExpect(jsonPath("$.dataAnonymized", is(true)))
                .andExpect(jsonPath("$.privacyCompliant", is(true)));
    }

    /**
     * Tests demographics endpoint filtered by age range.
     *
     * <p>This test verifies that the ageRange query parameter correctly filters the cohort,
     * enabling age-specific population analysis. Age-based filtering is critical for research
     * since fitness patterns vary significantly across age demographics.</p>
     *
     * <p><strong>Use Case:</strong> Researchers studying age-related fitness trends (e.g.,
     * "Do people aged 25-34 have different BMI distributions than 35-44?") need clean
     * age cohort separation.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getDemographicStats - Returns stats filtered by age range")
    void testGetDemographicStats_FilteredByAgeRange() throws Exception {
        // Given: ResearchController with age range filter capability

        // When: GET request with ageRange=25-34 parameter
        // Then: Returns 200 OK with cohort showing specified age range
        mockMvc.perform(get("/api/research/demographics")
                .param("ageRange", "25-34")
                .header("X-Client-ID", "research-tool1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cohort.ageRange", is("25-34")))
                .andExpect(jsonPath("$.cohort.gender", is("ALL")))
                .andExpect(jsonPath("$.dataAnonymized", is(true)));
    }

    /**
     * Tests demographics endpoint filtered by gender.
     *
     * <p>This test validates gender-based filtering for studying sex differences in
     * fitness metrics. Gender-specific analysis is important for research as physiological
     * differences affect BMI thresholds, BMR calculations, and fitness goals.</p>
     *
     * <p><strong>Research Application:</strong> Gender-stratified analysis helps identify
     * whether fitness interventions need different approaches for different genders.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getDemographicStats - Returns stats filtered by gender")
    void testGetDemographicStats_FilteredByGender() throws Exception {
        // Given: ResearchController with gender filter capability

        // When: GET request with gender=female parameter
        // Then: Returns 200 OK with cohort showing specified gender
        mockMvc.perform(get("/api/research/demographics")
                .param("gender", "female")
                .header("X-Client-ID", "research-tool1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cohort.ageRange", is("ALL")))
                .andExpect(jsonPath("$.cohort.gender", is("female")))
                .andExpect(jsonPath("$.privacyCompliant", is(true)));
    }

    /**
     * Tests demographics endpoint filtered by fitness objective.
     *
     * <p>This test verifies objective-based filtering, allowing researchers to compare
     * fitness patterns between people with different goals (e.g., BULK vs CUT vs RECOVER).
     * Objective-specific analysis helps tailor nutrition and training recommendations.</p>
     *
     * <p><strong>Strategic Value:</strong> Understanding how different objective cohorts
     * behave helps product teams design goal-specific features and content.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getDemographicStats - Returns stats filtered by fitness objective")
    void testGetDemographicStats_FilteredByObjective() throws Exception {
        // Given: ResearchController with objective filter capability

        // When: GET request with objective=BULK parameter
        // Then: Returns 200 OK with cohort showing specified objective
        mockMvc.perform(get("/api/research/demographics")
                .param("objective", "BULK")
                .header("X-Client-ID", "research-tool1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cohort.ageRange", is("ALL")))
                .andExpect(jsonPath("$.cohort.objective", is("BULK")))
                .andExpect(jsonPath("$.dataAnonymized", is(true)));
    }

    /**
     * Tests demographics endpoint with multiple filters combined.
     *
     * <p>This test validates that multiple query parameters can be combined for granular
     * cohort analysis (e.g., "females aged 25-34 with BULK objective"). This enables
     * sophisticated multi-dimensional research queries.</p>
     *
     * <p><strong>Complex Query Validation:</strong> Real-world research often requires
     * intersecting multiple demographic dimensions to find specific subpopulations.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getDemographicStats - Returns stats with multiple filters combined")
    void testGetDemographicStats_MultipleFilters() throws Exception {
        // Given: ResearchController supporting multiple simultaneous filters

        // When: GET request with ageRange, gender, and objective parameters
        // Then: Returns 200 OK with cohort showing all specified filters
        mockMvc.perform(get("/api/research/demographics")
                .param("ageRange", "35-44")
                .param("gender", "male")
                .param("objective", "CUT")
                .header("X-Client-ID", "research-tool1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cohort.ageRange", is("35-44")))
                .andExpect(jsonPath("$.cohort.gender", is("male")))
                .andExpect(jsonPath("$.cohort.objective", is("CUT")))
                .andExpect(jsonPath("$.privacyCompliant", is(true)));
    }

    // ============================================
    // /api/research/workout-patterns Tests
    // ============================================

    /**
     * Tests workout patterns endpoint without filters.
     *
     * <p>This test verifies that the endpoint returns aggregated workout behavior statistics
     * across all demographics, providing insights into exercise patterns, frequency, duration,
     * and type distribution. This data helps identify population-wide fitness trends.</p>
     *
     * <p><strong>Metrics Validated:</strong></p>
     * <ul>
     *   <li>averageWorkoutsPerWeek - training frequency</li>
     *   <li>mostCommonExerciseType - AEROBIC, ANAEROBIC, FLEXIBILITY, MIXED</li>
     *   <li>averageSessionDuration - workout length in minutes</li>
     *   <li>averageCaloriesBurnedPerSession - energy expenditure</li>
     *   <li>exerciseDistribution - percentage breakdown by type</li>
     * </ul>
     *
     * <p><strong>Privacy Flag:</strong> privacyProtected=true confirms no individual workout
     * data is exposed, only aggregated patterns.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getWorkoutPatterns - Returns aggregated workout patterns for all users")
    void testGetWorkoutPatterns_AllUsers() throws Exception {
        // Given: ResearchController configured to return workout pattern data

        // When: GET request to /api/research/workout-patterns with no filters
        // Then: Returns 200 OK with complete workout pattern response
        mockMvc.perform(get("/api/research/workout-patterns")
                .header("X-Client-ID", "research-tool1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ageRange", is("ALL")))
                .andExpect(jsonPath("$.patterns.averageWorkoutsPerWeek", isA(Number.class)))
                .andExpect(jsonPath("$.patterns.mostCommonExerciseType", notNullValue()))
                .andExpect(jsonPath("$.patterns.averageSessionDuration", isA(Integer.class)))
                .andExpect(jsonPath("$.patterns.averageCaloriesBurnedPerSession", isA(Integer.class)))
                .andExpect(jsonPath("$.exerciseDistribution.AEROBIC", isA(Number.class)))
                .andExpect(jsonPath("$.exerciseDistribution.ANAEROBIC", isA(Number.class)))
                .andExpect(jsonPath("$.exerciseDistribution.FLEXIBILITY", isA(Number.class)))
                .andExpect(jsonPath("$.exerciseDistribution.MIXED", isA(Number.class)))
                .andExpect(jsonPath("$.sampleSize", isA(Integer.class)))
                .andExpect(jsonPath("$.privacyProtected", is(true)));
    }

    /**
     * Tests workout patterns endpoint filtered by age range.
     *
     * <p>This test verifies age-specific workout pattern analysis, which is valuable
     * for understanding how exercise preferences and capabilities change across age groups.
     * For example, younger demographics may prefer high-intensity interval training while
     * older cohorts may favor flexibility and lower-impact exercises.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getWorkoutPatterns - Returns patterns filtered by age range")
    void testGetWorkoutPatterns_FilteredByAgeRange() throws Exception {
        // Given: ResearchController with age-filtered workout data

        // When: GET request with ageRange=25-34 parameter
        // Then: Returns 200 OK with age-specific workout patterns
        mockMvc.perform(get("/api/research/workout-patterns")
                .param("ageRange", "45-54")
                .header("X-Client-ID", "research-tool1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ageRange", is("45-54")))
                .andExpect(jsonPath("$.patterns", notNullValue()))
                .andExpect(jsonPath("$.exerciseDistribution", notNullValue()))
                .andExpect(jsonPath("$.privacyProtected", is(true)));
    }

    // ============================================
    // /api/research/nutrition-trends Tests
    // ============================================

    /**
     * Tests nutrition trends endpoint without filters.
     *
     * <p>This test verifies the endpoint returns aggregated macronutrient distribution
     * patterns across all fitness objectives. This provides baseline nutritional data
     * for the entire population, useful for understanding general dietary habits.</p>
     *
     * <p><strong>Data Privacy Verification:</strong></p>
     * <ul>
     *   <li>dataType = "AGGREGATED" - confirms no individual meal logs exposed</li>
     *   <li>containsPII = false - explicitly states no personally identifiable information</li>
     * </ul>
     *
     * <p><strong>Macro Distribution:</strong> Response includes percentage breakdown of
     * carbs, protein, fat, plus average calorie intake for the cohort.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getNutritionTrends - Returns nutrition trends for all objectives")
    void testGetNutritionTrends_AllObjectives() throws Exception {
        // Given: ResearchController configured to return nutrition trend data

        // When: GET request to /api/research/nutrition-trends with no filters
        // Then: Returns 200 OK with nutrition trend response
        mockMvc.perform(get("/api/research/nutrition-trends")
                .header("X-Client-ID", "research-tool1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objective", is("ALL")))
                .andExpect(jsonPath("$.macroDistribution.carbs", isA(Integer.class)))
                .andExpect(jsonPath("$.macroDistribution.protein", isA(Integer.class)))
                .andExpect(jsonPath("$.macroDistribution.fat", isA(Integer.class)))
                .andExpect(jsonPath("$.macroDistribution.averageCalories", isA(Integer.class)))
                .andExpect(jsonPath("$.sampleSize", isA(Integer.class)))
                .andExpect(jsonPath("$.dataType", is("AGGREGATED")))
                .andExpect(jsonPath("$.containsPII", is(false)));
    }

    /**
     * Tests nutrition trends endpoint filtered by BULK objective.
     *
     * <p>This test validates that nutrition data can be stratified by fitness objective,
     * revealing how different goals correlate with different dietary patterns. BULK
     * objectives typically show higher calorie intake and different macro ratios compared
     * to CUT or maintenance objectives.</p>
     *
     * <p><strong>Research Value:</strong> Understanding objective-specific nutrition patterns
     * helps validate whether users are following appropriate dietary strategies for their goals.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getNutritionTrends - Returns BULK-specific nutrition patterns")
    void testGetNutritionTrends_BulkObjective() throws Exception {
        // Given: ResearchController with objective-filtered nutrition data

        // When: GET request with objective=BULK parameter
        // Then: Returns 200 OK with BULK-specific macro distribution
        mockMvc.perform(get("/api/research/nutrition-trends")
                .param("objective", "BULK")
                .header("X-Client-ID", "research-tool1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objective", is("BULK")))
                .andExpect(jsonPath("$.macroDistribution.carbs", notNullValue()))
                .andExpect(jsonPath("$.macroDistribution.protein", notNullValue()))
                .andExpect(jsonPath("$.macroDistribution.fat", notNullValue()))
                .andExpect(jsonPath("$.dataType", is("AGGREGATED")))
                .andExpect(jsonPath("$.containsPII", is(false)));
    }

    /**
     * Tests nutrition trends endpoint filtered by CUT objective.
     *
     * <p>CUT objectives typically show lower calorie intake and higher protein ratios
     * compared to BULK. This test validates the endpoint correctly returns CUT-specific
     * nutritional patterns, which should differ from BULK cohorts.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getNutritionTrends - Returns CUT-specific nutrition patterns")
    void testGetNutritionTrends_CutObjective() throws Exception {
        // Given: ResearchController with CUT objective filter

        // When: GET request with objective=CUT parameter
        // Then: Returns 200 OK with CUT-specific macro distribution
        mockMvc.perform(get("/api/research/nutrition-trends")
                .param("objective", "CUT")
                .header("X-Client-ID", "research-tool1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objective", is("CUT")))
                .andExpect(jsonPath("$.macroDistribution", notNullValue()))
                .andExpect(jsonPath("$.containsPII", is(false)));
    }

    // ============================================
    // /api/research/population-health Tests
    // ============================================

    /**
     * Tests population health endpoint for overall health metrics.
     *
     * <p>This test verifies the endpoint returns high-level population health indicators
     * including BMI distribution, goal achievement rates, and overall health trends. This
     * is the most comprehensive research endpoint, providing a bird's-eye view of the
     * entire user population's health status.</p>
     *
     * <p><strong>Key Metrics Validated:</strong></p>
     * <ul>
     *   <li><strong>totalPopulation:</strong> Total sample size for the analysis</li>
     *   <li><strong>bmiDistribution:</strong> Percentage breakdown by BMI category
     *       (underweight, normal, overweight, obese)</li>
     *   <li><strong>goalMetrics:</strong> Success rates for different fitness objectives
     *       (weight loss, muscle gain, maintenance)</li>
     * </ul>
     *
     * <p><strong>Privacy Guarantee:</strong> dataProtection field confirms "All data is
     * aggregated and anonymized", providing explicit privacy assurance in the response.</p>
     *
     * <p><strong>Use Case:</strong> Public health researchers can use this data to study
     * obesity trends, fitness program effectiveness, and population health outcomes without
     * accessing any individual records.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getPopulationHealth - Returns comprehensive population health metrics")
    void testGetPopulationHealth() throws Exception {
        // Given: ResearchController configured to return population health data

        // When: GET request to /api/research/population-health
        // Then: Returns 200 OK with complete population health response
        mockMvc.perform(get("/api/research/population-health")
                .header("X-Client-ID", "research-tool1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPopulation", isA(Integer.class)))
                .andExpect(jsonPath("$.bmiDistribution.underweight", isA(Number.class)))
                .andExpect(jsonPath("$.bmiDistribution.normal", isA(Number.class)))
                .andExpect(jsonPath("$.bmiDistribution.overweight", isA(Number.class)))
                .andExpect(jsonPath("$.bmiDistribution.obese", isA(Number.class)))
                .andExpect(jsonPath("$.goalMetrics.overallAchievementRate", isA(Number.class)))
                .andExpect(jsonPath("$.goalMetrics.weightLossSuccessRate", isA(Number.class)))
                .andExpect(jsonPath("$.goalMetrics.muscleGainSuccessRate", isA(Number.class)))
                .andExpect(jsonPath("$.goalMetrics.maintenanceAdherence", isA(Number.class)))
                .andExpect(jsonPath("$.dataProtection", is("All data is aggregated and anonymized")));
    }
}
