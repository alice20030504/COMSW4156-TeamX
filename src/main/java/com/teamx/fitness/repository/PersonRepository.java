/**
 * <p>Part of the Fitness Management Service.</p>
 *
 * <p>JPA repository interface for accessing {@code PersonSimple} entities.
 * Provides query methods to find persons by client ID and perform isolation checks.</p>
 *
 * @checkstyle 2025-10-21 by alice
 * @version 1.0
 */

package com.teamx.fitness.repository;

import com.teamx.fitness.model.PersonSimple;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<PersonSimple, Long> {

  /**
   * Finds a person by unique client identifier.
   *
   * @param clientId the client ID
   * @return optional person
   */
  Optional<PersonSimple> findByClientId(String clientId);

  /**
   * Counts persons for a specific client.
   *
   * @param clientId the client ID
   * @return count of persons
   */
  long countByClientId(String clientId);

}
