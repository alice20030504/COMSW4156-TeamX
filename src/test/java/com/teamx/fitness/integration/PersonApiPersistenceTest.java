package com.teamx.fitness.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamx.fitness.repository.PersonRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * End-to-end API test verifying that POST requests persist data that can be retrieved via GET.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PersonApiPersistenceTest {

  private static final String CLIENT_HEADER = "X-Client-ID";

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private PersonRepository personRepository;

  @BeforeEach
  void resetDatabase() {
    personRepository.deleteAll();
  }

  @Test
  @DisplayName("POST followed by GET returns the persisted person for the client")
  void postThenGetReturnsPersistedPerson() throws Exception {
    String clientId = "mobile-integration";

    Map<String, Object> payload = new HashMap<>();
    payload.put("name", "Integration Tester");
    payload.put("weight", 70.5);
    payload.put("height", 175.0);
    payload.put("birthDate", LocalDate.of(1990, 5, 15).toString());

    mockMvc
        .perform(
            post("/api/persons")
                .header(CLIENT_HEADER, clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.clientId").value(clientId))
        .andExpect(jsonPath("$.name").value("Integration Tester"));

    MvcResult mvcResult =
        mockMvc
            .perform(get("/api/persons").header(CLIENT_HEADER, clientId))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode responseNode =
        objectMapper.readTree(mvcResult.getResponse().getContentAsString());

    boolean foundPersistedPerson = false;
    for (JsonNode node : responseNode) {
      if (clientId.equals(node.path("clientId").asText())) {
        assertThat(node.path("name").asText()).isEqualTo("Integration Tester");
        assertThat(node.path("weight").asDouble()).isEqualTo(70.5);
        assertThat(node.path("height").asDouble()).isEqualTo(175.0);
        foundPersistedPerson = true;
        break;
      }
    }

    assertThat(foundPersistedPerson)
        .as("Expected to find the person saved for client %s", clientId)
        .isTrue();
  }
}
