package com.teamx.fitness.service;

import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.repository.PersonRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing person-related operations.
 * Provides business logic for fitness calculations.
 */
@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * Fetch all persons belonging to a specific client.
     *
     * @param clientId the active client ID
     * @return list scoped to that client
     */
    @Transactional(readOnly = true)
    public List<PersonSimple> getPersonsForClient(String clientId) {
        return personRepository.findByClientId(clientId);
    }

    /**
     * Lookup a person by id scoped to the client.
     *
     * @param id person identifier
     * @param clientId current client id
     * @return optional scoped result
     */
    @Transactional(readOnly = true)
    public Optional<PersonSimple> findPersonForClient(Long id, String clientId) {
        return personRepository.findByIdAndClientId(id, clientId);
    }

    /**
     * Persist a new person for the client.
     *
     * @param person the request payload
     * @param clientId the active client id
     * @return persisted entity
     */
    @Transactional
    public PersonSimple createPersonForClient(PersonSimple person, String clientId) {
        person.setClientId(clientId);
        return personRepository.save(person);
    }

    /**
     * Update an existing person if it belongs to the client.
     *
     * @param id identifier of person
     * @param clientId current client
     * @param updated incoming payload
     * @return updated person or empty if not found
     */
    @Transactional
    public Optional<PersonSimple> updatePersonForClient(Long id, String clientId, PersonSimple updated) {
        return personRepository
                .findByIdAndClientId(id, clientId)
                .map(existing -> {
                    existing.setName(updated.getName());
                    existing.setWeight(updated.getWeight());
                    existing.setHeight(updated.getHeight());
                    existing.setBirthDate(updated.getBirthDate());
                    return personRepository.save(existing);
                });
    }

    /**
     * Delete a person if it belongs to the client.
     *
     * @param id identifier
     * @param clientId current client
     * @return true if deleted
     */
    @Transactional
    public boolean deletePersonForClient(Long id, String clientId) {
        return personRepository
                .findByIdAndClientId(id, clientId)
                .map(entity -> {
                    personRepository.delete(entity);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Calculate BMI (Body Mass Index).
     * Formula: BMI = weight(kg) / (height(m))^2
     *
     * @param weight weight in kilograms
     * @param height height in centimeters
     * @return calculated BMI
     */
    public Double calculateBMI(Double weight, Double height) {
        if (weight == null || height == null || height == 0) {
            return null;
        }
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
    }

    /**
     * Calculate age from birth date.
     *
     * @param birthDate person's birth date
     * @return age in years
     */
    public Integer calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Calculate Basal Metabolic Rate (BMR) using Harris-Benedict equation.
     * Men: BMR = 88.362 + (13.397 × weight in kg) + (4.799 × height in cm) - (5.677 × age in years)
     * Women: BMR = 447.593 + (9.247 × weight in kg) + (3.098 × height in cm) - (4.330 × age in years)
     *
     * @param weight weight in kilograms
     * @param height height in centimeters
     * @param age age in years
     * @param isMale true for male, false for female
     * @return calculated BMR
     */
    public Double calculateBMR(Double weight, Double height, Integer age, boolean isMale) {
        if (weight == null || height == null || age == null) {
            return null;
        }

        if (isMale) {
            return 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
        } else {
            return 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
        }
    }

    /**
     * Calculate daily calorie needs based on BMR and activity level.
     *
     * @param bmr Basal Metabolic Rate
     * @param weeklyTrainingFreq weekly training frequency
     * @return daily calorie needs
     */
    public Double calculateDailyCalorieNeeds(Double bmr, Integer weeklyTrainingFreq) {
        if (bmr == null || weeklyTrainingFreq == null) {
            return null;
        }

        // Activity factor based on weekly training frequency
        double activityFactor;
        if (weeklyTrainingFreq == 0) {
            activityFactor = 1.2; // Sedentary
        } else if (weeklyTrainingFreq <= 2) {
            activityFactor = 1.375; // Light activity
        } else if (weeklyTrainingFreq <= 4) {
            activityFactor = 1.55; // Moderate activity
        } else if (weeklyTrainingFreq <= 6) {
            activityFactor = 1.725; // Very active
        } else {
            activityFactor = 1.9; // Extra active
        }

        return bmr * activityFactor;
    }
}
