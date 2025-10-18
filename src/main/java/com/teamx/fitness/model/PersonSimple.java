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

  public PersonSimple() {}

  public PersonSimple(String name, Double weight, Double height) {
    this.name = name;
    this.weight = weight;
    this.height = height;
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
}