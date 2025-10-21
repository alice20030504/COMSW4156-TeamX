package com.teamx.fitness.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "persons_simple")
public class PersonSimple {

  /** Unique identifier for the person. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Name of the person. */
  @NotBlank(message = "Name is required")
  @Column(nullable = false)
  private String name;

  /** Weight in kilograms. */
  @NotNull(message = "Weight is required")
  @Column(nullable = false)
  private Double weight;

  /** Height in centimeters. */
  @NotNull(message = "Height is required")
  @Column(nullable = false)
  private Double height;

  /** Birth date of the person. */
  @NotNull(message = "Birth date is required")
  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  /** Client ID associated with the person. */
  @NotBlank(message = "Client ID is required")
  @Column(name = "client_id", nullable = false)
  private String clientId;

  public PersonSimple() { }

  public PersonSimple(String name, Double weight, Double height, LocalDate birthDate, String clientId) {
    this.name = name;
    this.weight = weight;
    this.height = height;
    this.birthDate = birthDate;
    this.clientId = clientId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }
}
