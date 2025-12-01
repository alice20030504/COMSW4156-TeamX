package com.teamx.fitness.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import java.time.Month;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

/**
 * Verifies that {@link PersonController} enforces client isolation rules using mocked
 * repositories and the thread-local {@link ClientContext}. Each endpoint is exercised with
 * representative valid, boundary, and invalid scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Client isolation safeguards")
class ClientIsolationIntegrationTest {

  /** Primary mock client identifier. */
  private static final String MOBILE_CLIENT_1 = "mobile-app1";
  /** Secondary mock client identifier. */
  private static final String MOBILE_CLIENT_2 = "mobile-app2";
  /** Baseline persisted weight used for default profiles (kg). */
  private static final double WEIGHT_BASE_KG = 70.0;
  /** Baseline persisted height used for default profiles (cm). */
  private static final double HEIGHT_BASE_CM = 170.0;
  /** Weight for the Alice sample profile (kg). */
  private static final double WEIGHT_ALICE_KG = 65.0;
  /** Weight for the Bob sample profile (kg). */
  private static final double WEIGHT_BOB_KG = 80.0;
  /** Updated weight for Carol after edits (kg). */
  private static final double WEIGHT_CAROL_UPDATED_KG = 72.0;
  /** Weight for the Dana sample profile (kg). */
  private static final double WEIGHT_DANA_KG = 68.0;
  /** Height for the Carol sample profile (cm). */
  private static final double HEIGHT_CAROL_CM = 172.0;
  /** Height for the Bob sample profile (cm). */
  private static final double HEIGHT_BOB_CM = 180.0;
  /** Height for the Dana sample profile (cm). */
  private static final double HEIGHT_DANA_CM = 168.0;
  /** BMI expected for Bob's metrics. */
  private static final double BMI_BOB = 24.69;
  /** BMI expected for Carol's updated metrics. */
  private static final double BMI_CAROL_UPDATED = 24.34;
  /** Persisted ID assigned to Carol's record. */
  private static final long PERSON_ID_CAROL = 9L;
  /** Persisted ID assigned to Dana's record. */
  private static final long PERSON_ID_DANA = 4L;
  /** Year constant for 1990. */
  private static final int YEAR_1990 = 1990;
  /** Year constant for 1991. */
  private static final int YEAR_1991 = 1991;
  /** Year constant for 1992. */
  private static final int YEAR_1992 = 1992;
  /** Year constant for 1995. */
  private static final int YEAR_1995 = 1995;
  /** First day of month constant. */
  private static final int DAY_ONE = 1;
  /** Tenth day of month constant. */
  private static final int DAY_TEN = 10;
  /** Fifteenth day of month constant. */
  private static final int DAY_FIFTEEN = 15;
  /** Twentieth day of month constant. */
  private static final int DAY_TWENTY = 20;
  /** Default DOB used for persisted profiles. */
  private static final LocalDate DOB_DEFAULT =
      LocalDate.of(YEAR_1990, Month.JANUARY, DAY_ONE);
  /** Alice sample DOB. */
  private static final LocalDate DOB_ALICE =
      LocalDate.of(YEAR_1990, Month.MAY, DAY_FIFTEEN);
  /** Bob sample DOB. */
  private static final LocalDate DOB_BOB =
      LocalDate.of(YEAR_1995, Month.MARCH, DAY_TWENTY);
  /** Carol sample DOB. */
  private static final LocalDate DOB_CAROL =
      LocalDate.of(YEAR_1992, Month.JULY, DAY_TEN);
  /** Dana sample DOB. */
  private static final LocalDate DOB_DANA =
      LocalDate.of(YEAR_1991, Month.JANUARY, DAY_ONE);

  /** Repository mock for persisted profiles. */
  @Mock private PersonRepository personRepository;

  /** Service mock for BMI/calorie helpers. */
  @Mock private PersonService personService;

  /** Controller instance under test. */
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
  void getProfileAllowsCurrentClient() {
    PersonSimple stored = buildPerson("Alice", Gender.FEMALE, FitnessGoal.CUT, MOBILE_CLIENT_1);
    stored.setWeight(WEIGHT_ALICE_KG);
    stored.setHeight(HEIGHT_BASE_CM);
    stored.setBirthDate(DOB_ALICE);

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
  void getProfileReturnsNotFoundForUnknownClient() {
    ClientContext.setClientId(MOBILE_CLIENT_2);
    when(personRepository.findByClientId(MOBILE_CLIENT_2)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> personController.getProfile());
  }

  /**
   * Valid scenario: created records are automatically tagged with the active client.
   */
  @Test
  @DisplayName("Create person assigns current client ID")
  void createPersonAssignsClientId() {
    PersonCreateRequest request = new PersonCreateRequest();
    request.setName("Bob");
    request.setWeight(WEIGHT_BOB_KG);
    request.setHeight(HEIGHT_BOB_CM);
    request.setBirthDate(DOB_BOB);
    request.setGoal(FitnessGoal.BULK);
    request.setGender(Gender.MALE);

    when(personService.calculateBMI(WEIGHT_BOB_KG, HEIGHT_BOB_CM)).thenReturn(BMI_BOB);
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
  void updatePersonRespectsOwnership() {
    ClientContext.setClientId(MOBILE_CLIENT_1);
    PersonSimple existing = buildPerson("Carol", Gender.FEMALE, FitnessGoal.CUT, MOBILE_CLIENT_1);
    existing.setWeight(WEIGHT_BASE_KG);
    existing.setHeight(HEIGHT_CAROL_CM);
    existing.setBirthDate(DOB_CAROL);
    existing.setId(PERSON_ID_CAROL);

    PersonSimple update = new PersonSimple();
    update.setName("Carol Updated");
    update.setWeight(WEIGHT_CAROL_UPDATED_KG);
    update.setHeight(HEIGHT_CAROL_CM);
    update.setBirthDate(existing.getBirthDate());
    update.setGoal(FitnessGoal.CUT);
    update.setGender(Gender.FEMALE);
    update.setPlanStrategy(existing.getPlanStrategy());

    when(personRepository.findByClientId(MOBILE_CLIENT_1)).thenReturn(Optional.of(existing));
    when(personService.calculateBMI(WEIGHT_CAROL_UPDATED_KG, HEIGHT_CAROL_CM))
        .thenReturn(BMI_CAROL_UPDATED);
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
  void updatePersonRejectsOtherClient() {
    ClientContext.setClientId(MOBILE_CLIENT_2);

    PersonSimple updateAttempt = new PersonSimple();
    updateAttempt.setName("Carol Updated");
    updateAttempt.setWeight(WEIGHT_CAROL_UPDATED_KG);
    updateAttempt.setHeight(HEIGHT_CAROL_CM);
    updateAttempt.setBirthDate(DOB_CAROL);
    updateAttempt.setGoal(FitnessGoal.CUT);
    updateAttempt.setGender(Gender.FEMALE);

    when(personRepository.findByClientId(MOBILE_CLIENT_2)).thenReturn(Optional.empty());

    assertThrows(
        ResponseStatusException.class, () -> personController.updatePerson(updateAttempt));
    verify(personRepository, never()).save(any(PersonSimple.class));
  }

  /**
   * Valid deletion scenario confirming owner access and context cleanup.
   */
  @Test
  @DisplayName("Delete removes record for owner and clears context")
  void deletePersonAllowsOwnerAndClearsContext() {
    ClientContext.setClientId(MOBILE_CLIENT_1);
    PersonSimple existing = buildPerson("Dana", Gender.FEMALE, FitnessGoal.BULK, MOBILE_CLIENT_1);
    existing.setWeight(WEIGHT_DANA_KG);
    existing.setHeight(HEIGHT_DANA_CM);
    existing.setBirthDate(DOB_DANA);
    existing.setId(PERSON_ID_DANA);

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
  void deletePersonRejectsOtherClient() {
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
    person.setWeight(WEIGHT_BASE_KG);
    person.setHeight(HEIGHT_BASE_CM);
    person.setBirthDate(DOB_DEFAULT);
    person.setPlanStrategy(PlanStrategy.BOTH);
    return person;
  }
}
