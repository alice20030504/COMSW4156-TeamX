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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.validation.Valid;
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
 * Personal Controller - Endpoints for managing personal accounts and health metrics.
 *
 * <p>This controller provides endpoints for normal users (mobile clients) to manage their own
 * personal fitness data. All endpoints require authentication using user ID and birth date.
 * Users can only access, create, update, or delete their own data.</p>
 */
@RestController
@RequestMapping("/api/persons")
@CrossOrigin(origins = "*")
@Tag(
    name = "Personal Controller",
    description = "Endpoints for managing personal accounts and health metrics. "
        + "Only the authenticated user can view or modify their own data.")
public class PersonController {

  @Autowired private PersonService personService;
  @Autowired private PersonRepository personRepository;
  @Autowired private AuthService authService;

  private static final double BMI_UNDERWEIGHT = 18.5;
  private static final double BMI_NORMAL = 25.0;
  private static final double BMI_OVERWEIGHT = 30.0;

  @PostMapping
  @Operation(
      summary = "Create a new user account",
      description = "Register a new user with personal information including name, weight, height, "
                   + "and birth date. The system automatically handles client ID assignment for "
                   + "data isolation."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User created successfully",
          content = @Content(schema = @Schema(implementation = PersonSimple.class),
              examples = @ExampleObject(value = """
                  {
                    \"id\": 1,
                    \"name\": \"John Doe\",
                    \"weight\": 75.5,
                    \"height\": 180.0,
                    \"birthDate\": \"1990-05-15\",
                    \"clientId\": \"mobile-app1\"
                  }
                  """))),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid client ID")
  })
  public ResponseEntity<PersonSimple> createPerson(
      @Parameter(description = "User information including name, weight, height, and birth date", required = true)
      @Valid @RequestBody PersonSimple person) {
    String clientId = ClientContext.getClientId();
    person.setClientId(clientId);
    PersonSimple savedPerson = personRepository.save(person);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedPerson);
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update existing user information",
      description = "Allow editing of height, weight, and birth date. Requires authentication."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User updated successfully",
          content = @Content(schema = @Schema(implementation = PersonSimple.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<PersonSimple> updatePerson(
      @Parameter(description = "The person ID", required = true)
      @PathVariable Long id,
      @Parameter(description = "Birth date for authentication (YYYY-MM-DD)", required = true)
      @RequestParam LocalDate birthDate,
      @Parameter(description = "Updated user data", required = true)
      @Valid @RequestBody PersonSimple updatedPerson) {

    if (!authService.validateUserAccess(id, birthDate)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String clientId = ClientContext.getClientId();
    Optional<PersonSimple> existing = personRepository.findByIdAndClientId(id, clientId);
    if (existing.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    updatedPerson.setId(id);
    updatedPerson.setClientId(clientId);
    PersonSimple saved = personRepository.save(updatedPerson);
    return ResponseEntity.ok(saved);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get person by ID",
      description = "Retrieve a person record by ID if it belongs to the calling client."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Person retrieved",
          content = @Content(schema = @Schema(implementation = PersonSimple.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<PersonSimple> getPerson(
      @Parameter(description = "The person ID", required = true)
      @PathVariable Long id,
      @Parameter(description = "Birth date for authentication (YYYY-MM-DD)", required = true)
      @RequestParam LocalDate birthDate) {

    if (!authService.validateUserAccess(id, birthDate)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String clientId = ClientContext.getClientId();
    return personRepository.findByIdAndClientId(id, clientId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete person by ID",
      description = "Delete a person record if it belongs to the calling client."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<Void> deletePerson(
      @Parameter(description = "The person ID", required = true)
      @PathVariable Long id,
      @Parameter(description = "Birth date for authentication (YYYY-MM-DD)", required = true)
      @RequestParam LocalDate birthDate) {

    if (!authService.validateUserAccess(id, birthDate)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String clientId = ClientContext.getClientId();
    Optional<PersonSimple> existing = personRepository.findByIdAndClientId(id, clientId);
    if (existing.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    personRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(
      summary = "List persons for the client",
      description = "Return all person records belonging to the calling client."
  )
  public ResponseEntity<List<PersonSimple>> listPersons() {
    String clientId = ClientContext.getClientId();
    List<PersonSimple> persons = personRepository.findByClientId(clientId);
    return ResponseEntity.ok(persons);
  }

  @GetMapping("/health")
  @Operation(
      summary = "Service health check",
      description = "Return service status and metadata."
  )
  public ResponseEntity<Map<String, Object>> health() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "OK");
    response.put("service", "Personal Fitness Management Service");
    response.put("version", "1.0.0");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/calories")
  @Operation(
      summary = "Calculate daily calorie needs based on parameters",
      description = "Calculate daily calorie requirements based on personal metrics including "
                   + "weight, height, age, gender, and weekly training frequency. "
                   + "Uses BMR (Basal Metabolic Rate) calculation with activity multiplier."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Calorie calculation successful",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
                  {
                    \"bmr\": 1650.5,
                    \"dailyCalories\": 2475.75,
                    \"weeklyTrainingFreq\": 4
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

  @GetMapping("/bmi")
  @Operation(
      summary = "Calculate BMI and return BMI category",
      description = "Calculate Body Mass Index (BMI) from weight and height, "
                   + "and return the corresponding health category. "
                   + "BMI categories: Underweight (<18.5), Normal (18.5-24.9), "
                   + "Overweight (25-29.9), Obese (>=30)."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "BMI calculation successful",
          content = @Content(schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(value = """
                  {
                    \"weight\": 75.5,
                    \"height\": 180.0,
                    \"bmi\": 23.3,
                    \"category\": \"Normal weight\"
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

