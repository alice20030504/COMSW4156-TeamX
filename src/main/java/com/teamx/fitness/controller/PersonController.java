package com.teamx.fitness.controller;

import com.teamx.fitness.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for person-related endpoints.
 * Provides fitness calculation APIs.
 */
@RestController
@RequestMapping("/api/persons")
@CrossOrigin(origins = "*")
public class PersonController {

    @Autowired
    private PersonService personService;

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
     * @return daily calorie needs
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
        if (bmi == null) return "Unknown";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal weight";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }
}
