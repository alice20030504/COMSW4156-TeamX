package com.teamx.fitness.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import com.teamx.fitness.controller.PersonController;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.repository.PersonRepository;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.AuthService;
import com.teamx.fitness.service.PersonService;
import java.time.LocalDate;
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

/**
 * Verifies that {@link PersonController} enforces client isolation rules using mocked
 * repositories and the thread-local {@link ClientContext}. Each endpoint is exercised with
 * representative valid, boundary, and invalid scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Client isolation safeguards")
class ClientIsolationIntegrationTest {
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(authService.createUnauthorizedResponse()).thenReturn(
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ID or birth date."));
        // Set up lenient stubs for common auth validations
        lenient().when(authService.validateUserAccess(anyLong(), any(LocalDate.class))).thenReturn(false);
    }

  private static final String MOBILE_CLIENT_1 = "mobile-app1";
  private static final String MOBILE_CLIENT_2 = "mobile-app2";

  @Mock private PersonRepository personRepository;

  @Mock private PersonService personService;

  @Mock private AuthService authService;

  @InjectMocks private PersonController personController;

  @AfterEach
  void clearContext() {
    ClientContext.clear();
  }

  /**
   * Valid scenario: the owning client retrieves their record successfully.
   */
  @Test
  @DisplayName("Owner can fetch their person record")
  void getPersonById_AllowsOwner() {
    PersonSimple stored =
        new PersonSimple("Alice", 65.0, 170.0, LocalDate.of(1990, 5, 15), MOBILE_CLIENT_1);
    stored.setId(1L);
    String birthDate = "1990-05-15";

    ClientContext.setClientId(MOBILE_CLIENT_1);
    when(authService.validateUserAccess(1L, LocalDate.parse(birthDate))).thenReturn(true);
    when(personRepository.findByIdAndClientId(1L, MOBILE_CLIENT_1)).thenReturn(Optional.of(stored));

    ResponseEntity<?> response = personController.getPerson(1L, LocalDate.parse(birthDate));

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Alice", ((PersonSimple) response.getBody()).getName());
  }

  /**
   * Invalid scenario: a different client receives a 401 when attempting to access another client's
   * record due to authentication failure.
   */
  @Test
  @DisplayName("Different client receives 401 for invalid authentication")
  void getPersonById_BlocksDifferentClient() {
    String birthDate = "1990-05-15";
    ClientContext.setClientId(MOBILE_CLIENT_2);
    // Default behavior set in setUp()

    ResponseEntity<?> response = personController.getPerson(1L, LocalDate.parse(birthDate));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Valid scenario: created records are automatically tagged with the active client.
   */
  @Test
  @DisplayName("Create person assigns current client ID")
  void createPerson_AssignsClientId() {
    ClientContext.setClientId(MOBILE_CLIENT_1);
    PersonSimple payload =
        new PersonSimple("Bob", 80.0, 180.0, LocalDate.of(1995, 3, 20), null);
    PersonSimple saved =
        new PersonSimple("Bob", 80.0, 180.0, LocalDate.of(1995, 3, 20), MOBILE_CLIENT_1);
    saved.setId(5L);

    when(personRepository.save(any(PersonSimple.class))).thenReturn(saved);

    ResponseEntity<PersonSimple> response = personController.createPerson(payload);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(MOBILE_CLIENT_1, response.getBody().getClientId());
    verify(personRepository).save(any(PersonSimple.class));
  }

  /**
   * Valid update scenario demonstrating owner access to modify their record.
   */
  @Test
  @DisplayName("Update respects client ownership")
  void updatePerson_RespectsOwnership() {
    ClientContext.setClientId(MOBILE_CLIENT_1);
    PersonSimple existing =
        new PersonSimple("Carol", 70.0, 172.0, LocalDate.of(1992, 7, 10), MOBILE_CLIENT_1);
    existing.setId(9L);
    String birthDate = "1992-07-10";

    PersonSimple update =
        new PersonSimple("Carol Updated", 72.0, 172.0, existing.getBirthDate(), null);
    update.setId(9L);

    when(authService.validateUserAccess(9L, LocalDate.parse(birthDate))).thenReturn(true);
    when(personRepository.findByIdAndClientId(9L, MOBILE_CLIENT_1)).thenReturn(Optional.of(existing));
    doAnswer(invocation -> {
        PersonSimple savedPerson = invocation.getArgument(0);
        savedPerson.setName("Carol Updated");
        savedPerson.setClientId(MOBILE_CLIENT_1);
        return savedPerson;
    }).when(personRepository).save(any(PersonSimple.class));

    ResponseEntity<?> response = personController.updatePerson(9L, LocalDate.parse(birthDate), update);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Carol Updated", ((PersonSimple) response.getBody()).getName());
  }

  /**
   * Invalid update scenario: non-owners receive a 401 due to authentication failure.
   */
  @Test
  @DisplayName("Update returns 401 when authentication fails")
  void updatePerson_RejectsOtherClient() {
    String birthDate = "1992-07-10";
    ClientContext.setClientId(MOBILE_CLIENT_2);
    // Default behavior set in setUp()

    ResponseEntity<?> response =
        personController.updatePerson(
            9L,
            LocalDate.parse(birthDate),
            new PersonSimple("Carol Updated", 72.0, 172.0, LocalDate.of(1992, 7, 10), null));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(personRepository, never()).save(any(PersonSimple.class));
  }

  /**
   * Valid deletion scenario confirming owner access and context cleanup.
   */
  @Test
  @DisplayName("Delete removes record for owner and clears context")
  void deletePerson_AllowsOwnerAndClearsContext() {
    ClientContext.setClientId(MOBILE_CLIENT_1);
    PersonSimple existing =
        new PersonSimple("Dana", 68.0, 168.0, LocalDate.of(1991, 1, 1), MOBILE_CLIENT_1);
    existing.setId(4L);
    String birthDate = "1991-01-01";

    when(authService.validateUserAccess(4L, LocalDate.parse(birthDate))).thenReturn(true);
    when(personRepository.findByIdAndClientId(4L, MOBILE_CLIENT_1)).thenReturn(Optional.of(existing));

    ResponseEntity<?> response = personController.deletePerson(4L, LocalDate.parse(birthDate));

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(personRepository).findByIdAndClientId(4L, MOBILE_CLIENT_1);
    verify(personRepository).deleteById(4L);

    ClientContext.clear();
    assertFalse(ClientContext.isMobileClient(ClientContext.getClientId()));
  }

  /**
   * Invalid deletion scenario: other clients cannot remove records they do not own.
   */
  @Test
  @DisplayName("Delete returns 401 for authentication failure")
  void deletePerson_RejectsOtherClient() {
    String birthDate = "1991-01-01";
    ClientContext.setClientId(MOBILE_CLIENT_2);
    // Default behavior set in setUp()

    ResponseEntity<?> response = personController.deletePerson(4L, LocalDate.parse(birthDate));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(personRepository, never()).deleteById(any());
  }

}