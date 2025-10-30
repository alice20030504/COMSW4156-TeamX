package com.teamx.fitness.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

/**
 * Service for managing person-related operations.
 * Provides business logic for fitness calculations.
 */
@Service
public class PersonService {
    /** Male BMR base constant (Harris-Benedict). */
    private static final double MALE_BMR_BASE = 88.362;

    /** Male BMR weight factor (Harris-Benedict). */
    private static final double MALE_BMR_WEIGHT = 13.397;

    /** Male BMR height factor (Harris-Benedict). */
    private static final double MALE_BMR_HEIGHT = 4.799;

    /** Male BMR age factor (Harris-Benedict). */
    private static final double MALE_BMR_AGE = 5.677;

    /** Female BMR base constant (Harris-Benedict). */
    private static final double FEMALE_BMR_BASE = 447.593;

    /** Female BMR weight factor (Harris-Benedict). */
    private static final double FEMALE_BMR_WEIGHT = 9.247;

    /** Female BMR height factor (Harris-Benedict). */
    private static final double FEMALE_BMR_HEIGHT = 3.098;

    /** Female BMR age factor (Harris-Benedict). */
    private static final double FEMALE_BMR_AGE = 4.330;

    /** Activity factor for sedentary (0 training days). */
    private static final double ACTIVITY_SEDENTARY = 1.2;

    /** Activity factor for light activity (1-2 training days). */
    private static final double ACTIVITY_LIGHT = 1.375;

    /** Activity factor for moderate activity (3-4 training days). */
    private static final double ACTIVITY_MODERATE = 1.55;

    /** Activity factor for very active (5-6 training days). */
    private static final double ACTIVITY_VERY = 1.725;

    /** Activity factor for extra active (7+ training days). */
    private static final double ACTIVITY_EXTRA = 1.9;

    /** Maximum training days for moderate activity. */
    private static final int MAX_MODERATE_TRAINING = 4;

    /** Maximum training days for very active activity. */
    private static final int MAX_VERY_ACTIVE_TRAINING = 6;

    /**
     * Calculate BMI (Body Mass Index).
     * Formula: BMI = weight(kg) / (height(m))^2
     *
     * @param weight weight in kilograms
     * @param height height in centimeters
     * @return calculated BMI
     */
    public Double calculateBMI(Double weight, Double height) {
        if (weight == null || height == null || height == 0) {
            return null;
        }
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
    }

    /**
     * Calculate age from birth date.
     *
     * @param birthDate person's birth date
     * @return age in years
     */
    public Integer calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Calculate Basal Metabolic Rate (BMR) using Harris-Benedict equation.
     * Men: BMR = 88.362 + (13.397 * weight in kg) + (4.799 * height in cm) - (5.677 * age in years)
     * Women: BMR = 447.593 + (9.247 * weight in kg) + (3.098 * height in cm) - (4.330 * age in years)
     *
     * @param weight weight in kilograms
     * @param height height in centimeters
     * @param age age in years
     * @param isMale true for male, false for female
     * @return calculated BMR
     */
    public Double calculateBMR(Double weight, Double height, Integer age, boolean isMale) {
        if (weight == null || height == null || age == null) {
            return null;
        }

        if (isMale) {
            return MALE_BMR_BASE + (MALE_BMR_WEIGHT * weight) + (MALE_BMR_HEIGHT * height) - (MALE_BMR_AGE * age);
        } else {
            return FEMALE_BMR_BASE + (FEMALE_BMR_WEIGHT * weight) + (FEMALE_BMR_HEIGHT * height)
                    - (FEMALE_BMR_AGE * age);
        }
    }

    /**
     * Calculate daily calorie needs based on BMR and activity level.
     *
     * @param bmr Basal Metabolic Rate
     * @param weeklyTrainingFreq weekly training frequency
     * @return daily calorie needs
     */
    public Double calculateDailyCalorieNeeds(Double bmr, Integer weeklyTrainingFreq) {
        if (bmr == null || weeklyTrainingFreq == null) {
            return null;
        }

        double activityFactor;
        if (weeklyTrainingFreq == 0) {
            activityFactor = ACTIVITY_SEDENTARY;
        } else if (weeklyTrainingFreq <= 2) {
            activityFactor = ACTIVITY_LIGHT;
        } else if (weeklyTrainingFreq <= MAX_MODERATE_TRAINING) {
            activityFactor = ACTIVITY_MODERATE;
        } else if (weeklyTrainingFreq <= MAX_VERY_ACTIVE_TRAINING) {
            activityFactor = ACTIVITY_VERY;
        } else {
            activityFactor = ACTIVITY_EXTRA;
        }

        return bmr * activityFactor;
    }
}

