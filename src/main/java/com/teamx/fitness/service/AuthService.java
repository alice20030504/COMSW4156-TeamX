package com.teamx.fitness.service;

import com.teamx.fitness.repository.PersonRepository;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Authentication service for validating user access to personal data.
 * Provides lightweight authentication using user ID and birth date.
 */
@Service
public class AuthService {

  /** Repository for accessing person records for authentication. */
  @Autowired
  private PersonRepository personRepository;

  /**
   * Validates that a user can access their own data using ID and birth date.
   * This ensures only the authenticated user can view or modify their own records.
   *
   * @param id the person ID
   * @param birthDate the person's birth date for verification
   * @return true if authentication is successful, false otherwise
   */
  public boolean validateUserAccess(Long id, LocalDate birthDate) {
    if (id == null || birthDate == null) {
      return false;
    }
    
    return personRepository.existsByIdAndBirthDate(id, birthDate);
  }

  /**
   * Creates an unauthorized response for invalid authentication.
   *
   * @return ResponseEntity with 401 Unauthorized status
   */
  public ResponseEntity<String> createUnauthorizedResponse() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body("Invalid ID or birth date. Only the authenticated user can access their own data.");
  }
}

