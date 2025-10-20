package com.teamx.fitness.controller;

import com.teamx.fitness.service.PersonService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for PersonController REST endpoints.
 *
 * <p>This test class uses Spring's MockMvc framework to test the controller layer
 * in isolation from the service layer. The PersonService dependency is mocked using
 * Mockito's @MockBean annotation, ensuring tests focus solely on controller behavior,
 * HTTP request/response handling, parameter validation, and error scenarios.</p>
 *
 * <p><strong>Testing Strategy:</strong></p>
 * <ul>
 *   <li><strong>Valid Cases:</strong> Normal inputs with expected 200 OK responses and proper JSON structure</li>
 *   <li><strong>Boundary Cases:</strong> Edge values like zero, thresholds, extreme numbers</li>
 *   <li><strong>Invalid Cases:</strong> Missing parameters, null values, malformed inputs returning 400 Bad Request</li>
 * </ul>
 *
 * <p><strong>Why Test Controllers:</strong></p>
 * <ul>
 *   <li>Controllers are the entry point for all HTTP requests - critical for API reliability</li>
 *   <li>Proper parameter binding and validation prevents malformed requests from reaching services</li>
 *   <li>Response formatting affects all API consumers and must be consistent</li>
 *   <li>Error handling at the controller level provides clear feedback to clients</li>
 * </ul>
 *
 * <p><strong>Testing Pattern:</strong> Arrange-Act-Assert (Given-When-Then)</p>
 *
 * @see PersonController
 * @see PersonService
 */
@WebMvcTest(PersonController.class)
@DisplayName("PersonController REST API Tests")
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    // ============================================
    // /api/persons/bmi Tests
    // ============================================

    /**
     * Tests BMI calculation endpoint with valid weight and height parameters.
     *
     * <p>This test verifies that the controller correctly handles a typical BMI calculation
     * request by accepting valid numeric parameters, delegating the calculation to PersonService,
     * and returning a properly formatted JSON response with 200 OK status.</p>
     *
     * <p><strong>Test Significance:</strong> BMI calculation is a core feature used by clients
     * to assess health metrics. Ensuring accurate parameter handling and response formatting
     * is critical for client integration.</p>
     *
     * <p><strong>Expected Response Structure:</strong></p>
     * <pre>
     * {
     *   "weight": 70.0,
     *   "height": 175.0,
     *   "bmi": 22.86,
     *   "category": "Normal weight"
     * }
     * </pre>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateBMI - Valid: Returns 200 OK with BMI calculation for valid parameters")
    void testCalculateBMI_ValidParameters() throws Exception {
        // Given: PersonService returns a valid BMI calculation
        when(personService.calculateBMI(70.0, 175.0)).thenReturn(22.86);

        // When: GET request to /api/persons/bmi with valid weight and height
        // Then: Returns 200 OK with complete response including BMI and category
        mockMvc.perform(get("/api/persons/bmi")
                .param("weight", "70.0")
                .param("height", "175.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(70.0))
                .andExpect(jsonPath("$.height").value(175.0))
                .andExpect(jsonPath("$.bmi").value(22.86))
                .andExpect(jsonPath("$.category").value("Normal weight"));
    }

    /**
     * Tests BMI endpoint with parameters that result in "Underweight" category.
     *
     * <p>This boundary test verifies that the controller's private getBMICategory() helper
     * method correctly classifies BMI values below 18.5 as "Underweight". This is important
     * for providing accurate health assessments to users.</p>
     *
     * <p><strong>Clinical Significance:</strong> BMI &lt; 18.5 indicates potential health risks
     * associated with being underweight, making accurate categorization essential for
     * health tracking applications.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateBMI - Boundary: Returns 'Underweight' category for BMI < 18.5")
    void testCalculateBMI_BoundaryUnderweight() throws Exception {
        // Given: BMI calculation returns value in underweight range
        when(personService.calculateBMI(55.0, 173.0)).thenReturn(18.4);

        // When: GET request with parameters resulting in low BMI
        // Then: Response includes "Underweight" category
        mockMvc.perform(get("/api/persons/bmi")
                .param("weight", "55.0")
                .param("height", "173.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").value(18.4))
                .andExpect(jsonPath("$.category").value("Underweight"));
    }

    /**
     * Tests BMI endpoint with parameters that result in "Overweight" category.
     *
     * <p>This boundary test ensures proper classification of BMI values between 25 and 30,
     * which fall into the overweight category. This range represents a critical health threshold
     * where lifestyle interventions are typically recommended.</p>
     *
     * <p><strong>Health Context:</strong> BMI 25-30 indicates overweight status, a key metric
     * for fitness management and weight loss goal setting.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateBMI - Boundary: Returns 'Overweight' category for BMI 25-30")
    void testCalculateBMI_BoundaryOverweight() throws Exception {
        // Given: BMI calculation returns value in overweight range
        when(personService.calculateBMI(85.0, 175.0)).thenReturn(27.8);

        // When: GET request with parameters resulting in elevated BMI
        // Then: Response includes "Overweight" category
        mockMvc.perform(get("/api/persons/bmi")
                .param("weight", "85.0")
                .param("height", "175.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").value(27.8))
                .andExpect(jsonPath("$.category").value("Overweight"));
    }

    /**
     * Tests BMI endpoint with parameters that result in "Obese" category.
     *
     * <p>This boundary test verifies classification of BMI values ≥ 30, which indicate obesity.
     * Accurate identification of this category is critical for medical risk assessment and
     * prioritizing health interventions.</p>
     *
     * <p><strong>Medical Importance:</strong> BMI ≥ 30 significantly increases health risks
     * and requires different fitness and nutritional approaches than lower BMI ranges.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateBMI - Boundary: Returns 'Obese' category for BMI >= 30")
    void testCalculateBMI_BoundaryObese() throws Exception {
        // Given: BMI calculation returns value in obese range
        when(personService.calculateBMI(95.0, 170.0)).thenReturn(32.9);

        // When: GET request with parameters resulting in high BMI
        // Then: Response includes "Obese" category
        mockMvc.perform(get("/api/persons/bmi")
                .param("weight", "95.0")
                .param("height", "170.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").value(32.9))
                .andExpect(jsonPath("$.category").value("Obese"));
    }

    /**
     * Tests BMI endpoint when service returns null due to invalid input.
     *
     * <p>This test verifies graceful handling when PersonService cannot calculate BMI
     * (e.g., due to null parameters, zero height). The controller should still return
     * 200 OK but with null BMI and "Unknown" category, allowing clients to handle
     * calculation failures appropriately.</p>
     *
     * <p><strong>Error Handling Philosophy:</strong> Rather than throwing exceptions for
     * business logic failures, the service returns null, and the controller communicates
     * this to clients with a clear "Unknown" category indicator.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateBMI - Invalid: Returns 'Unknown' category when service returns null")
    void testCalculateBMI_ServiceReturnsNull() throws Exception {
        // Given: PersonService returns null (invalid calculation)
        when(personService.calculateBMI(anyDouble(), anyDouble())).thenReturn(null);

        // When: GET request with parameters that can't be calculated
        // Then: Response includes null BMI and "Unknown" category
        mockMvc.perform(get("/api/persons/bmi")
                .param("weight", "70.0")
                .param("height", "0.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").value(nullValue()))
                .andExpect(jsonPath("$.category").value("Unknown"));
    }

    // ============================================
    // /api/persons/age Tests
    // ============================================

    /**
     * Tests age calculation endpoint with a valid birth date.
     *
     * <p>This test verifies that the controller correctly parses ISO 8601 date strings,
     * delegates age calculation to PersonService, and returns the calculated age with
     * the original birth date in the response. Age calculation is fundamental for
     * personalized fitness recommendations and BMR calculations.</p>
     *
     * <p><strong>Date Format:</strong> Uses ISO 8601 format (YYYY-MM-DD) which is the
     * standard for date interchange in REST APIs.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateAge - Valid: Returns 200 OK with age calculation for valid birth date")
    void testCalculateAge_ValidBirthDate() throws Exception {
        // Given: PersonService calculates age as 30 years
        LocalDate birthDate = LocalDate.now().minusYears(30);
        when(personService.calculateAge(birthDate)).thenReturn(30);

        // When: GET request to /api/persons/age with valid ISO date
        // Then: Returns 200 OK with calculated age
        mockMvc.perform(get("/api/persons/age")
                .param("birthDate", birthDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthDate").value(birthDate.toString()))
                .andExpect(jsonPath("$.age").value(30));
    }

    /**
     * Tests age endpoint with boundary case of birth date being today (age 0).
     *
     * <p>This boundary test ensures the endpoint handles newborn cases correctly,
     * returning age 0 rather than throwing errors or returning negative values.
     * This edge case is important for applications supporting infant fitness tracking.</p>
     *
     * <p><strong>Edge Case Handling:</strong> Age 0 is a valid result for same-day births
     * and should be handled gracefully without special error cases.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateAge - Boundary: Returns age 0 for birth date being today")
    void testCalculateAge_BoundaryToday() throws Exception {
        // Given: Person born today has age 0
        LocalDate today = LocalDate.now();
        when(personService.calculateAge(today)).thenReturn(0);

        // When: GET request with today's date
        // Then: Returns age 0 correctly
        mockMvc.perform(get("/api/persons/age")
                .param("birthDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.age").value(0));
    }


    // ============================================
    // /api/persons/calories Tests
    // ============================================

    /**
     * Tests daily calorie needs endpoint with valid parameters for male user.
     *
     * <p>This test verifies the complete workflow of calculating daily calorie needs:
     * the controller accepts multiple parameters, orchestrates two service calls
     * (BMR calculation followed by activity level adjustment), and returns a comprehensive
     * response including both intermediate (BMR) and final (daily calories) values.</p>
     *
     * <p><strong>Nutritional Context:</strong> Daily calorie needs are fundamental for
     * diet planning. The calculation factors in basal metabolic rate (BMR) and activity
     * level, making it essential for personalized nutrition recommendations.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateDailyCalories - Valid: Returns calorie needs for valid male parameters")
    void testCalculateDailyCalories_ValidMale() throws Exception {
        // Given: PersonService calculates BMR and daily calorie needs
        when(personService.calculateBMR(70.0, 175.0, 30, true)).thenReturn(1680.0);
        when(personService.calculateDailyCalorieNeeds(1680.0, 4)).thenReturn(2604.0);

        // When: GET request with complete valid parameters for male user
        // Then: Returns 200 OK with BMR and daily calorie needs
        mockMvc.perform(get("/api/persons/calories")
                .param("weight", "70.0")
                .param("height", "175.0")
                .param("age", "30")
                .param("gender", "male")
                .param("weeklyTrainingFreq", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmr").value(1680.0))
                .andExpect(jsonPath("$.dailyCalories").value(2604.0))
                .andExpect(jsonPath("$.weeklyTrainingFreq").value(4));
    }

    /**
     * Tests calorie endpoint with valid parameters for female user.
     *
     * <p>This test ensures gender-specific BMR formulas are correctly invoked through
     * the controller. The Mifflin-St Jeor equation uses different constants for males
     * and females, making gender parameter handling critical for accurate calculations.</p>
     *
     * <p><strong>Gender Differences:</strong> Female BMR is typically 5-10% lower than
     * male BMR at equivalent weight/height/age, requiring separate validation to ensure
     * the correct formula path is taken.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateDailyCalories - Valid: Returns calorie needs for valid female parameters")
    void testCalculateDailyCalories_ValidFemale() throws Exception {
        // Given: PersonService calculates BMR for female
        when(personService.calculateBMR(60.0, 165.0, 25, false)).thenReturn(1380.0);
        when(personService.calculateDailyCalorieNeeds(1380.0, 3)).thenReturn(1863.0);

        // When: GET request with female gender parameter
        // Then: Returns 200 OK with gender-specific BMR calculation
        mockMvc.perform(get("/api/persons/calories")
                .param("weight", "60.0")
                .param("height", "165.0")
                .param("age", "25")
                .param("gender", "female")
                .param("weeklyTrainingFreq", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmr").value(1380.0))
                .andExpect(jsonPath("$.dailyCalories").value(1863.0));
    }

    /**
     * Tests calorie endpoint with boundary case of sedentary lifestyle (0 training days).
     *
     * <p>This boundary test verifies handling of the minimum activity level. Users with
     * zero training days receive a 1.2x activity multiplier on their BMR, representing
     * sedentary lifestyle. This is a common scenario for users just starting their
     * fitness journey.</p>
     *
     * <p><strong>Activity Level Significance:</strong> The sedentary multiplier (1.2x)
     * is the baseline for daily calorie needs and serves as the starting point for
     * all activity-adjusted calculations.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateDailyCalories - Boundary: Handles sedentary lifestyle (0 training days)")
    void testCalculateDailyCalories_BoundarySedentary() throws Exception {
        // Given: PersonService handles sedentary activity level
        when(personService.calculateBMR(70.0, 175.0, 30, true)).thenReturn(1680.0);
        when(personService.calculateDailyCalorieNeeds(1680.0, 0)).thenReturn(2016.0);

        // When: GET request with 0 weekly training frequency
        // Then: Returns calorie needs with sedentary multiplier (1.2x BMR)
        mockMvc.perform(get("/api/persons/calories")
                .param("weight", "70.0")
                .param("height", "175.0")
                .param("age", "30")
                .param("gender", "male")
                .param("weeklyTrainingFreq", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyCalories").value(2016.0));
    }

    /**
     * Tests calorie endpoint with case-insensitive gender parameter handling.
     *
     * <p>This test verifies that the controller normalizes gender input by accepting
     * various capitalizations (e.g., "Male", "MALE", "male"). This improves API usability
     * and prevents errors from case-sensitive parameter matching.</p>
     *
     * <p><strong>API Usability:</strong> Case-insensitive parameter handling is a best
     * practice that makes the API more forgiving and easier to integrate with various
     * client implementations.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateDailyCalories - Valid: Accepts case-insensitive gender values")
    void testCalculateDailyCalories_CaseInsensitiveGender() throws Exception {
        // Given: PersonService calculates for male
        when(personService.calculateBMR(70.0, 175.0, 30, true)).thenReturn(1680.0);
        when(personService.calculateDailyCalorieNeeds(1680.0, 4)).thenReturn(2604.0);

        // When: GET request with uppercase gender parameter
        // Then: Controller handles case-insensitive gender matching
        mockMvc.perform(get("/api/persons/calories")
                .param("weight", "70.0")
                .param("height", "175.0")
                .param("age", "30")
                .param("gender", "MALE")
                .param("weeklyTrainingFreq", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmr").value(1680.0));
    }

    // ============================================
    // /api/persons/health Tests
    // ============================================

    /**
     * Tests health check endpoint for service availability monitoring.
     *
     * <p>This test verifies the health check endpoint which is commonly used by load
     * balancers, monitoring systems, and orchestration platforms to verify service
     * availability. The endpoint requires no parameters and returns service metadata
     * including status, name, and version.</p>
     *
     * <p><strong>Operational Importance:</strong> Health checks are critical for:
     * <ul>
     *   <li>Load balancer routing decisions</li>
     *   <li>Automated service restart triggers</li>
     *   <li>Monitoring system alerts</li>
     *   <li>Deployment verification</li>
     * </ul>
     * </p>
     *
     * <p><strong>Expected Response:</strong> Always returns 200 OK with "UP" status
     * when the service is running, regardless of downstream service states.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("healthCheck - Returns 200 OK with service metadata")
    void testHealthCheck() throws Exception {
        // Given: No service dependencies needed for health check

        // When: GET request to /api/persons/health
        // Then: Returns 200 OK with service status and metadata
        mockMvc.perform(get("/api/persons/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Personal Fitness Management Service"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }
}
