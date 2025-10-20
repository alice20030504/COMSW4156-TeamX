package com.teamx.fitness.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for PersonService business logic methods.
 *
 * <p>PersonService is the core business logic layer of the fitness management application,
 * providing essential health and fitness calculations including BMI (Body Mass Index),
 * age calculation, BMR (Basal Metabolic Rate), and daily calorie needs. These calculations
 * form the foundation for personalized fitness recommendations and nutritional planning.</p>
 *
 * <p><strong>Why Testing PersonService is Critical:</strong></p>
 * <ul>
 *   <li><strong>Calculation Accuracy:</strong> Health metrics directly influence user
 *       fitness plans and dietary recommendations - errors could lead to unhealthy outcomes</li>
 *   <li><strong>Business Logic Reliability:</strong> Service layer contains complex formulas
 *       (Mifflin-St Jeor, Harris-Benedict) that must be validated independently of controllers</li>
 *   <li><strong>Edge Case Handling:</strong> Must gracefully handle null inputs, zero values,
 *       and boundary conditions without throwing exceptions</li>
 *   <li><strong>Regression Prevention:</strong> Formula changes or refactoring must not
 *       inadvertently break existing calculations</li>
 * </ul>
 *
 * <p><strong>Testing Strategy - Three Case Types Per Method:</strong></p>
 * <ul>
 *   <li><strong>Valid Cases:</strong> Normal, expected inputs that represent typical user
 *       scenarios (e.g., 70kg weight, 175cm height, 30 years old). These validate that
 *       formulas produce correct results for mainstream usage.</li>
 *   <li><strong>Boundary Cases:</strong> Edge values at critical thresholds (e.g., BMI
 *       exactly 18.5 or 25, age 0, extremely high training frequency). These test
 *       conditional logic branches and ensure correct classification at transition points.</li>
 *   <li><strong>Invalid Cases:</strong> Null parameters, zero denominators, missing required
 *       data. These verify graceful degradation and null-safety without exceptions.</li>
 * </ul>
 *
 * <p><strong>Testing Pattern:</strong> All tests follow the Arrange-Act-Assert (AAA) pattern,
 * documented with Given-When-Then comments for clarity:</p>
 * <ul>
 *   <li><strong>Given (Arrange):</strong> Set up test inputs and preconditions</li>
 *   <li><strong>When (Act):</strong> Execute the method under test</li>
 *   <li><strong>Then (Assert):</strong> Verify expected outcomes</li>
 * </ul>
 *
 * <p><strong>Coverage Goals:</strong> Achieve 100% branch coverage for PersonService,
 * ensuring every conditional path, formula branch, and error handling path is exercised.</p>
 *
 * <p><strong>Test Independence:</strong> Each test is fully independent with its own
 * setup in @BeforeEach, ensuring no test state leakage or ordering dependencies.</p>
 *
 * @see PersonService
 */
@DisplayName("PersonService Tests")
class PersonServiceTest {

    private PersonService personService;

    @BeforeEach
    void setUp() {
        personService = new PersonService();
    }

    // ============================================
    // calculateBMI() Tests
    // ============================================

    /**
     * Tests BMI calculation with typical adult weight and height values.
     *
     * <p>This valid case test verifies the core BMI calculation formula:
     * BMI = weight(kg) / (height(m))²</p>
     *
     * <p>With weight = 70kg and height = 175cm (1.75m), the expected BMI is:
     * 70 / (1.75)² = 70 / 3.0625 ≈ 22.86</p>
     *
     * <p><strong>Test Significance:</strong> This represents a typical adult male profile
     * and validates the formula produces correct results for mainstream usage. BMI of 22.86
     * falls within the "Normal weight" category (18.5-24.9), which is the desired outcome
     * for most fitness tracking scenarios.</p>
     *
     * <p><strong>Formula Validation:</strong> The 0.01 delta tolerance accounts for
     * floating-point rounding while ensuring calculation accuracy to two decimal places.</p>
     */
    @Test
    @DisplayName("calculateBMI - Valid: Normal weight and height")
    void testCalculateBMI_Valid() {
        // Given: 70kg, 175cm
        Double weight = 70.0;
        Double height = 175.0;

        // When
        Double bmi = personService.calculateBMI(weight, height);

        // Then: BMI should be approximately 22.86
        assertNotNull(bmi);
        assertEquals(22.86, bmi, 0.01);
    }

    /**
     * Tests BMI calculation at the underweight threshold boundary.
     *
     * <p>This boundary test verifies behavior near the clinical threshold of BMI = 18.5,
     * which separates "Underweight" from "Normal weight" categories. Testing at boundaries
     * is critical because conditional logic often has off-by-one errors at threshold values.</p>
     *
     * <p><strong>Medical Context:</strong> BMI &lt; 18.5 is classified as underweight by
     * the WHO and may indicate malnutrition or health risks. Accurate boundary detection
     * ensures users receive appropriate category classifications for health assessments.</p>
     *
     * <p><strong>Test Design:</strong> Uses weight=55kg and height=173cm to produce BMI ≈ 18.37,
     * just below the 18.5 threshold. The assertion verifies the result falls within the
     * expected underweight range (18.0-19.0), providing some flexibility while ensuring
     * the calculation is in the correct ballpark.</p>
     */
    @Test
    @DisplayName("calculateBMI - Boundary: Underweight threshold (18.5)")
    void testCalculateBMI_BoundaryUnderweight() {
        // Given: Values that result in BMI ~18.5
        Double weight = 55.0;
        Double height = 173.0;

        // When
        Double bmi = personService.calculateBMI(weight, height);

        // Then
        assertNotNull(bmi);
        assertTrue(bmi < 19.0 && bmi > 18.0);
    }

    /**
     * Tests BMI calculation at the overweight threshold boundary.
     *
     * <p>This boundary test validates calculation accuracy at BMI = 25, the threshold
     * between "Normal weight" and "Overweight" categories. This is one of the most
     * clinically significant boundaries as it marks the point where health risks
     * begin to increase according to epidemiological studies.</p>
     *
     * <p><strong>Health Implications:</strong> BMI ≥ 25 is associated with increased
     * cardiovascular risks and is often used as a trigger for recommending dietary
     * changes or increased physical activity. Accurate classification here directly
     * impacts fitness program recommendations.</p>
     *
     * <p><strong>Test Values:</strong> Weight=80kg, height=179cm produces BMI ≈ 24.97,
     * right at the threshold. The test allows a small range (24.5-25.5) to account for
     * floating-point arithmetic while ensuring the value clusters around the critical
     * threshold.</p>
     */
    @Test
    @DisplayName("calculateBMI - Boundary: Overweight threshold (25)")
    void testCalculateBMI_BoundaryOverweight() {
        // Given: Values that result in BMI ~25
        Double weight = 80.0;
        Double height = 179.0;

        // When
        Double bmi = personService.calculateBMI(weight, height);

        // Then
        assertNotNull(bmi);
        assertTrue(bmi >= 24.5 && bmi <= 25.5);
    }

    /**
     * Tests BMI calculation with null weight parameter.
     *
     * <p>This invalid case test verifies graceful handling of missing weight data.
     * Rather than throwing a NullPointerException, the service should return null
     * to indicate the calculation cannot be performed. This null-safe design prevents
     * crashes and allows controllers to handle missing data appropriately.</p>
     *
     * <p><strong>Design Philosophy:</strong> Returning null instead of throwing exceptions
     * for business logic failures is a deliberate choice that:</p>
     * <ul>
     *   <li>Allows controllers to distinguish between validation failures (400) vs
     *       server errors (500)</li>
     *   <li>Enables partial data scenarios where some calculations succeed and others
     *       return null</li>
     *   <li>Simplifies error handling at the service boundary</li>
     * </ul>
     */
    @Test
    @DisplayName("calculateBMI - Invalid: Null weight")
    void testCalculateBMI_InvalidNullWeight() {
        // Given
        Double weight = null;
        Double height = 175.0;

        // When
        Double bmi = personService.calculateBMI(weight, height);

        // Then
        assertNull(bmi);
    }

    /**
     * Tests BMI calculation with null height parameter.
     *
     * <p>Similar to the null weight test, this verifies null-safety for the height parameter.
     * BMI calculation requires both weight and height; missing either should result in
     * null return rather than an exception.</p>
     *
     * <p><strong>Defensive Programming:</strong> Validating both parameters independently
     * ensures the service is resilient to partial data and doesn't make assumptions about
     * input completeness.</p>
     */
    @Test
    @DisplayName("calculateBMI - Invalid: Null height")
    void testCalculateBMI_InvalidNullHeight() {
        // Given
        Double weight = 70.0;
        Double height = null;

        // When
        Double bmi = personService.calculateBMI(weight, height);

        // Then
        assertNull(bmi);
    }

    /**
     * Tests BMI calculation with zero height to prevent division by zero.
     *
     * <p>This critical invalid case tests protection against division by zero errors.
     * Since BMI formula divides weight by height squared, height=0 would cause
     * ArithmeticException or Infinity. The service must detect this edge case and
     * return null instead.</p>
     *
     * <p><strong>Error Prevention:</strong> Division by zero is a classic programming
     * error. This test ensures the service validates denominators before calculation,
     * preventing runtime exceptions that could crash the application.</p>
     *
     * <p><strong>Real-World Context:</strong> While zero height is physically impossible,
     * it could occur due to data entry errors, corrupted database records, or
     * uninitialized variables. Robust handling prevents cascading failures.</p>
     */
    @Test
    @DisplayName("calculateBMI - Invalid: Zero height")
    void testCalculateBMI_InvalidZeroHeight() {
        // Given
        Double weight = 70.0;
        Double height = 0.0;

        // When
        Double bmi = personService.calculateBMI(weight, height);

        // Then
        assertNull(bmi);
    }

    // ============================================
    // calculateAge() Tests
    // ============================================

    /**
     * Tests age calculation with a birth date 30 years in the past.
     *
     * <p>This valid case verifies accurate age calculation using Java's Period.between()
     * method, which correctly handles leap years, varying month lengths, and calendar
     * complexities. Age calculation is fundamental for BMR formulas and age-specific
     * fitness recommendations.</p>
     *
     * <p><strong>Calculation Method:</strong> Uses LocalDate.now().minusYears(30) to
     * dynamically compute a birth date, ensuring the test remains valid regardless of
     * when it's executed. This approach is superior to hardcoded dates that become
     * outdated or require maintenance.</p>
     *
     * <p><strong>Business Importance:</strong> Age affects BMR (decreases ~2% per decade),
     * exercise intensity recommendations, and nutritional needs. Accurate age calculation
     * is therefore critical for personalized fitness planning.</p>
     */
    @Test
    @DisplayName("calculateAge - Valid: Birth date 30 years ago")
    void testCalculateAge_Valid() {
        // Given: Birth date 30 years ago
        LocalDate birthDate = LocalDate.now().minusYears(30);

        // When
        Integer age = personService.calculateAge(birthDate);

        // Then
        assertNotNull(age);
        assertEquals(30, age);
    }

    /**
     * Tests age calculation boundary case where birth date is today (age 0).
     *
     * <p>This boundary test ensures the service correctly handles newborn cases, returning
     * age 0 rather than negative values or errors. While less common in adult fitness
     * applications, this edge case is important for applications supporting infant health
     * tracking or comprehensive family fitness management.</p>
     *
     * <p><strong>Edge Case Significance:</strong> Same-day age calculation exercises the
     * lower bound of the Period.between() logic, ensuring it doesn't have off-by-one
     * errors when start and end dates are identical.</p>
     */
    @Test
    @DisplayName("calculateAge - Boundary: Born today (age 0)")
    void testCalculateAge_BoundaryToday() {
        // Given: Born today
        LocalDate birthDate = LocalDate.now();

        // When
        Integer age = personService.calculateAge(birthDate);

        // Then
        assertNotNull(age);
        assertEquals(0, age);
    }

    /**
     * Tests age calculation for someone born exactly one year ago.
     *
     * <p>This boundary test verifies correct handling of the transition from age 0 to
     * age 1, ensuring the year calculation accounts for the full 365/366 days. This
     * validates that the service correctly uses year-based age rather than day-based
     * or month-based calculations.</p>
     *
     * <p><strong>Test Value:</strong> The small age values (0 and 1) are particularly
     * important to test because they're where calculation logic is most likely to have
     * special cases or errors.</p>
     */
    @Test
    @DisplayName("calculateAge - Boundary: Born 1 year ago")
    void testCalculateAge_BoundaryOneYear() {
        // Given: Born exactly 1 year ago
        LocalDate birthDate = LocalDate.now().minusYears(1);

        // When
        Integer age = personService.calculateAge(birthDate);

        // Then
        assertNotNull(age);
        assertEquals(1, age);
    }

    /**
     * Tests age calculation with null birth date parameter.
     *
     * <p>This invalid case verifies null-safety, ensuring the service returns null
     * rather than throwing NullPointerException when birth date is missing. This
     * follows the same defensive programming pattern as other calculation methods.</p>
     *
     * <p><strong>Graceful Degradation:</strong> Returning null allows the application
     * to continue functioning even with incomplete user profiles, enabling partial
     * calculations while clearly indicating which data is missing.</p>
     */
    @Test
    @DisplayName("calculateAge - Invalid: Null birth date")
    void testCalculateAge_InvalidNull() {
        // Given
        LocalDate birthDate = null;

        // When
        Integer age = personService.calculateAge(birthDate);

        // Then
        assertNull(age);
    }

    // ============================================
    // calculateBMR() Tests
    // ============================================

    /**
     * Tests BMR (Basal Metabolic Rate) calculation for a male with typical values.
     *
     * <p>This valid case verifies the Mifflin-St Jeor equation for males:
     * BMR = (10 × weight_kg) + (6.25 × height_cm) - (5 × age) + 5</p>
     *
     * <p>For a 70kg, 175cm, 30-year-old male:
     * BMR = (10 × 70) + (6.25 × 175) - (5 × 30) + 5
     *     = 700 + 1093.75 - 150 + 5 = 1648.75 kcal/day</p>
     *
     * <p><strong>Clinical Significance:</strong> BMR represents the minimum calories
     * needed to maintain basic physiological functions (breathing, circulation, cell
     * production) at rest. This is the foundation for calculating total daily energy
     * expenditure (TDEE) and setting dietary targets.</p>
     *
     * <p><strong>Gender-Specific Formula:</strong> The male formula adds +5 to account
     * for higher average muscle mass and metabolic rate compared to females (-161).</p>
     */
    @Test
    @DisplayName("calculateBMR - Valid: Male with normal values")
    void testCalculateBMR_ValidMale() {
        // Given: Male, 70kg, 175cm, 30 years
        Double weight = 70.0;
        Double height = 175.0;
        Integer age = 30;
        boolean isMale = true;

        // When
        Double bmr = personService.calculateBMR(weight, height, age, isMale);

        // Then: Male formula
        assertNotNull(bmr);
        assertTrue(bmr > 1600 && bmr < 1700); // Expected range
    }

    /**
     * Tests BMR calculation for a female with typical values.
     *
     * <p>This valid case verifies the Mifflin-St Jeor equation for females:
     * BMR = (10 × weight_kg) + (6.25 × height_cm) - (5 × age) - 161</p>
     *
     * <p>For a 60kg, 165cm, 25-year-old female:
     * BMR = (10 × 60) + (6.25 × 165) - (5 × 25) - 161
     *     = 600 + 1031.25 - 125 - 161 = 1345.25 kcal/day</p>
     *
     * <p><strong>Gender Differences:</strong> The female formula subtracts 161 instead
     * of adding 5 (a 166 kcal difference), reflecting biological differences in body
     * composition, muscle mass, and hormonal influences on metabolism. This difference
     * is critical for accurate nutritional recommendations tailored to female physiology.</p>
     *
     * <p><strong>Test Independence:</strong> Both male and female formulas must be tested
     * separately to ensure the isMale boolean parameter correctly routes to the
     * appropriate calculation path.</p>
     */
    @Test
    @DisplayName("calculateBMR - Valid: Female with normal values")
    void testCalculateBMR_ValidFemale() {
        // Given: Female, 60kg, 165cm, 25 years
        Double weight = 60.0;
        Double height = 165.0;
        Integer age = 25;
        boolean isMale = false;

        // When
        Double bmr = personService.calculateBMR(weight, height, age, isMale);

        // Then: Female formula
        assertNotNull(bmr);
        assertTrue(bmr > 1300 && bmr < 1500); // Expected range
    }

    /**
     * Tests BMR calculation boundary case with age = 0 (newborn).
     *
     * <p>This boundary test verifies the formula handles the minimum age value without
     * special conditions or errors. For infants, the Mifflin-St Jeor equation (designed
     * for adults) may not be clinically accurate, but the service should still compute
     * a result without failing.</p>
     *
     * <p><strong>Edge Case Handling:</strong> Age 0 tests whether the formula's age
     * coefficient (-5 × age) correctly evaluates to 0, and whether other parameters
     * (low weight, low height) are handled properly.</p>
     *
     * <p><strong>Clinical Note:</strong> In real applications, infant BMR would use
     * different formulas (e.g., Schofield equations), but this test validates that
     * the service doesn't crash on edge cases outside its primary use case.</p>
     */
    @Test
    @DisplayName("calculateBMR - Boundary: Age 0")
    void testCalculateBMR_BoundaryAgeZero() {
        // Given: Age 0
        Double weight = 5.0;
        Double height = 50.0;
        Integer age = 0;
        boolean isMale = true;

        // When
        Double bmr = personService.calculateBMR(weight, height, age, isMale);

        // Then: Should still calculate
        assertNotNull(bmr);
        assertTrue(bmr > 0);
    }

    /**
     * Tests BMR calculation with null weight parameter.
     *
     * <p>Validates null-safety for weight input. BMR calculation requires all three
     * parameters (weight, height, age); missing weight should return null rather than
     * throwing exceptions.</p>
     */
    @Test
    @DisplayName("calculateBMR - Invalid: Null weight")
    void testCalculateBMR_InvalidNullWeight() {
        // Given
        Double weight = null;
        Double height = 175.0;
        Integer age = 30;
        boolean isMale = true;

        // When
        Double bmr = personService.calculateBMR(weight, height, age, isMale);

        // Then
        assertNull(bmr);
    }

    /**
     * Tests BMR calculation with null height parameter.
     *
     * <p>Validates null-safety for height input. Similar to null weight test, ensures
     * all required parameters are validated independently.</p>
     */
    @Test
    @DisplayName("calculateBMR - Invalid: Null height")
    void testCalculateBMR_InvalidNullHeight() {
        // Given
        Double weight = 70.0;
        Double height = null;
        Integer age = 30;
        boolean isMale = true;

        // When
        Double bmr = personService.calculateBMR(weight, height, age, isMale);

        // Then
        assertNull(bmr);
    }

    /**
     * Tests BMR calculation with null age parameter.
     *
     * <p>Validates null-safety for age input. Age is critical to the BMR formula
     * (affects the -5 × age term), so null age should prevent calculation and
     * return null.</p>
     */
    @Test
    @DisplayName("calculateBMR - Invalid: Null age")
    void testCalculateBMR_InvalidNullAge() {
        // Given
        Double weight = 70.0;
        Double height = 175.0;
        Integer age = null;
        boolean isMale = true;

        // When
        Double bmr = personService.calculateBMR(weight, height, age, isMale);

        // Then
        assertNull(bmr);
    }

    // ============================================
    // calculateDailyCalorieNeeds() Tests
    // ============================================

    /**
     * Tests daily calorie needs calculation for sedentary lifestyle (0 training days).
     *
     * <p>This valid case verifies the Harris-Benedict equation's sedentary multiplier:
     * Daily Calories = BMR × 1.2 (for little or no exercise)</p>
     *
     * <p>With BMR = 1680 kcal/day:
     * Daily Calories = 1680 × 1.2 = 2016 kcal/day</p>
     *
     * <p><strong>Activity Levels:</strong> The Harris-Benedict equation uses activity
     * multipliers to adjust BMR for lifestyle:</p>
     * <ul>
     *   <li>1.2 = Sedentary (little/no exercise)</li>
     *   <li>1.375 = Light (1-3 days/week)</li>
     *   <li>1.55 = Moderate (3-5 days/week)</li>
     *   <li>1.725 = Very Active (6-7 days/week)</li>
     *   <li>1.9 = Extra Active (twice per day, intense training)</li>
     * </ul>
     *
     * <p><strong>Nutritional Planning:</strong> This calculation determines the total
     * daily energy expenditure (TDEE), which forms the baseline for weight management:
     * eating below TDEE leads to weight loss, above TDEE leads to weight gain.</p>
     */
    @Test
    @DisplayName("calculateDailyCalorieNeeds - Valid: Sedentary (0 days)")
    void testCalculateDailyCalorieNeeds_ValidSedentary() {
        // Given: BMR 1680, 0 training days
        Double bmr = 1680.0;
        Integer weeklyTrainingFreq = 0;

        // When
        Double dailyCalories = personService.calculateDailyCalorieNeeds(bmr, weeklyTrainingFreq);

        // Then: 1680 * 1.2
        assertNotNull(dailyCalories);
        assertEquals(2016.0, dailyCalories, 0.1);
    }

    /**
     * Tests daily calorie calculation for moderate activity level (4 training days/week).
     *
     * <p>Validates the 1.55 multiplier for moderate exercise (3-5 days/week):
     * 1680 × 1.55 = 2604 kcal/day</p>
     *
     * <p><strong>Typical User:</strong> Represents someone with regular gym attendance
     * or consistent cardio routine - the most common fitness level for active individuals.</p>
     */
    @Test
    @DisplayName("calculateDailyCalorieNeeds - Valid: Moderate activity (4 days)")
    void testCalculateDailyCalorieNeeds_ValidModerate() {
        // Given: BMR 1680, 4 training days
        Double bmr = 1680.0;
        Integer weeklyTrainingFreq = 4;

        // When
        Double dailyCalories = personService.calculateDailyCalorieNeeds(bmr, weeklyTrainingFreq);

        // Then: 1680 * 1.55
        assertNotNull(dailyCalories);
        assertEquals(2604.0, dailyCalories, 0.1);
    }

    /**
     * Tests daily calorie calculation for very active lifestyle (6 training days/week).
     *
     * <p>Validates the 1.725 multiplier for very active exercise (6-7 days/week):
     * 1680 × 1.725 = 2898 kcal/day</p>
     *
     * <p><strong>Athletic Level:</strong> Represents dedicated athletes or serious
     * fitness enthusiasts who train almost daily.</p>
     */
    @Test
    @DisplayName("calculateDailyCalorieNeeds - Valid: Very active (6 days)")
    void testCalculateDailyCalorieNeeds_ValidVeryActive() {
        // Given: BMR 1680, 6 training days
        Double bmr = 1680.0;
        Integer weeklyTrainingFreq = 6;

        // When
        Double dailyCalories = personService.calculateDailyCalorieNeeds(bmr, weeklyTrainingFreq);

        // Then: 1680 * 1.725
        assertNotNull(dailyCalories);
        assertEquals(2898.0, dailyCalories, 0.1);
    }

    /**
     * Tests boundary at 2 training days/week (light activity threshold).
     *
     * <p>Validates the 1.375 multiplier for light activity (1-3 days/week):
     * 1680 × 1.375 = 2310 kcal/day</p>
     *
     * <p><strong>Boundary Significance:</strong> Tests the threshold between sedentary
     * and light activity categories, ensuring correct multiplier selection.</p>
     */
    @Test
    @DisplayName("calculateDailyCalorieNeeds - Boundary: Threshold at 2 days")
    void testCalculateDailyCalorieNeeds_BoundaryTwoDays() {
        // Given: 2 training days (light activity threshold)
        Double bmr = 1680.0;
        Integer weeklyTrainingFreq = 2;

        // When
        Double dailyCalories = personService.calculateDailyCalorieNeeds(bmr, weeklyTrainingFreq);

        // Then: 1680 * 1.375
        assertNotNull(dailyCalories);
        assertEquals(2310.0, dailyCalories, 0.1);
    }

    /**
     * Tests boundary for extremely high training frequency (8+ days/week).
     *
     * <p>Validates the 1.9 multiplier for extra active lifestyle (training twice per day):
     * 1680 × 1.9 = 3192 kcal/day</p>
     *
     * <p><strong>Elite Athletes:</strong> Represents professional athletes or bodybuilders
     * with multiple daily training sessions. Tests upper bound handling.</p>
     */
    @Test
    @DisplayName("calculateDailyCalorieNeeds - Boundary: High frequency (8+ days)")
    void testCalculateDailyCalorieNeeds_BoundaryExtraActive() {
        // Given: 8 training days
        Double bmr = 1680.0;
        Integer weeklyTrainingFreq = 8;

        // When
        Double dailyCalories = personService.calculateDailyCalorieNeeds(bmr, weeklyTrainingFreq);

        // Then: 1680 * 1.9 (extra active)
        assertNotNull(dailyCalories);
        assertEquals(3192.0, dailyCalories, 0.1);
    }

    /**
     * Tests daily calorie calculation with null BMR input.
     *
     * <p>Validates null-safety when BMR cannot be calculated (e.g., from null weight/height).
     * Without BMR, daily calorie needs cannot be computed, so should return null.</p>
     */
    @Test
    @DisplayName("calculateDailyCalorieNeeds - Invalid: Null BMR")
    void testCalculateDailyCalorieNeeds_InvalidNullBMR() {
        // Given
        Double bmr = null;
        Integer weeklyTrainingFreq = 4;

        // When
        Double dailyCalories = personService.calculateDailyCalorieNeeds(bmr, weeklyTrainingFreq);

        // Then
        assertNull(dailyCalories);
    }

    /**
     * Tests daily calorie calculation with null training frequency.
     *
     * <p>Validates null-safety for activity level parameter. Without knowing training
     * frequency, the appropriate activity multiplier cannot be selected, so the
     * calculation should return null rather than assuming a default value.</p>
     */
    @Test
    @DisplayName("calculateDailyCalorieNeeds - Invalid: Null frequency")
    void testCalculateDailyCalorieNeeds_InvalidNullFrequency() {
        // Given
        Double bmr = 1680.0;
        Integer weeklyTrainingFreq = null;

        // When
        Double dailyCalories = personService.calculateDailyCalorieNeeds(bmr, weeklyTrainingFreq);

        // Then
        assertNull(dailyCalories);
    }
}
