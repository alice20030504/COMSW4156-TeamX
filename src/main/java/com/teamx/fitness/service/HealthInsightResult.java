package com.teamx.fitness.service;

/**
 * Encapsulates derived health metrics for a person.
 *
 * @param bmi calculated body mass index
 * @param bmiCategory textual category for BMI
 * @param healthIndex BMI-oriented score (0-100)
 * @param planAlignmentIndex plan alignment score (0-100)
 * @param overallScore combined score surfaced to users
 * @param percentile percentile compared to anonymous cohort (nullable)
 * @param cohortWarning message when percentile cannot be computed (nullable)
 * @param recommendation tailored guidance for the user
 */
public record HealthInsightResult(
    Double bmi,
    String bmiCategory,
    double healthIndex,
    double planAlignmentIndex,
    double overallScore,
    Double percentile,
    String cohortWarning,
    String recommendation) { }
