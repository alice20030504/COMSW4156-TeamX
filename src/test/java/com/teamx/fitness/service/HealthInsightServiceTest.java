package com.teamx.fitness.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.Gender;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.model.PlanStrategy;
import com.teamx.fitness.repository.PersonRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link HealthInsightService} covering boundary, invalid, and valid flows.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HealthInsightService")
class HealthInsightServiceTest {

  /** Mocked persistence layer feeding cohort data. */
  @Mock
  private PersonRepository personRepository;

  /** Service under test. */
  private HealthInsightService healthInsightService;

  @BeforeEach
  void setUpService() {
    healthInsightService = new HealthInsightService(new PersonService(), personRepository, 2);
  }

  @Test
  @DisplayName("buildInsights returns percentile when cohort threshold reached")
  void buildInsightsCalculatesPercentileForValidCohort() {
    PersonSimple main = templatePerson("clientA");
    updateMetrics(main, 90.0, 175.0);
    applyPlan(main, FitnessGoal.CUT, PlanStrategy.BOTH, 4.0, 8, 4);
    PersonSimple peerB = templatePerson("clientB");
    updateMetrics(peerB, 78.0, 170.0);
    applyPlan(peerB, FitnessGoal.CUT, PlanStrategy.DIET, 3.0, 6, 3);
    PersonSimple peerC = templatePerson("clientC");
    updateMetrics(peerC, 82.0, 180.0);
    applyPlan(peerC, FitnessGoal.BULK, PlanStrategy.WORKOUT, 2.0, 10, 5);
    when(personRepository.findAll()).thenReturn(List.of(main, peerB, peerC));

    HealthInsightResult result = healthInsightService.buildInsights(main);

    assertNotNull(result.percentile(), "Percentile should be available when cohort >= threshold");
    assertNull(result.cohortWarning(), "Cohort warning should be absent when data is sufficient");
    assertTrue(result.recommendation().contains("Cutting"), "Message should reference the CUT goal context");
  }

  @Test
  @DisplayName("buildInsights supplies warning when cohort is too small")
  void buildInsightsWarnsWhenCohortTooSmall() {
    healthInsightService = new HealthInsightService(new PersonService(), personRepository, 5);
    PersonSimple main = templatePerson("client-small");
    updateMetrics(main, 80.0, 172.0);
    applyPlan(main, FitnessGoal.CUT, PlanStrategy.DIET, 3.0, 8, 3);
    PersonSimple peer = templatePerson("client-peer");
    updateMetrics(peer, 79.0, 170.0);
    applyPlan(peer, FitnessGoal.BULK, PlanStrategy.WORKOUT, 2.0, 12, 4);
    when(personRepository.findAll()).thenReturn(List.of(main, peer));

    HealthInsightResult result = healthInsightService.buildInsights(main);

    assertNull(result.percentile(), "Percentile should be null when there are not enough peers");
    assertNotNull(result.cohortWarning(), "Cohort warning should explain the missing percentile");
  }

  @Test
  @DisplayName("buildInsights nudges obese bulking users toward cutting first")
  void buildInsightsEncouragesCutBeforeBulking() {
    healthInsightService = new HealthInsightService(new PersonService(), personRepository, 1);
    PersonSimple main = templatePerson("client-obese");
    updateMetrics(main, 150.0, 175.0);
    applyPlan(main, FitnessGoal.BULK, PlanStrategy.WORKOUT, 5.0, 12, 4);
    PersonSimple support = templatePerson("client-support");
    updateMetrics(support, 85.0, 178.0);
    applyPlan(support, FitnessGoal.CUT, PlanStrategy.BOTH, 4.0, 8, 4);
    mockCohort(main, support);

    HealthInsightResult result = healthInsightService.buildInsights(main);

    assertTrue(
        result.recommendation().contains("obese"),
        "Message should acknowledge obese BMI");
    assertTrue(
        result.recommendation().contains("CUT"),
        "Users bulking while obese should be nudged toward cutting first");
  }

  @Test
  @DisplayName("buildInsights praises balanced high-scoring trajectories")
  void buildInsightsRecognisesBalancedTrajectory() {
    healthInsightService = new HealthInsightService(new PersonService(), personRepository, 1);
    PersonSimple main = templatePerson("client-strong");
    updateMetrics(main, 74.0, 178.0);
    applyPlan(main, null, PlanStrategy.BOTH, 4.0, 8, 5);
    PersonSimple peer = templatePerson("client-peer");
    updateMetrics(peer, 73.0, 176.0);
    applyPlan(peer, null, PlanStrategy.BOTH, 3.0, 8, 5);
    mockCohort(main, peer);

    HealthInsightResult result = healthInsightService.buildInsights(main);

    assertTrue(
        result.overallScore() >= 80.0,
        "Overall score should exceed strong threshold");
    assertTrue(
        result.planAlignmentIndex() >= 70.0,
        "Plan alignment should exceed strong threshold");
    assertTrue(
        result.recommendation().contains("Solid balance"),
        "High-scoring plans should be praised");
  }

  @Test
  @DisplayName("buildInsights warns when overall score is low")
  void buildInsightsWarnsWhenTrajectoryIsLow() {
    healthInsightService = new HealthInsightService(new PersonService(), personRepository, 1);
    PersonSimple main = templatePerson("client-low");
    updateMetrics(main, 160.0, 165.0);
    applyPlan(main, null, null, null, null, null);
    PersonSimple peer = templatePerson("client-peer-low");
    updateMetrics(peer, 150.0, 168.0);
    applyPlan(peer, null, PlanStrategy.WORKOUT, 1.0, 52, 1);
    mockCohort(main, peer);

    HealthInsightResult result = healthInsightService.buildInsights(main);

    assertTrue(
        result.overallScore() < 50.0,
        "Overall score should fall below the warning threshold");
    assertTrue(
        result.recommendation().contains("Overall score is trending low"),
        "Low scores should produce warning message");
  }

  @Test
  @DisplayName("buildInsights flags overly aggressive cutting targets")
  void buildInsightsFlagsAggressiveCutting() {
    healthInsightService = new HealthInsightService(new PersonService(), personRepository, 1);
    PersonSimple main = templatePerson("client-cut-fast");
    updateMetrics(main, 95.0, 175.0);
    applyPlan(main, FitnessGoal.CUT, PlanStrategy.BOTH, 15.0, 10, 3);
    PersonSimple peer = templatePerson("client-cut-peer");
    updateMetrics(peer, 90.0, 175.0);
    applyPlan(peer, FitnessGoal.CUT, PlanStrategy.BOTH, 4.0, 10, 3);
    mockCohort(main, peer);

    HealthInsightResult result = healthInsightService.buildInsights(main);

    assertTrue(result.recommendation().contains("very aggressive"), "Aggressive cutting should be flagged");
  }

  @Test
  @DisplayName("buildInsights encourages lean bulks when rates are modest")
  void buildInsightsEncouragesLeanBulk() {
    healthInsightService = new HealthInsightService(new PersonService(), personRepository, 1);
    PersonSimple main = templatePerson("client-bulk-lean");
    updateMetrics(main, 72.0, 178.0);
    applyPlan(main, FitnessGoal.BULK, PlanStrategy.WORKOUT, 3.0, 18, 4);
    PersonSimple peer = templatePerson("client-bulk-peer");
    updateMetrics(peer, 74.0, 178.0);
    applyPlan(peer, FitnessGoal.BULK, PlanStrategy.WORKOUT, 4.0, 20, 4);
    mockCohort(main, peer);

    HealthInsightResult result = healthInsightService.buildInsights(main);

    assertTrue(
        result.recommendation().contains("Lean bulk focus looks good"),
        "Moderate bulks should receive encouragement");
  }

  @Test
  @DisplayName("buildInsights warns underweight cutters to rebuild first")
  void buildInsightsHandlesUnderweightCut() {
    PersonSimple main = templatePerson("client-under");
    updateMetrics(main, 52.0, 185.0);
    applyPlan(main, FitnessGoal.CUT, PlanStrategy.DIET, 2.0, 12, 3);
    PersonSimple peer = templatePerson("peer-under");
    updateMetrics(peer, 60.0, 180.0);
    applyPlan(peer, FitnessGoal.BULK, PlanStrategy.WORKOUT, 3.0, 12, 3);
    mockCohort(main, peer);

    HealthInsightResult result = healthInsightService.buildInsights(main);

    assertTrue(
        result.recommendation().contains("underweight"),
        "Underweight cutters should be warned");
    assertTrue(
        result.recommendation().contains("lean bulk"),
        "Users should be nudged to rebuild before cutting further");
  }

  @Test
  @DisplayName("buildInsights flags aggressive bulk rates")
  void buildInsightsFlagsAggressiveBulk() {
    PersonSimple main = templatePerson("client-bulk-fast");
    updateMetrics(main, 78.0, 180.0);
    applyPlan(main, FitnessGoal.BULK, PlanStrategy.BOTH, 12.0, 8, 4);
    PersonSimple peer = templatePerson("peer-bulk-fast");
    updateMetrics(peer, 80.0, 179.0);
    applyPlan(peer, FitnessGoal.BULK, PlanStrategy.BOTH, 6.0, 12, 4);
    mockCohort(main, peer);

    HealthInsightResult result = healthInsightService.buildInsights(main);

    assertTrue(
        result.recommendation().contains("may add unnecessary fat"),
        "Aggressive bulk rates should be flagged");
  }

  @Test
  @DisplayName("buildInsights provides neutral recommendation when no specific branch applies")
  void buildInsightsProvidesNeutralRecommendation() {
    PersonSimple main = templatePerson("client-neutral");
    updateMetrics(main, 82.0, 178.0);
    applyPlan(main, null, PlanStrategy.BOTH, 2.0, 16, 3);
    healthInsightService = new HealthInsightService(new PersonService(), personRepository, 1);
    PersonSimple peer = templatePerson("peer-neutral");
    updateMetrics(peer, 83.0, 180.0);
    applyPlan(peer, FitnessGoal.CUT, PlanStrategy.BOTH, 2.0, 16, 3);
    mockCohort(main, peer);

    HealthInsightResult result = healthInsightService.buildInsights(main);

    assertTrue(
        result.recommendation().contains("Stay consistent"),
        "Neutral trajectories should receive steady advice");
  }

  @Test
  @DisplayName("buildInsights ignores peers with invalid BMI metrics")
  void buildInsightsSkipsInvalidPeers() {
    PersonSimple main = templatePerson("client-valid");
    updateMetrics(main, 82.0, 178.0);
    applyPlan(main, FitnessGoal.CUT, PlanStrategy.DIET, 3.0, 10, 3);
    PersonSimple invalidPeer = templatePerson("peer-invalid");
    updateMetrics(invalidPeer, 82.0, 178.0);
    applyPlan(invalidPeer, FitnessGoal.CUT, PlanStrategy.DIET, 3.0, 10, 3);
    invalidPeer.setHeight(0.0); // triggers BMI failure
    PersonSimple peer = templatePerson("peer-valid");
    updateMetrics(peer, 81.0, 176.0);
    applyPlan(peer, FitnessGoal.CUT, PlanStrategy.WORKOUT, 2.0, 12, 3);
    mockCohort(main, invalidPeer, peer);

    HealthInsightResult result = healthInsightService.buildInsights(main);

    assertNotNull(result.percentile(), "Valid peers should still yield percentiles despite erroneous entries");
  }

  private PersonSimple templatePerson(String clientId) {
    PersonSimple person = new PersonSimple();
    person.setClientId(clientId);
    person.setName("User-" + clientId);
    person.setWeight(80.0);
    person.setHeight(175.0);
    person.setGoal(FitnessGoal.CUT);
    person.setPlanStrategy(PlanStrategy.BOTH);
    person.setTargetChangeKg(2.0);
    person.setTargetDurationWeeks(12);
    person.setTrainingFrequencyPerWeek(3);
    person.setBirthDate(LocalDate.of(1990, 1, 1));
    person.setGender(Gender.MALE);
    return person;
  }

  private void updateMetrics(PersonSimple person, Double weight, Double height) {
    if (weight != null) {
      person.setWeight(weight);
    }
    if (height != null) {
      person.setHeight(height);
    }
  }

  private void applyPlan(
      PersonSimple person,
      FitnessGoal goal,
      PlanStrategy strategy,
      Double targetChange,
      Integer durationWeeks,
      Integer trainingPerWeek) {
    person.setGoal(goal);
    person.setPlanStrategy(strategy);
    person.setTargetChangeKg(targetChange);
    person.setTargetDurationWeeks(durationWeeks);
    person.setTrainingFrequencyPerWeek(trainingPerWeek);
  }

  private void mockCohort(PersonSimple... people) {
    when(personRepository.findAll()).thenReturn(Arrays.asList(people));
  }
}
