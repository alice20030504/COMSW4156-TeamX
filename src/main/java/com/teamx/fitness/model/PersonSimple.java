package com.teamx.fitness.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Entity
@Table(name = "persons_simple")
public class PersonSimple {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Name is required")
  @Column(nullable = false)
  private String name;

  @NotNull(message = "Weight is required")
  @Column(nullable = false)
  private Double weight;

  @NotNull(message = "Height is required")
  @Column(nullable = false)
  private Double height;

  @NotNull(message = "Birth date is required")
  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @NotBlank(message = "Client ID is required")
  @Column(name = "client_id", nullable = false)
  private String clientId;

  public PersonSimple() {}

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