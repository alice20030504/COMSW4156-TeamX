package com.teamx.fitness.controller;

import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.repository.PersonRepository;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.AuthService;
import com.teamx.fitness.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.HashMap;
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
 * 👤 Personal Controller - Endpoints for managing personal accounts and health metrics.
 * 
 * This controller provides endpoints for normal users (mobile clients) to manage their own
 * personal fitness data. All endpoints require authentication using user ID and birth date.
 * Users can only access, create, update, or delete their own data.
 */
@RestController
@RequestMapping("/api/persons")
@CrossOrigin(origins = "*")
@Tag(name = "Personal Controller", description = "👤 Endpoints for managing personal accounts and health metrics. Only the authenticated user can view or modify their own data.")
public class PersonController {

  /** Service layer for person-related calculations. */
  @Autowired private PersonService personService;

  /** Repository for accessing person records. */
  @Autowired private PersonRepository personRepository;

  /** Authentication service for user validation. */
  @Autowired private AuthService authService;

  /** Constant for lower BMI threshold. */
  private static final double BMI_UNDERWEIGHT = 18.5;

  /** Constant for normal BMI upper threshold. */
  private static final double BMI_NORMAL = 25.0;

  /** Constant for overweight BMI upper threshold. */
  private static final double BMI_OVERWEIGHT = 30.0;

  /**
   * Create a new user account.
   * 
   * Users register with their name, weight, height, and birth date.
   * The system automatically assigns a client ID for data isolation.
   */
  @PostMapping
  @Operation(
      summary = "Create a new user account",
      description = "Register a new user with personal information including name, weight, height, and birth date. " +
                   "The system automatically handles client ID assignment for data isolation."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User created successfully",
          content = @Content(schema = @Schema(implementation = PersonSimple.class),
              examples = @ExampleObject(value = """
                  {
                    "id": 1,
                    "name": "John Doe",
                    "weight": 75.5,
                    "height": 180.0,
                    "birthDate": "1990-05-15",
                    "clientId": "mobile-app1"
                  }
                  """))),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid client ID")
  })
  public ResponseEntity<PersonSimple> createPerson(
      @Parameter(description = "User information including name, weight, height, and birth date", required = true)
      @RequestBody PersonSimple person) {
    String clientId = ClientContext.getClientId();
    person.setClientId(clientId);
    PersonSimple savedPerson = personRepository.save(person);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedPerson);
  }

  /**
   * Update existing user information.
   * 
   * Allow editing of height, weight, and birth date.
   * Requires authentication with user ID and birth date.
   */
  @PutMapping("/{id}")
  @Operation(
      summary = "Update existing user information",
      description = "Update user's height, weight, and birth date. " +
                   "Requires authentication with user ID and birth date. " +
                   "Only the authenticated user can modify their own data."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User updated successfully",
          content = @Content(schema = @Schema(implementation = PersonSimple.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid ID or birth date"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<?> updatePerson(
      @Parameter(description = "User ID", required = true)
      @PathVariable Long id,
      @Parameter(description = "Birth date for authentication (YYYY-MM-DD)", required = true)
      @RequestParam String birthDate,
      @Parameter(description = "Updated user information", required = true)
      @RequestBody PersonSimple updatedPerson) {
    
    // Authenticate user with ID and birth date
    LocalDate birthDateParsed = LocalDate.parse(birthDate);
    if (!authService.validateUserAccess(id, birthDateParsed)) {
      return authService.createUnauthorizedResponse();
    }
    
    String clientId = ClientContext.getClientId();
    return personRepository
        .findByIdAndClientId(id, clientId)
        .map(existingPerson -> {
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
   * Retrieve an individual's record.
   * 
   * The response includes calculated fields such as age and BMI if available.
   * Requires authentication with user ID and birth date.
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "Retrieve an individual's record",
      description = "Get user's personal information including calculated fields like age and BMI. " +
                   "Requires authentication with user ID and birth date. " +
                   "Only the authenticated user can view their own data."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User record retrieved successfully",
          content = @Content(schema = @Schema(implementation = PersonSimple.class),
              examples = @ExampleObject(value = """
                  {
                    "id": 1,
                    "name": "John Doe",
                    "weight": 75.5,
                    "height": 180.0,
                    "birthDate": "1990-05-15",
                    "clientId": "mobile-app1"
                  }
                  """))),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid ID or birth date"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<?> getPersonById(
      @Parameter(description = "User ID", required = true)
      @PathVariable Long id,
      @Parameter(description = "Birth date for authentication (YYYY-MM-DD)", required = true)
      @RequestParam String birthDate) {
    
    // Authenticate user with ID and birth date
    LocalDate birthDateParsed = LocalDate.parse(birthDate);
    if (!authService.validateUserAccess(id, birthDateParsed)) {
      return authService.createUnauthorizedResponse();
    }
    
    String clientId = ClientContext.getClientId();
    return personRepository
        .findByIdAndClientId(id, clientId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Delete their own account.
   * 
   * Requires authentication with user ID and birth date.
   * Only the authenticated user can delete their own account.
   */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete their own account",
      description = "Delete the user's account permanently. " +
                   "Requires authentication with user ID and birth date. " +
                   "Only the authenticated user can delete their own account."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid ID or birth date"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<?> deletePerson(
      @Parameter(description = "User ID", required = true)
      @PathVariable Long id,
      @Parameter(description = "Birth date for authentication (YYYY-MM-DD)", required = true)
      @RequestParam String birthDate) {
    
    // Authenticate user with ID and birth date
    LocalDate birthDateParsed = LocalDate.parse(birthDate);
    if (!authService.validateUserAccess(id, birthDateParsed)) {
      return authService.createUnauthorizedResponse();
    }
    
    String clientId = ClientContext.getClientId();
    return personRepository
        .findByIdAndClientId(id, clientId)
        .map(person -> {
          personRepository.delete(person);
          return ResponseEntity.noContent().<Void>build();
        })
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Perform a health check on their own account.
   * 
   * Return a structured JSON such as { "status": "UP", "service": "Personal Fitness Management Service" }.
   */
  @GetMapping("/health")
  @Operation(
      summary = "Perform a health check on their own account",
      description = "Check the health status of the Personal Fitness Management Service. " +
                   "Returns service status and version information."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Health check successful",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
                  {
                    "status": "UP",
                    "service": "Personal Fitness Management Service",
                    "version": "1.0.0"
                  }
                  """)))
  })
  public ResponseEntity<Map<String, String>> healthCheck() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put("service", "Personal Fitness Management Service");
    response.put("version", "1.0.0");
    return ResponseEntity.ok(response);
  }

  /**
   * Calculate daily calorie needs based on parameters.
   * 
   * Calculate daily calorie requirements based on weight, height, age, gender, and training frequency.
   */
  @GetMapping("/calories")
  @Operation(
      summary = "Calculate daily calorie needs based on parameters",
      description = "Calculate daily calorie requirements based on personal metrics including " +
                   "weight, height, age, gender, and weekly training frequency. " +
                   "Uses BMR (Basal Metabolic Rate) calculation with activity multiplier."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Calorie calculation successful",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
                  {
                    "bmr": 1650.5,
                    "dailyCalories": 2475.75,
                    "weeklyTrainingFreq": 4
                  }
                  """))),
      @ApiResponse(responseCode = "400", description = "Invalid input parameters")
  })
  public ResponseEntity<Map<String, Object>> calculateDailyCalories(
      @Parameter(description = "Weight in kilograms", required = true)
      @RequestParam Double weight,
      @Parameter(description = "Height in centimeters", required = true)
      @RequestParam Double height,
      @Parameter(description = "Age in years", required = true)
      @RequestParam Integer age,
      @Parameter(description = "Gender (male/female)", required = true)
      @RequestParam String gender,
      @Parameter(description = "Weekly training frequency", required = true)
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
   * Calculate BMI and return BMI category.
   * 
   * Calculate BMI (Body Mass Index) and return the corresponding category 
   * ("Underweight", "Normal weight", "Overweight", "Obese").
   */
  @GetMapping("/bmi")
  @Operation(
      summary = "Calculate BMI and return BMI category",
      description = "Calculate Body Mass Index (BMI) from weight and height, " +
                   "and return the corresponding health category. " +
                   "BMI categories: Underweight (<18.5), Normal (18.5-24.9), " +
                   "Overweight (25-29.9), Obese (≥30)."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "BMI calculation successful",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
                  {
                    "weight": 75.5,
                    "height": 180.0,
                    "bmi": 23.3,
                    "category": "Normal weight"
                  }
                  """))),
      @ApiResponse(responseCode = "400", description = "Invalid input parameters")
  })
  public ResponseEntity<Map<String, Object>> calculateBMI(
      @Parameter(description = "Weight in kilograms", required = true)
      @RequestParam Double weight,
      @Parameter(description = "Height in centimeters", required = true)
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
