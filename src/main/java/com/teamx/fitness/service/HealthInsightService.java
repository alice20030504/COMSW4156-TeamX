package com.teamx.fitness.service;

import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.PlanStrategy;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.repository.PersonRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Generates health indices, cohort percentiles, and tailored recommendations.
 */
@Service
public class HealthInsightService {

  /** BMI threshold below which a user is deemed underweight. */
  private static final double BMI_UNDERWEIGHT = 18.5;
  /** BMI threshold marking the start of the normal weight range. */
  private static final double BMI_NORMAL = 25.0;
  /** BMI threshold separating overweight from obese categories. */
  private static final double BMI_OVERWEIGHT = 30.0;
  /** Maximum score returned to clients. */
  private static final double MAX_OVERALL_SCORE = 100.0;
  /** Weight given to the health index contribution. */
  private static final double HEALTH_WEIGHT = 0.6;
  /** Weight given to the plan-alignment contribution. */
  private static final double PLAN_WEIGHT = 0.4;
  /** Small offset to avoid floating-point rounding surprises in percentiles. */
  private static final double PERCENTILE_EPSILON = 1e-6;
  /** Factor used when rounding derived metrics to a single decimal place. */
  private static final double ROUNDING_FACTOR = 10.0;

  /** Baseline score used when BMI is categorised as underweight. */
  private static final double BMI_SCORE_UNDERWEIGHT_VALUE = 45.0;
  /** Baseline score used when BMI falls in the normal range. */
  private static final double BMI_SCORE_NORMAL_VALUE = 70.0;
  /** Baseline score used when BMI indicates overweight. */
  private static final double BMI_SCORE_OVERWEIGHT_VALUE = 55.0;
  /** Baseline score used when BMI indicates obesity. */
  private static final double BMI_SCORE_OBESE_VALUE = 35.0;

  /** Maximum number of bonus points granted for training frequency. */
  private static final double TRAINING_SCORE_MAX = 20.0;
  /** Points awarded per weekly training session. */
  private static final double TRAINING_SCORE_STEP = 4.0;
  /** Bonus when no plan strategy is specified. */
  private static final double STRATEGY_SCORE_NONE = 5.0;
  /** Bonus when both diet and workout strategies are combined. */
  private static final double STRATEGY_SCORE_BOTH = 10.0;
  /** Bonus when the plan strategy matches the active goal. */
  private static final double STRATEGY_SCORE_MATCH = 8.0;
  /** Bonus when the plan strategy mismatches the goal. */
  private static final double STRATEGY_SCORE_MISMATCH = 5.0;

  /** Neutral suitability score returned for most combinations. */
  private static final double SUITABILITY_DEFAULT = 20.0;
  /** Maximum suitability score when goal perfectly matches BMI range. */
  private static final double SUITABILITY_MAX = 40.0;
  /** Suitability score for overweight cutters. */
  private static final double SUITABILITY_OVERWEIGHT = 35.0;
  /** Suitability score for normal-weight bulkers. */
  private static final double SUITABILITY_NORMAL_WEIGHT = 30.0;
  /** Partial suitability score for mildly mismatched goals. */
  private static final double SUITABILITY_PARTIAL = 15.0;
  /** Minimum suitability score for obviously mismatched goals. */
  private static final double SUITABILITY_MIN = 5.0;

  /** Default intensity when the plan lacks change and duration data. */
  private static final double PLAN_INTENSITY_BASE = 15.0;
  /** Score when the plan attempts a very rapid rate of change. */
  private static final double PLAN_INTENSITY_FAST = 10.0;
  /** Score for moderately aggressive plans. */
  private static final double PLAN_INTENSITY_MODERATE = 20.0;
  /** Score for steady-paced plans. */
  private static final double PLAN_INTENSITY_STEADY = 30.0;
  /** Score when the plan change rate is very light. */
  private static final double PLAN_INTENSITY_LIGHT = 18.0;
  /** Weekly change rate considered very high. */
  private static final double RATE_HIGH = 1.0;
  /** Weekly change rate considered moderate. */
  private static final double RATE_MEDIUM = 0.7;
  /** Weekly change rate indicative of a steady approach. */
  private static final double RATE_STEADY = 0.2;

  /** Weekly training frequency treated as heavy. */
  private static final int FREQ_HEAVY = 5;
  /** Weekly training frequency treated as moderate. */
  private static final int FREQ_MODERATE = 3;
  /** Minimum weekly training frequency that counts as active. */
  private static final int FREQ_MIN = 1;
  /** Weekly training frequency seen as high for diet-only plans. */
  private static final int FREQ_DIET_HIGH = 4;
  /** Weekly training frequency seen as moderate for diet-only plans. */
  private static final int FREQ_DIET_MED = 2;

  /** Consistency score for very high training adherence. */
  private static final double CONSISTENCY_TOP = 20.0;
  /** Consistency score for strong training adherence. */
  private static final double CONSISTENCY_STRONG = 17.0;
  /** Consistency score for basic recommended adherence. */
  private static final double CONSISTENCY_BASE = 12.0;
  /** Consistency score applied when adherence is minimal. */
  private static final double CONSISTENCY_LOW = 5.0;
  /** Consistency score for high frequency in diet-focused plans. */
  private static final double CONSISTENCY_DIET_HIGH = 16.0;
  /** Consistency score for moderate frequency in diet-focused plans. */
  private static final double CONSISTENCY_DIET_MED = 12.0;
  /** Consistency score for light frequency in diet-focused plans. */
  private static final double CONSISTENCY_DIET_LIGHT = 8.0;
  /** Consistency score when diet-focused plans have minimal training. */
  private static final double CONSISTENCY_DIET_MIN = 6.0;

  /** Bonus when a plan balances both diet and workout strategies. */
  private static final double BALANCE_SCORE_HIGH = 10.0;
  /** Bonus when a plan's single strategy still suits the BMI. */
  private static final double BALANCE_SCORE_POSITIVE = 8.0;
  /** Bonus when the plan's strategy is neutral for the BMI. */
  private static final double BALANCE_SCORE_NEUTRAL = 6.0;

  /** Weekly change threshold warning for aggressive cutting plans. */
  private static final double CUT_RATE_WARNING = 0.9;
  /** Weekly change threshold warning for aggressive bulking plans. */
  private static final double BULK_RATE_WARNING = 0.6;
  /** Score threshold used to flag strong overall trajectories. */
  private static final double SCORE_STRONG_THRESHOLD = 80.0;
  /** Plan-alignment threshold used with strong trajectories. */
  private static final double PLAN_ALIGNMENT_STRONG_THRESHOLD = 70.0;
  /** Score threshold indicating a potentially risky trajectory. */
  private static final double SCORE_LOW_THRESHOLD = 50.0;

  /** Calculator reused from PersonService for BMI and calorie helpers. */
  private final PersonService personService;
  /** Repository access for gathering cohort data. */
  private final PersonRepository personRepository;
  /** Minimum cohort size required before percentiles are emitted. */
  private final int minCohortSize;

  public HealthInsightService(
      PersonService personService,
      PersonRepository personRepository,
      @Value("${app.fitness.research.min-cohort-size:10}") int minCohortSize) {
    this.personService = personService;
    this.personRepository = personRepository;
    this.minCohortSize = minCohortSize;
  }

  /**
   * Builds derived metrics for the supplied person.
   *
   * @param person persisted profile
   * @return computed insight metrics
   */
  public HealthInsightResult buildInsights(PersonSimple person) {
    Double bmi = personService.calculateBMI(person.getWeight(), person.getHeight());
    String bmiCategory = categorizeBmi(bmi);

    double healthIndex = calculateHealthIndex(person, bmi);
    double planAlignmentIndex = calculatePlanAlignmentIndex(person, bmiCategory);
    double overallScore = roundToOne(
        clamp(healthIndex * HEALTH_WEIGHT + planAlignmentIndex * PLAN_WEIGHT, 0, MAX_OVERALL_SCORE));

    CohortSnapshot cohortSnapshot = buildCohortSnapshot(overallScore);

    String recommendation =
        buildRecommendation(person, bmi, bmiCategory, planAlignmentIndex, overallScore);

    return new HealthInsightResult(
        roundToOne(bmi),
        bmiCategory,
        healthIndex,
        planAlignmentIndex,
        overallScore,
        cohortSnapshot.percentile,
        cohortSnapshot.warning,
        recommendation);
  }

  private CohortSnapshot buildCohortSnapshot(double personScore) {
    List<Double> cohortScores = new ArrayList<>();
    for (PersonSimple peer : personRepository.findAll()) {
      Double bmi = safeBmi(peer);
      if (bmi == null) {
        continue;
      }
      double peerHealth = calculateHealthIndex(peer, bmi);
      double peerPlan = calculatePlanAlignmentIndex(peer, categorizeBmi(bmi));
      double peerScore = clamp(peerHealth * HEALTH_WEIGHT + peerPlan * PLAN_WEIGHT, 0, MAX_OVERALL_SCORE);
      cohortScores.add(peerScore);
    }
    cohortScores.removeIf(Objects::isNull);

    if (cohortScores.size() < minCohortSize) {
      return new CohortSnapshot(null,
          "Need at least " + minCohortSize + " profiles for percentile comparison.");
    }

    long belowOrEqual = cohortScores.stream()
        .filter(score -> score <= personScore + PERCENTILE_EPSILON)
        .count();
    double percentile = roundToOne((belowOrEqual * 100.0) / cohortScores.size());
    return new CohortSnapshot(percentile, null);
  }

  private Double safeBmi(PersonSimple person) {
    try {
      return personService.calculateBMI(person.getWeight(), person.getHeight());
    } catch (ResponseStatusException ex) {
      return null;
    }
  }

  private double calculateHealthIndex(PersonSimple person, double bmi) {
    double bmiScore;
    if (bmi < BMI_UNDERWEIGHT) {
      bmiScore = BMI_SCORE_UNDERWEIGHT_VALUE;
    } else if (bmi < BMI_NORMAL) {
      bmiScore = BMI_SCORE_NORMAL_VALUE;
    } else if (bmi < BMI_OVERWEIGHT) {
      bmiScore = BMI_SCORE_OVERWEIGHT_VALUE;
    } else {
      bmiScore = BMI_SCORE_OBESE_VALUE;
    }

    int frequency = person.getTrainingFrequencyPerWeek() != null
        ? Math.max(person.getTrainingFrequencyPerWeek(), 0)
        : 0;
    double trainingScore = Math.min(TRAINING_SCORE_MAX, frequency * TRAINING_SCORE_STEP);

    double strategyScore;
    PlanStrategy strategy = person.getPlanStrategy();
    if (strategy == null) {
      strategyScore = STRATEGY_SCORE_NONE;
    } else {
      strategyScore = switch (strategy) {
        case BOTH -> STRATEGY_SCORE_BOTH;
        case WORKOUT -> person.getGoal() == FitnessGoal.BULK ? STRATEGY_SCORE_MATCH : STRATEGY_SCORE_MISMATCH;
        case DIET -> person.getGoal() == FitnessGoal.CUT ? STRATEGY_SCORE_MATCH : STRATEGY_SCORE_MISMATCH;
      };
    }

    return roundToOne(clamp(bmiScore + trainingScore + strategyScore, 0, MAX_OVERALL_SCORE));
  }

  private double calculatePlanAlignmentIndex(PersonSimple person, String bmiCategory) {
    double suitability = goalSuitabilityScore(person.getGoal(), bmiCategory);
    double intensity = planIntensityScore(person);
    double trainingConsistency = trainingConsistencyScore(person);
    double strategyBalance = strategyBalanceScore(person.getPlanStrategy(), bmiCategory);

    return roundToOne(
        clamp(suitability + intensity + trainingConsistency + strategyBalance, 0, MAX_OVERALL_SCORE));
  }

  private double goalSuitabilityScore(FitnessGoal goal, String bmiCategory) {
    if (goal == null) {
      return SUITABILITY_DEFAULT;
    }
    return switch (goal) {
      case CUT -> switch (bmiCategory) {
        case "Obese" -> SUITABILITY_MAX;
        case "Overweight" -> SUITABILITY_OVERWEIGHT;
        case "Normal weight" -> SUITABILITY_DEFAULT;
        default -> SUITABILITY_MIN;
      };
      case BULK -> switch (bmiCategory) {
        case "Underweight" -> SUITABILITY_MAX;
        case "Normal weight" -> SUITABILITY_NORMAL_WEIGHT;
        case "Overweight" -> SUITABILITY_PARTIAL;
        default -> SUITABILITY_MIN;
      };
    };
  }

  private double planIntensityScore(PersonSimple person) {
    Double change = person.getTargetChangeKg();
    Integer duration = person.getTargetDurationWeeks();
    if (change == null || duration == null || duration <= 0) {
      return PLAN_INTENSITY_BASE;
    }
    double weeklyChange = Math.abs(change) / duration;
    if (weeklyChange >= RATE_HIGH) {
      return PLAN_INTENSITY_FAST;
    }
    if (weeklyChange >= RATE_MEDIUM) {
      return PLAN_INTENSITY_MODERATE;
    }
    if (weeklyChange >= RATE_STEADY) {
      return PLAN_INTENSITY_STEADY;
    }
    return PLAN_INTENSITY_LIGHT;
  }

  private double trainingConsistencyScore(PersonSimple person) {
    PlanStrategy strategy = person.getPlanStrategy();
    int frequency = person.getTrainingFrequencyPerWeek() != null
        ? Math.max(person.getTrainingFrequencyPerWeek(), 0)
        : 0;
    if (strategy == PlanStrategy.WORKOUT || strategy == PlanStrategy.BOTH) {
      if (frequency >= FREQ_HEAVY) {
        return CONSISTENCY_TOP;
      } else if (frequency >= FREQ_MODERATE) {
        return CONSISTENCY_STRONG;
      } else if (frequency >= FREQ_MIN) {
        return CONSISTENCY_BASE;
      }
      return CONSISTENCY_LOW;
    }
    if (frequency >= FREQ_DIET_HIGH) {
      return CONSISTENCY_DIET_HIGH;
    } else if (frequency >= FREQ_DIET_MED) {
      return CONSISTENCY_DIET_MED;
    } else if (frequency >= FREQ_MIN) {
      return CONSISTENCY_DIET_LIGHT;
    }
    return CONSISTENCY_DIET_MIN;
  }

  private double strategyBalanceScore(PlanStrategy strategy, String bmiCategory) {
    if (strategy == null) {
      return STRATEGY_SCORE_NONE;
    }
    return switch (strategy) {
      case BOTH -> STRATEGY_SCORE_BOTH;
      case DIET -> "Overweight".equals(bmiCategory) || "Obese".equals(bmiCategory) ? BALANCE_SCORE_POSITIVE
          : BALANCE_SCORE_NEUTRAL;
      case WORKOUT -> "Underweight".equals(bmiCategory) ? BALANCE_SCORE_POSITIVE : BALANCE_SCORE_NEUTRAL;
    };
  }

  private String buildRecommendation(
      PersonSimple person,
      double bmi,
      String bmiCategory,
      double planAlignmentIndex,
      double overallScore) {

    FitnessGoal goal = person.getGoal();
    double weeklyChange = calculateWeeklyChange(person);
    int training = person.getTrainingFrequencyPerWeek() != null
        ? Math.max(person.getTrainingFrequencyPerWeek(), 0)
        : 0;
    String formattedBmi = String.format(Locale.US, "%.1f", bmi);

    if (goal == FitnessGoal.BULK && "Obese".equals(bmiCategory)) {
      return "Keep bulking cautiously: BMI is " + formattedBmi
          + " (obese). Consider a short CUT phase before resuming bulk work.";
    }
    if (goal == FitnessGoal.CUT && "Underweight".equals(bmiCategory)) {
      return "Keep prioritising recovery: BMI is " + formattedBmi
          + " (underweight). Shift toward maintenance or a lean bulk to rebuild.";
    }
    if (goal == FitnessGoal.CUT) {
      if (!Double.isNaN(weeklyChange) && weeklyChange > CUT_RATE_WARNING) {
        return "Cut target (" + formatRate(weeklyChange)
            + " kg/week) is very aggressive—slow the deficit to avoid burnout.";
      }
      return "Cutting effort is on track—keep protein high and aim for "
          + training + " focused sessions each week.";
    }
    if (goal == FitnessGoal.BULK) {
      if (!Double.isNaN(weeklyChange) && weeklyChange > BULK_RATE_WARNING) {
        return "Bulk rate (" + formatRate(weeklyChange)
            + " kg/week) may add unnecessary fat. Dial the surplus back slightly.";
      }
      return "Lean bulk focus looks good. Prioritize progressive overload and adequate sleep.";
    }

    if (overallScore >= SCORE_STRONG_THRESHOLD && planAlignmentIndex >= PLAN_ALIGNMENT_STRONG_THRESHOLD) {
      return "Solid balance between health metrics and your plan—maintain the current approach.";
    }
    if (overallScore < SCORE_LOW_THRESHOLD) {
      return "Overall score is trending low. Revisit goals and add structured training for a steadier trajectory.";
    }
    return "Stay consistent with the plan and review progress every few weeks.";
  }

  private double calculateWeeklyChange(PersonSimple person) {
    Double change = person.getTargetChangeKg();
    Integer duration = person.getTargetDurationWeeks();
    if (change == null || duration == null || duration <= 0) {
      return Double.NaN;
    }
    return Math.abs(change) / duration;
  }

  private double roundToOne(double value) {
    return Math.round(value * ROUNDING_FACTOR) / ROUNDING_FACTOR;
  }

  private double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  private String categorizeBmi(Double bmi) {
    if (bmi == null || Double.isNaN(bmi)) {
      return "Unknown";
    }
    if (bmi < BMI_UNDERWEIGHT) {
      return "Underweight";
    }
    if (bmi < BMI_NORMAL) {
      return "Normal weight";
    }
    if (bmi < BMI_OVERWEIGHT) {
      return "Overweight";
    }
    return "Obese";
  }

  private String formatRate(double weeklyChange) {
    return String.format(Locale.US, "%.2f", weeklyChange);
  }

  private record CohortSnapshot(Double percentile, String warning) { }
}
