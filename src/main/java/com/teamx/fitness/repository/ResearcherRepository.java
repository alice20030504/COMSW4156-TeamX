/**
 * <p>Part of the Fitness Management Service.</p>
 *
 * <p>JPA repository interface for accessing {@code Researcher} entities.
 * Provides query methods to find researchers by client ID.</p>
 *
 * @checkstyle 2025-11-20
 * @version 1.0
 */

package com.teamx.fitness.repository;

import com.teamx.fitness.model.Researcher;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResearcherRepository extends JpaRepository<Researcher, Long> {

  /**
   * Finds a researcher by unique client identifier.
   *
   * @param clientId the client ID
   * @return optional researcher
   */
  Optional<Researcher> findByClientId(String clientId);

  /**
   * Checks if a researcher exists with the given email.
   *
   * @param email the email address
   * @return true if researcher exists with matching email
   */
  boolean existsByEmail(String email);
}

