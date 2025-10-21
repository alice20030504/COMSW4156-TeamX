package com.teamx.fitness.repository;

import com.teamx.fitness.model.PersonSimple;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<PersonSimple, Long> {

  /**
   * Finds all persons for a specific client.
   *
   * @param clientId the client ID
   * @return list of persons belonging to the client
   */
  List<PersonSimple> findByClientId(String clientId);

  /**
   * Finds a specific person by ID and client ID.
   * This ensures data isolation - clients can only access their own data.
   *
   * @param id the person ID
   * @param clientId the client ID
   * @return the person if found and belongs to the client, empty otherwise
   */
  Optional<PersonSimple> findByIdAndClientId(Long id, String clientId);

  /**
   * Counts persons for a specific client.
   *
   * @param clientId the client ID
   * @return count of persons
   */
  long countByClientId(String clientId);
}
