package com.teamx.fitness.service;

import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.model.PlanStrategy;
import com.teamx.fitness.repository.PersonRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
  /** Target-weight proximity used to guess whether targetChangeKg is absolute weight. */
  private static final double TARGET_WEIGHT_PROXIMITY_KG = 40.0;
  /** Weekly change tolerated without penalty for bulk goals. */
  private static final double BULK_WEEKLY_TOLERANCE = 2.0;
  /** Weekly change tolerated without penalty for cut goals. */
  private static final double CUT_WEEKLY_TOLERANCE = 1.3;
  /** Relative change (as % of weight) tolerated without penalty. */
  private static final double RELATIVE_CHANGE_FREE_THRESHOLD = 0.05;
  /** Penalty factor applied per kg/week beyond tolerance. */
  private static final double WEEKLY_PENALTY_FACTOR = 30.0;
  /** Penalty factor per additional percent of bodyweight shift. */
  private static final double RELATIVE_PENALTY_FACTOR = 120.0;
  /** Bonus when training frequency is excellent for the active goal. */
  private static final double TRAINING_BONUS_HIGH = 8.0;
  /** Bonus when training frequency is solid for the active goal. */
  private static final double TRAINING_BONUS_MEDIUM = 4.0;
  /** Neutral bonus for acceptable training frequency. */
  private static final double TRAINING_BONUS_LIGHT = 2.0;
  /** Penalty when training commitment is very low. */
  private static final double TRAINING_PENALTY_LOW = -6.0;
  /** Penalty when training info is not provided. */
  private static final double TRAINING_PENALTY_MISSING = -4.0;
  /** Bonus when plan strategy perfectly complements the goal. */
  private static final double STRATEGY_BONUS_MATCH = 6.0;
  /** Bonus when plan strategy moderately supports the goal. */
  private static final double STRATEGY_BONUS_SUPPORT = 3.0;
  /** Penalty when plan strategy contradicts the goal. */
  private static final double STRATEGY_PENALTY_MISMATCH = -4.0;
  /** Penalty when no plan strategy is specified. */
  private static final double STRATEGY_PENALTY_MISSING = -5.0;

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
    boolean hasPlanInputs = hasPlanInputs(person);
    Double planAlignmentIndex = hasPlanInputs ? calculatePlanAlignmentIndex(person) : null;
    double overallScore = planAlignmentIndex != null
        ? roundToOne(clamp(healthIndex * HEALTH_WEIGHT + planAlignmentIndex * PLAN_WEIGHT, 0, MAX_OVERALL_SCORE))
        : healthIndex;

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
      boolean peerHasPlan = hasPlanInputs(peer);
      Double peerPlan = peerHasPlan ? calculatePlanAlignmentIndex(peer) : null;
      double peerScore = peerPlan != null
          ? clamp(peerHealth * HEALTH_WEIGHT + peerPlan * PLAN_WEIGHT, 0, MAX_OVERALL_SCORE)
          : peerHealth;
      cohortScores.add(peerScore);
    }
    if (cohortScores.size() < minCohortSize) {
      return new CohortSnapshot(null,
          "Need at least " + minCohortSize + " profiles for percentile comparison.");
    }

    long belowOrEqual = cohortScores.stream()
        .filter(score -> score <= personScore + PERCENTILE_EPSILON)
        .count();
    double percentile = roundToOne(belowOrEqual * 100.0 / cohortScores.size());
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

  private double calculatePlanAlignmentIndex(PersonSimple person) {
    Double delta = resolvePlanDelta(person);
    Double currentWeight = person.getWeight();
    Integer duration = person.getTargetDurationWeeks();
    FitnessGoal goal = person.getGoal();
    if (delta == null || currentWeight == null || duration == null || duration <= 0 || goal == null) {
      return 0.0;
    }

    if (goal == FitnessGoal.CUT && delta >= 0) {
      return 0.0;
    }
    if (goal == FitnessGoal.BULK && delta <= 0) {
      return 0.0;
    }

    double weeklyChange = Math.abs(delta) / duration;
    double relativeChange = Math.abs(delta) / currentWeight;
    double weeklyTolerance = goal == FitnessGoal.BULK ? BULK_WEEKLY_TOLERANCE : CUT_WEEKLY_TOLERANCE;

    double weeklyPenalty = Math.max(0, weeklyChange - weeklyTolerance) * WEEKLY_PENALTY_FACTOR;
    double relativePenalty = Math.max(0, relativeChange - RELATIVE_CHANGE_FREE_THRESHOLD) * RELATIVE_PENALTY_FACTOR;
    double score = 100.0 - weeklyPenalty - relativePenalty;
    score += trainingFrequencyAdjustment(person);
    score += strategyAdjustment(person);
    return roundToOne(clamp(score, 0, MAX_OVERALL_SCORE));
  }

  private String buildRecommendation(
      PersonSimple person,
      double bmi,
      String bmiCategory,
      Double planAlignmentIndex,
      double overallScore) {

    FitnessGoal goal = person.getGoal();
    double weeklyChange = calculateWeeklyChange(person);
    int training = person.getTrainingFrequencyPerWeek() != null
        ? Math.max(person.getTrainingFrequencyPerWeek(), 0)
        : 0;
    String formattedBmi = String.format(Locale.US, "%.1f", bmi);

    // Check for plan alignment = 0 first (unrealistic plan)
    if (planAlignmentIndex != null && planAlignmentIndex == 0.0) {
      Double delta = resolvePlanDelta(person);
      Integer duration = person.getTargetDurationWeeks();
      
      // Check for specific issues that cause plan alignment to be 0
      if (delta == null || duration == null || duration <= 0) {
        return "Your plan alignment is 0 because required plan information is missing or invalid. "
            + "Please ensure all plan fields (target change, duration, training frequency) are properly configured.";
      }
      
      if (goal == FitnessGoal.BULK && delta <= 0) {
        return "Your plan is unrealistic: You've set a BULK goal but your target change would result in weight loss. "
            + "Please revise your target change to be positive (weight gain) to align with your bulking goal, "
            + "or change your goal to CUT if you want to lose weight.";
      }
      
      if (goal == FitnessGoal.CUT && delta >= 0) {
        return "Your plan is unrealistic: You've set a CUT goal but your target change would result in weight gain. "
            + "Please revise your target change to be negative (weight loss) to align with your cutting goal, "
            + "or change your goal to BULK if you want to gain weight.";
      }
      
      // Generic message for other cases (e.g., extremely aggressive rates, very low training frequency, etc.)
      return "Your plan alignment is 0, indicating your goal plan is unrealistic. "
          + "This could be due to: extremely aggressive weight change targets, unrealistic timeline, "
          + "insufficient training frequency, or mismatched plan strategy. "
          + "Please review and adjust your target change, duration, training frequency, "
          + "and plan strategy to create a realistic plan.";
    }

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

    if (overallScore >= SCORE_STRONG_THRESHOLD
        && planAlignmentIndex != null
        && planAlignmentIndex >= PLAN_ALIGNMENT_STRONG_THRESHOLD) {
      return "Solid balance between health metrics and your plan—maintain the current approach.";
    }
    if (overallScore < SCORE_LOW_THRESHOLD) {
      return "Overall score is trending low. Revisit goals and add structured training for a steadier trajectory.";
    }
    return "Stay consistent with the plan and review progress every few weeks.";
  }

  private double calculateWeeklyChange(PersonSimple person) {
    Double delta = resolvePlanDelta(person);
    Integer duration = person.getTargetDurationWeeks();
    if (delta == null || duration == null || duration <= 0) {
      return Double.NaN;
    }
    return Math.abs(delta) / duration;
  }

  private double trainingFrequencyAdjustment(PersonSimple person) {
    Integer rawFrequency = person.getTrainingFrequencyPerWeek();
    FitnessGoal goal = person.getGoal();
    if (goal == null) {
      return 0.0;
    }
    if (rawFrequency == null) {
      return TRAINING_PENALTY_MISSING;
    }
    int frequency = Math.max(rawFrequency, 0);
    if (goal == FitnessGoal.BULK) {
      if (frequency >= 5) {
        return TRAINING_BONUS_HIGH;
      }
      if (frequency >= 3) {
        return TRAINING_BONUS_MEDIUM;
      }
      if (frequency >= 2) {
        return TRAINING_BONUS_LIGHT;
      }
      return TRAINING_PENALTY_LOW;
    }
    if (goal == FitnessGoal.CUT) {
      if (frequency >= 4) {
        return TRAINING_BONUS_HIGH;
      }
      if (frequency >= 2) {
        return TRAINING_BONUS_MEDIUM;
      }
      if (frequency >= 1) {
        return TRAINING_BONUS_LIGHT;
      }
      return TRAINING_PENALTY_LOW;
    }
    return 0.0;
  }

  private double strategyAdjustment(PersonSimple person) {
    PlanStrategy strategy = person.getPlanStrategy();
    FitnessGoal goal = person.getGoal();
    if (goal == null) {
      return 0.0;
    }
    if (strategy == null) {
      return STRATEGY_PENALTY_MISSING;
    }
    if (goal == FitnessGoal.BULK) {
      return switch (strategy) {
        case BOTH -> STRATEGY_BONUS_MATCH;
        case WORKOUT -> STRATEGY_BONUS_SUPPORT;
        case DIET -> STRATEGY_PENALTY_MISMATCH;
      };
    }
    if (goal == FitnessGoal.CUT) {
      return switch (strategy) {
        case BOTH -> STRATEGY_BONUS_MATCH;
        case DIET -> STRATEGY_BONUS_SUPPORT;
        case WORKOUT -> STRATEGY_PENALTY_MISMATCH;
      };
    }
    return 0.0;
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

  private boolean hasPlanInputs(PersonSimple person) {
    return resolvePlanDelta(person) != null
        && person.getGoal() != null
        && person.getTargetDurationWeeks() != null
        && person.getTargetDurationWeeks() > 0;
  }

  private Double resolvePlanDelta(PersonSimple person) {
    Double current = person.getWeight();
    Double rawInput = person.getTargetChangeKg();
    FitnessGoal goal = person.getGoal();
    if (current == null || rawInput == null || goal == null) {
      return null;
    }

    if (Math.abs(rawInput - current) <= TARGET_WEIGHT_PROXIMITY_KG) {
      return rawInput - current;
    }

    double changeMagnitude = rawInput;
    if (goal == FitnessGoal.CUT) {
      return -Math.abs(changeMagnitude);
    }
    if (goal == FitnessGoal.BULK) {
      return Math.abs(changeMagnitude);
    }
    return changeMagnitude;
  }

  private record CohortSnapshot(Double percentile, String warning) { }
}
