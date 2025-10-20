package com.teamx.fitness.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.repository.PersonRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests demonstrating client isolation and data separation.
 * Tests that API calls on behalf of different clients work properly without interference.
 * This addresses the rubric requirement for multi-client data isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Client Isolation Integration Tests")
public class ClientIsolationIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private PersonRepository personRepository;

  @Autowired private ObjectMapper objectMapper;

  private static final String MOBILE_CLIENT_1 = "mobile-app1";
  private static final String MOBILE_CLIENT_2 = "mobile-app2";
  private static final String RESEARCH_CLIENT = "research-tool1";
  private static final String CLIENT_ID_HEADER = "X-Client-ID";

  @BeforeEach
  public void setUp() {
    // Clean database before each test
    personRepository.deleteAll();
  }

  @Test
  @DisplayName("Mobile client 1 can only see their own data")
  public void testMobileClient1CanOnlySeOwnData() throws Exception {
    // Create data for mobile-app1
    PersonSimple person1 =
        new PersonSimple("Alice", 65.0, 170.0, LocalDate.of(1990, 5, 15), MOBILE_CLIENT_1);
    personRepository.save(person1);

    // Create data for mobile-app2
    PersonSimple person2 =
        new PersonSimple("Bob", 80.0, 180.0, LocalDate.of(1995, 3, 20), MOBILE_CLIENT_2);
    personRepository.save(person2);

    // Mobile client 1 should only see Alice
    mockMvc
        .perform(get("/api/persons").header(CLIENT_ID_HEADER, MOBILE_CLIENT_1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is("Alice")))
        .andExpect(jsonPath("$[0].clientId", is(MOBILE_CLIENT_1)));
  }

  @Test
  @DisplayName("Mobile client 2 can only see their own data")
  public void testMobileClient2CanOnlySeeOwnData() throws Exception {
    // Create data for mobile-app1
    PersonSimple person1 =
        new PersonSimple("Alice", 65.0, 170.0, LocalDate.of(1990, 5, 15), MOBILE_CLIENT_1);
    personRepository.save(person1);

    // Create data for mobile-app2
    PersonSimple person2 =
        new PersonSimple("Bob", 80.0, 180.0, LocalDate.of(1995, 3, 20), MOBILE_CLIENT_2);
    personRepository.save(person2);

    // Mobile client 2 should only see Bob
    mockMvc
        .perform(get("/api/persons").header(CLIENT_ID_HEADER, MOBILE_CLIENT_2))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is("Bob")))
        .andExpect(jsonPath("$[0].clientId", is(MOBILE_CLIENT_2)));
  }

  @Test
  @DisplayName("Data posted by mobile client 1 does not show up for mobile client 2")
  public void testDataIsolationBetweenClients() throws Exception {
    // Mobile client 1 creates a person
    PersonSimple newPerson = new PersonSimple();
    newPerson.setName("Charlie");
    newPerson.setWeight(75.0);
    newPerson.setHeight(175.0);
    newPerson.setBirthDate(LocalDate.of(1992, 7, 10));

    mockMvc
        .perform(
            post("/api/persons")
                .header(CLIENT_ID_HEADER, MOBILE_CLIENT_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPerson)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.clientId", is(MOBILE_CLIENT_1)));

    // Mobile client 2 should not see Charlie
    mockMvc
        .perform(get("/api/persons").header(CLIENT_ID_HEADER, MOBILE_CLIENT_2))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

    // Mobile client 1 should see Charlie
    mockMvc
        .perform(get("/api/persons").header(CLIENT_ID_HEADER, MOBILE_CLIENT_1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is("Charlie")));
  }

  @Test
  @DisplayName("Mobile client cannot access person from another client by ID")
  public void testMobileClientCannotAccessOtherClientData() throws Exception {
    // Create data for mobile-app1
    PersonSimple person1 =
        new PersonSimple("Alice", 65.0, 170.0, LocalDate.of(1990, 5, 15), MOBILE_CLIENT_1);
    PersonSimple saved = personRepository.save(person1);

    // Mobile client 2 tries to access Alice by ID - should get 404
    mockMvc
        .perform(
            get("/api/persons/" + saved.getId()).header(CLIENT_ID_HEADER, MOBILE_CLIENT_2))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Mobile client cannot update person from another client")
  public void testMobileClientCannotUpdateOtherClientData() throws Exception {
    // Create data for mobile-app1
    PersonSimple person1 =
        new PersonSimple("Alice", 65.0, 170.0, LocalDate.of(1990, 5, 15), MOBILE_CLIENT_1);
    PersonSimple saved = personRepository.save(person1);

    // Mobile client 2 tries to update Alice - should get 404
    PersonSimple updatedPerson = new PersonSimple();
    updatedPerson.setName("Hacked Name");
    updatedPerson.setWeight(100.0);
    updatedPerson.setHeight(200.0);
    updatedPerson.setBirthDate(LocalDate.of(2000, 1, 1));

    mockMvc
        .perform(
            put("/api/persons/" + saved.getId())
                .header(CLIENT_ID_HEADER, MOBILE_CLIENT_2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPerson)))
        .andExpect(status().isNotFound());

    // Verify Alice's data was not changed
    PersonSimple afterUpdate = personRepository.findById(saved.getId()).orElseThrow();
    assert afterUpdate.getName().equals("Alice");
  }

  @Test
  @DisplayName("Mobile client cannot delete person from another client")
  public void testMobileClientCannotDeleteOtherClientData() throws Exception {
    // Create data for mobile-app1
    PersonSimple person1 =
        new PersonSimple("Alice", 65.0, 170.0, LocalDate.of(1990, 5, 15), MOBILE_CLIENT_1);
    PersonSimple saved = personRepository.save(person1);

    // Mobile client 2 tries to delete Alice - should get 404
    mockMvc
        .perform(
            delete("/api/persons/" + saved.getId()).header(CLIENT_ID_HEADER, MOBILE_CLIENT_2))
        .andExpect(status().isNotFound());

    // Verify Alice still exists
    assert personRepository.findById(saved.getId()).isPresent();
  }

  @Test
  @DisplayName("Multiple persons can exist with same name across different clients")
  public void testSameNameAcrossClients() throws Exception {
    // Both clients create a person named "John"
    PersonSimple john1 = new PersonSimple("John", 70.0, 175.0, LocalDate.of(1990, 1, 1), MOBILE_CLIENT_1);
    PersonSimple john2 = new PersonSimple("John", 80.0, 180.0, LocalDate.of(1995, 1, 1), MOBILE_CLIENT_2);

    personRepository.save(john1);
    personRepository.save(john2);

    // Each client should only see their own John
    mockMvc
        .perform(get("/api/persons").header(CLIENT_ID_HEADER, MOBILE_CLIENT_1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].weight", is(70.0)));

    mockMvc
        .perform(get("/api/persons").header(CLIENT_ID_HEADER, MOBILE_CLIENT_2))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].weight", is(80.0)));
  }

  @Test
  @DisplayName("Request without client ID header is rejected")
  public void testRequestWithoutClientIdIsRejected() throws Exception {
    mockMvc
        .perform(get("/api/persons"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", containsString("X-Client-ID header is required")));
  }

  @Test
  @DisplayName("Request with invalid client ID format is rejected")
  public void testRequestWithInvalidClientIdIsRejected() throws Exception {
    mockMvc
        .perform(get("/api/persons").header(CLIENT_ID_HEADER, "invalid-client-123"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath(
                "$.message",
                containsString("Invalid client ID format. Must start with 'mobile-' or 'research-'")));
  }
}
