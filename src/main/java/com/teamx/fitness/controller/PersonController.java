package com.teamx.fitness.controller;

import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.repository.PersonRepository;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.PersonService;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for person-related endpoints.
 * Provides fitness calculation APIs.
 */
@RestController
@RequestMapping("/api/persons")
@CrossOrigin(origins = "*")
public class PersonController {

  /** Service layer for person-related calculations. */
  @Autowired private PersonService personService;

  /** Repository for accessing person records. */
  @Autowired private PersonRepository personRepository;

  /** Constant for lower BMI threshold. */
  private static final double BMI_UNDERWEIGHT = 18.5;

  /** Constant for normal BMI upper threshold. */
  private static final double BMI_NORMAL = 25.0;

  /** Constant for overweight BMI upper threshold. */
  private static final double BMI_OVERWEIGHT = 30.0;

  /**
   * Get all persons for the authenticated client.
   * Demonstrates data isolation - each client only sees their own data.
   *
   * @return list of persons belonging to the client
   */
  @GetMapping
  public ResponseEntity<List<PersonSimple>> getAllPersons() {
    String clientId = ClientContext.getClientId();
    List<PersonSimple> persons = personRepository.findByClientId(clientId);
    return ResponseEntity.ok(persons);
  }

  /**
   * Get a specific person by ID.
   * Only returns the person if it belongs to the authenticated client.
   *
   * @param id the person ID
   * @return the person if found and belongs to the client, 404 otherwise
   */
  @GetMapping("/{id}")
  public ResponseEntity<PersonSimple> getPersonById(@PathVariable Long id) {
    String clientId = ClientContext.getClientId();
    return personRepository
        .findByIdAndClientId(id, clientId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Create a new person for the authenticated client.
   *
   * @param person the person data
   * @return the created person
   */
  @PostMapping
  public ResponseEntity<PersonSimple> createPerson(@RequestBody PersonSimple person) {
    String clientId = ClientContext.getClientId();
    person.setClientId(clientId);
    PersonSimple savedPerson = personRepository.save(person);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedPerson);
  }

  /**
   * Update an existing person.
   * Only allows updating if the person belongs to the authenticated client.
   *
   * @param id the person ID
   * @param updatedPerson the updated person data
   * @return the updated person if successful, 404 if not found or doesn't belong to client
   */
  @PutMapping("/{id}")
  public ResponseEntity<PersonSimple> updatePerson(
      @PathVariable Long id, @RequestBody PersonSimple updatedPerson) {
    String clientId = ClientContext.getClientId();

    return personRepository
        .findByIdAndClientId(id, clientId)
        .map(
            existingPerson -> {
              existingPerson.setName(updatedPerson.getName());
              existingPerson.setWeight(updatedPerson.getWeight());
              existingPerson.setHeight(updatedPerson.getHeight());
              existingPerson.setBirthDate(updatedPerson.getBirthDate());
              PersonSimple saved = personRepository.save(existingPerson);
              return ResponseEntity.ok(saved);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Delete a person.
   * Only allows deleting if the person belongs to the authenticated client.
   *
   * @param id the person ID
   * @return 204 if successful, 404 if not found or doesn't belong to client
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
    String clientId = ClientContext.getClientId();

    return personRepository
        .findByIdAndClientId(id, clientId)
        .map(
            person -> {
              personRepository.delete(person);
              return ResponseEntity.noContent().<Void>build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Calculate BMI for given weight and height.
   *
   * @param weight weight in kilograms
   * @param height height in centimeters
   * @return calculated BMI
   */
  @GetMapping("/bmi")
  public ResponseEntity<Map<String, Object>> calculateBMI(
          @RequestParam Double weight,
          @RequestParam Double height) {

      Double bmi = personService.calculateBMI(weight, height);

      Map<String, Object> response = new HashMap<>();
      response.put("weight", weight);
      response.put("height", height);
      response.put("bmi", bmi);
      response.put("category", getBMICategory(bmi));

      return ResponseEntity.ok(response);
  }

  /**
   * Calculate age from birth date.
   *
   * @param birthDate person's birth date
   * @return calculated age
   */
  @GetMapping("/age")
  public ResponseEntity<Map<String, Object>> calculateAge(
          @RequestParam String birthDate) {

      LocalDate date = LocalDate.parse(birthDate);
      Integer age = personService.calculateAge(date);

      Map<String, Object> response = new HashMap<>();
      response.put("birthDate", birthDate);
      response.put("age", age);

      return ResponseEntity.ok(response);
  }

  /**
   * Calculate daily calorie needs.
   *
   * @param weight weight in kilograms
   * @param height height in centimeters
   * @param age age in years
   * @param gender gender (male/female)
   * @param weeklyTrainingFreq weekly training frequency
   * @return daily calorie needs (kcal/day)
   */
  @GetMapping("/calories")
  public ResponseEntity<Map<String, Object>> calculateDailyCalories(
          @RequestParam Double weight,
          @RequestParam Double height,
          @RequestParam Integer age,
          @RequestParam String gender,
          @RequestParam Integer weeklyTrainingFreq) {

      boolean isMale = "male".equalsIgnoreCase(gender);
      Double bmr = personService.calculateBMR(weight, height, age, isMale);
      Double dailyCalories = personService.calculateDailyCalorieNeeds(bmr, weeklyTrainingFreq);

      Map<String, Object> response = new HashMap<>();
      response.put("bmr", bmr);
      response.put("dailyCalories", dailyCalories);
      response.put("weeklyTrainingFreq", weeklyTrainingFreq);

      return ResponseEntity.ok(response);
  }

  /**
  * Health check endpoint.
  *
  * @return JSON response indicating service status
  */
  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> healthCheck() {
      Map<String, String> response = new HashMap<>();
      response.put("status", "UP");
      response.put("service", "Personal Fitness Management Service");
      response.put("version", "1.0.0");
      return ResponseEntity.ok(response);
  }

  /**
   * Get BMI category based on BMI value.
   */
  private String getBMICategory(Double bmi) {
    if (bmi == null) {
        return "Unknown";
    }
    if (bmi < BMI_UNDERWEIGHT) {
        return "Underweight";
    } else if (bmi < BMI_NORMAL) {
        return "Normal weight";
    } else if (bmi < BMI_OVERWEIGHT) {
        return "Overweight";
    } else {
        return "Obese";
    }
  }
}
