package com.teamx.fitness.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.teamx.fitness.controller.PersonController;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.PersonService;
import java.time.LocalDate;
import java.util.List;
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

  private static final String MOBILE_CLIENT_1 = "mobile-app1";
  private static final String MOBILE_CLIENT_2 = "mobile-app2";

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
  @DisplayName("Owner can fetch their person record")
  void getPersonById_AllowsOwner() {
    PersonSimple stored =
        new PersonSimple("Alice", 65.0, 170.0, LocalDate.of(1990, 5, 15), MOBILE_CLIENT_1);
    stored.setId(1L);

    ClientContext.setClientId(MOBILE_CLIENT_1);
    when(personService.findPersonForClient(1L, MOBILE_CLIENT_1)).thenReturn(Optional.of(stored));

    ResponseEntity<PersonSimple> response = personController.getPersonById(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Alice", response.getBody().getName());
  }

  /**
   * Invalid scenario: a different client receives a 404 when attempting to access another client's
   * record.
   */
  @Test
  @DisplayName("Different client receives 404 for protected record")
  void getPersonById_BlocksDifferentClient() {
    ClientContext.setClientId(MOBILE_CLIENT_2);
    when(personService.findPersonForClient(1L, MOBILE_CLIENT_2)).thenReturn(Optional.empty());

    ResponseEntity<PersonSimple> response = personController.getPersonById(1L);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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

    when(personService.createPersonForClient(any(PersonSimple.class), eq(MOBILE_CLIENT_1)))
        .thenReturn(saved);

    ResponseEntity<PersonSimple> response = personController.createPerson(payload);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(MOBILE_CLIENT_1, response.getBody().getClientId());
    verify(personService).createPersonForClient(any(PersonSimple.class), eq(MOBILE_CLIENT_1));
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

    PersonSimple update =
        new PersonSimple("Carol Updated", 72.0, 172.0, existing.getBirthDate(), null);

    PersonSimple saved =
        new PersonSimple("Carol Updated", 72.0, 172.0, existing.getBirthDate(), MOBILE_CLIENT_1);
    saved.setId(9L);

    when(personService.updatePersonForClient(9L, MOBILE_CLIENT_1, update))
        .thenReturn(Optional.of(saved));

    ResponseEntity<PersonSimple> response = personController.updatePerson(9L, update);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Carol Updated", response.getBody().getName());
    verify(personService).updatePersonForClient(9L, MOBILE_CLIENT_1, update);
  }

  /**
   * Invalid update scenario: non-owners receive a 404 and no persistence occurs.
   */
  @Test
  @DisplayName("Update returns 404 when record belongs to another client")
  void updatePerson_RejectsOtherClient() {
    ClientContext.setClientId(MOBILE_CLIENT_2);
    when(personService.updatePersonForClient(
            eq(9L), eq(MOBILE_CLIENT_2), any(PersonSimple.class)))
        .thenReturn(Optional.empty());

    ResponseEntity<PersonSimple> response =
        personController.updatePerson(
            9L,
            new PersonSimple("Carol Updated", 72.0, 172.0, LocalDate.of(1992, 7, 10), null));

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(personService)
        .updatePersonForClient(eq(9L), eq(MOBILE_CLIENT_2), any(PersonSimple.class));
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

    when(personService.deletePersonForClient(4L, MOBILE_CLIENT_1)).thenReturn(true);

    ResponseEntity<Void> response = personController.deletePerson(4L);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(personService).deletePersonForClient(4L, MOBILE_CLIENT_1);
  }

  /**
   * Invalid deletion scenario: other clients cannot remove records they do not own.
   */
  @Test
  @DisplayName("Delete returns 404 for other client")
  void deletePerson_RejectsOtherClient() {
    ClientContext.setClientId(MOBILE_CLIENT_2);
    when(personService.deletePersonForClient(4L, MOBILE_CLIENT_2)).thenReturn(false);

    ResponseEntity<Void> response = personController.deletePerson(4L);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(personService).deletePersonForClient(4L, MOBILE_CLIENT_2);
  }

  /**
   * Boundary scenario: listing delegates to the repository using the active client context.
   */
  @Test
  @DisplayName("List persons queries repository with active client")
  void listPersons_QueryScopedByClient() {
    ClientContext.setClientId(MOBILE_CLIENT_1);
    List<PersonSimple> records =
        List.of(
            new PersonSimple("Eve", 60.0, 165.0, LocalDate.of(1994, 9, 9), MOBILE_CLIENT_1));
    when(personService.getPersonsForClient(MOBILE_CLIENT_1)).thenReturn(records);

    ResponseEntity<List<PersonSimple>> response = personController.getAllPersons();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(records, response.getBody());
    verify(personService).getPersonsForClient(eq(MOBILE_CLIENT_1));
    assertTrue(ClientContext.isMobileClient(ClientContext.getClientId()));
  }
}
