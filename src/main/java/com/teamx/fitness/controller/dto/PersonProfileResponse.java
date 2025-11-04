package com.teamx.fitness.controller.dto;

import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.Gender;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.model.PlanStrategy;
import java.time.LocalDate;

/**
 * Person profile payload tailored for API responses.
 */
public class PersonProfileResponse {

  private String clientId;
  private String name;
  private Double weight;
  private Double height;
  private LocalDate birthDate;
  private FitnessGoal goal;
  /** Gender stored for the person. */
  private Gender gender;
  private Double targetChangeKg;
  private Integer targetDurationWeeks;
  private Integer trainingFrequencyPerWeek;
  private PlanStrategy planStrategy;

  public static PersonProfileResponse fromEntity(PersonSimple person) {
    PersonProfileResponse response = new PersonProfileResponse();
    response.setClientId(person.getClientId());
    response.setName(person.getName());
    response.setWeight(person.getWeight());
    response.setHeight(person.getHeight());
    response.setBirthDate(person.getBirthDate());
    response.setGoal(person.getGoal());
    response.setGender(person.getGender());
    response.setTargetChangeKg(person.getTargetChangeKg());
    response.setTargetDurationWeeks(person.getTargetDurationWeeks());
    response.setTrainingFrequencyPerWeek(person.getTrainingFrequencyPerWeek());
    response.setPlanStrategy(person.getPlanStrategy());
    return response;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getWeight() {
    return weight;
  }

  public void setWeight(Double weight) {
    this.weight = weight;
  }

  public Double getHeight() {
    return height;
  }

  public void setHeight(Double height) {
    this.height = height;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public FitnessGoal getGoal() {
    return goal;
  }

  public void setGoal(FitnessGoal goal) {
    this.goal = goal;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public Double getTargetChangeKg() {
    return targetChangeKg;
  }

  public void setTargetChangeKg(Double targetChangeKg) {
    this.targetChangeKg = targetChangeKg;
  }

  public Integer getTargetDurationWeeks() {
    return targetDurationWeeks;
  }

  public void setTargetDurationWeeks(Integer targetDurationWeeks) {
    this.targetDurationWeeks = targetDurationWeeks;
  }

  public Integer getTrainingFrequencyPerWeek() {
    return trainingFrequencyPerWeek;
  }

  public void setTrainingFrequencyPerWeek(Integer trainingFrequencyPerWeek) {
    this.trainingFrequencyPerWeek = trainingFrequencyPerWeek;
  }

  public PlanStrategy getPlanStrategy() {
    return planStrategy;
  }

  public void setPlanStrategy(PlanStrategy planStrategy) {
    this.planStrategy = planStrategy;
  }
}
