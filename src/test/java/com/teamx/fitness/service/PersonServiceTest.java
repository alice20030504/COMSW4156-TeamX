package com.teamx.fitness.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PersonService.
 * Tests cover Valid, Boundary, and Invalid cases for each method.
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
