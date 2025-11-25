package com.teamx.fitness.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.teamx.fitness.controller.PersonController;
import com.teamx.fitness.controller.dto.PersonCreateRequest;
import com.teamx.fitness.controller.dto.PersonCreatedResponse;
import com.teamx.fitness.controller.dto.PersonProfileResponse;
import com.teamx.fitness.model.FitnessGoal;
import com.teamx.fitness.model.Gender;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.model.PlanStrategy;
import com.teamx.fitness.repository.PersonRepository;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.PersonService;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Verifies that {@link PersonController} enforces client isolation rules using mocked
 * repositories and the thread-local {@link ClientContext}. Each endpoint is exercised with
 * representative valid, boundary, and invalid scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Client isolation safeguards")
class ClientIsolationIntegrationTest {

  private static final String MOBILE_CLIENT_1 = "mobile-app1";
  private static final String MOBILE_CLIENT_2 = "mobile-app2";

  @Mock private PersonRepository personRepository;

  @Mock private PersonService personService;

  @InjectMocks private PersonController personController;

  @AfterEach
  void clearContext() {
    ClientContext.clear();
  }

  /**
   * Valid scenario: the owning client retrieves their record successfully.
   */
  @Test
  @DisplayName("Current client can fetch their persisted profile")
  void getProfile_AllowsCurrentClient() {
    PersonSimple stored = buildPerson("Alice", Gender.FEMALE, FitnessGoal.CUT, MOBILE_CLIENT_1);
    stored.setWeight(65.0);
    stored.setHeight(170.0);
    stored.setBirthDate(LocalDate.of(1990, 5, 15));

    ClientContext.setClientId(MOBILE_CLIENT_1);
    when(personRepository.findByClientId(MOBILE_CLIENT_1)).thenReturn(Optional.of(stored));

    ResponseEntity<PersonProfileResponse> response =
        personController.getProfile();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MOBILE_CLIENT_1, response.getBody().getClientId());
    assertEquals(Gender.FEMALE, response.getBody().getGender());
  }

  /**
   * Invalid scenario: a different client receives a 401 when attempting to access another client's
   * record due to authentication failure.
   */
  @Test
  @DisplayName("Profile lookup returns 404 for unknown client")
  void getProfile_ReturnsNotFoundForUnknownClient() {
    ClientContext.setClientId(MOBILE_CLIENT_2);
    when(personRepository.findByClientId(MOBILE_CLIENT_2)).thenReturn(Optional.empty());

    org.junit.jupiter.api.Assertions.assertThrows(
        ResponseStatusException.class, () -> personController.getProfile());
  }

  /**
   * Valid scenario: created records are automatically tagged with the active client.
   */
  @Test
  @DisplayName("Create person assigns current client ID")
  void createPerson_AssignsClientId() {
    PersonCreateRequest request = new PersonCreateRequest();
    request.setName("Bob");
    request.setWeight(80.0);
    request.setHeight(180.0);
    request.setBirthDate(LocalDate.of(1995, 3, 20));
    request.setGoal(FitnessGoal.BULK);
    request.setGender(Gender.MALE);

    when(personService.calculateBMI(80.0, 180.0)).thenReturn(24.69);
    lenient().when(personRepository.findByClientId(any(String.class))).thenReturn(Optional.empty());
    when(personRepository.save(any(PersonSimple.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, PersonSimple.class));

    ResponseEntity<PersonCreatedResponse> response = personController.createPerson(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    String clientId = response.getBody().getClientId();
    assertTrue(clientId.startsWith(ClientContext.MOBILE_PREFIX));
    verify(personRepository).save(any(PersonSimple.class));
  }

  /**
   * Valid update scenario demonstrating owner access to modify their record.
   */
  @Test
  @DisplayName("Update respects client ownership")
  void updatePerson_RespectsOwnership() {
    ClientContext.setClientId(MOBILE_CLIENT_1);
    PersonSimple existing = buildPerson("Carol", Gender.FEMALE, FitnessGoal.CUT, MOBILE_CLIENT_1);
    existing.setWeight(70.0);
    existing.setHeight(172.0);
    existing.setBirthDate(LocalDate.of(1992, 7, 10));
    existing.setId(9L);

    PersonSimple update = new PersonSimple();
    update.setName("Carol Updated");
    update.setWeight(72.0);
    update.setHeight(172.0);
    update.setBirthDate(existing.getBirthDate());
    update.setGoal(FitnessGoal.CUT);
    update.setGender(Gender.FEMALE);
    update.setPlanStrategy(existing.getPlanStrategy());

    when(personRepository.findByClientId(MOBILE_CLIENT_1)).thenReturn(Optional.of(existing));
    when(personService.calculateBMI(72.0, 172.0)).thenReturn(24.34);
    doAnswer(invocation -> {
        PersonSimple savedPerson = invocation.getArgument(0);
        savedPerson.setName("Carol Updated");
        savedPerson.setClientId(MOBILE_CLIENT_1);
        return savedPerson;
    }).when(personRepository).save(any(PersonSimple.class));

    ResponseEntity<PersonSimple> response = personController.updatePerson(update);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Carol Updated", ((PersonSimple) response.getBody()).getName());
  }

  /**
   * Invalid update scenario: non-owners receive a 401 due to authentication failure.
   */
  @Test
  @DisplayName("Update returns 404 when profile missing")
  void updatePerson_RejectsOtherClient() {
    ClientContext.setClientId(MOBILE_CLIENT_2);

    PersonSimple updateAttempt = new PersonSimple();
    updateAttempt.setName("Carol Updated");
    updateAttempt.setWeight(72.0);
    updateAttempt.setHeight(172.0);
    updateAttempt.setBirthDate(LocalDate.of(1992, 7, 10));
    updateAttempt.setGoal(FitnessGoal.CUT);
    updateAttempt.setGender(Gender.FEMALE);

    when(personRepository.findByClientId(MOBILE_CLIENT_2)).thenReturn(Optional.empty());

    org.junit.jupiter.api.Assertions.assertThrows(
        ResponseStatusException.class,
        () -> personController.updatePerson(updateAttempt));
    verify(personRepository, never()).save(any(PersonSimple.class));
  }

  /**
   * Valid deletion scenario confirming owner access and context cleanup.
   */
  @Test
  @DisplayName("Delete removes record for owner and clears context")
  void deletePerson_AllowsOwnerAndClearsContext() {
    ClientContext.setClientId(MOBILE_CLIENT_1);
    PersonSimple existing = buildPerson("Dana", Gender.FEMALE, FitnessGoal.BULK, MOBILE_CLIENT_1);
    existing.setWeight(68.0);
    existing.setHeight(168.0);
    existing.setBirthDate(LocalDate.of(1991, 1, 1));
    existing.setId(4L);

    when(personRepository.findByClientId(MOBILE_CLIENT_1)).thenReturn(Optional.of(existing));

    ResponseEntity<?> response = personController.deletePerson();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(personRepository).findByClientId(MOBILE_CLIENT_1);
    verify(personRepository).delete(existing);

    ClientContext.clear();
    assertFalse(ClientContext.isMobileClient(ClientContext.getClientId()));
  }

  /**
   * Invalid deletion scenario: other clients cannot remove records they do not own.
   */
  @Test
  @DisplayName("Delete returns 404 when profile missing")
  void deletePerson_RejectsOtherClient() {
    ClientContext.setClientId(MOBILE_CLIENT_2);
    when(personRepository.findByClientId(MOBILE_CLIENT_2)).thenReturn(Optional.empty());

    ResponseEntity<?> response = personController.deletePerson();
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(personRepository, never()).delete(any(PersonSimple.class));
  }

  private PersonSimple buildPerson(String name, Gender gender, FitnessGoal goal, String clientId) {
    PersonSimple person = new PersonSimple();
    person.setName(name);
    person.setGender(gender);
    person.setGoal(goal);
    person.setClientId(clientId);
    person.setWeight(70.0);
    person.setHeight(170.0);
    person.setBirthDate(LocalDate.of(1990, 1, 1));
    person.setPlanStrategy(PlanStrategy.BOTH);
    return person;
  }
}
