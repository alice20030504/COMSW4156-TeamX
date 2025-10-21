package com.teamx.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.repository.PersonRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.teamx.fitness.model.PersonSimple;
import com.teamx.fitness.repository.PersonRepository;
import com.teamx.fitness.security.ClientContext;
import com.teamx.fitness.service.PersonService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Focused unit tests for {@link PersonController} covering core calculation and client-scoped
 * behaviors with mocked collaborators.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PersonController basic calculations")
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PersonService personService;

    @MockBean
    private PersonRepository personRepository;

    // ============================================
    // CRUD Operations Tests
    // ============================================

    /**
     * Tests GET /api/persons endpoint to retrieve all persons for a client.
     *
     * <p>This test verifies that the controller correctly retrieves all Person records
     * associated with the authenticated client by delegating to PersonRepository's
     * client-filtered query method. This is fundamental for data isolation, ensuring
     * each client only sees their own data.</p>
     *
     * <p><strong>Data Isolation Significance:</strong> In a multi-tenant application,
     * it's critical that getAllPersons() filters by client ID to prevent data leakage.
     * This test validates that repository filtering works correctly at the controller level.</p>
     *
     * <p><strong>Expected Response:</strong> Returns 200 OK with a JSON array of all
     * persons belonging to the client. Each person object should include complete
     * data (name, weight, height, birthDate, clientId).</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getAllPersons - Returns all persons for the authenticated client")
    void testGetAllPersons_ReturnsClientData() throws Exception {
        // Given: Repository returns list of persons for client
        PersonSimple person1 = new PersonSimple("Alice", 65.0, 170.0,
            LocalDate.of(1990, 5, 15), "mobile-app1");
        person1.setId(1L);
        PersonSimple person2 = new PersonSimple("Bob", 80.0, 180.0,
            LocalDate.of(1985, 3, 20), "mobile-app1");
        person2.setId(2L);

        when(personRepository.findByClientId("mobile-app1"))
            .thenReturn(Arrays.asList(person1, person2));

        // When: GET request to /api/persons with client header
        // Then: Returns 200 OK with array of 2 persons
        mockMvc.perform(get("/api/persons")
                .header("X-Client-ID", "mobile-app1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Alice")))
                .andExpect(jsonPath("$[0].weight", is(65.0)))
                .andExpect(jsonPath("$[1].name", is("Bob")))
                .andExpect(jsonPath("$[1].weight", is(80.0)));

        verify(personRepository).findByClientId("mobile-app1");
    }

    /**
     * Tests GET /api/persons endpoint when client has no data.
     *
     * <p>This boundary test ensures the endpoint handles empty result sets gracefully,
     * returning an empty array rather than null or throwing an error. This is common
     * for new clients or after data deletion.</p>
     *
     * <p><strong>Empty State Handling:</strong> Returning empty arrays (not null) is a
     * REST API best practice that prevents client-side null pointer exceptions and
     * simplifies client code with consistent array processing.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getAllPersons - Returns empty array when client has no data")
    void testGetAllPersons_EmptyList() throws Exception {
        // Given: Repository returns empty list for client with no data
        when(personRepository.findByClientId("mobile-app2"))
            .thenReturn(Collections.emptyList());

        // When: GET request to /api/persons
        // Then: Returns 200 OK with empty array
        mockMvc.perform(get("/api/persons")
                .header("X-Client-ID", "mobile-app2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Tests GET /api/persons/{id} endpoint for successful retrieval.
     *
     * <p>This test verifies that the controller correctly retrieves a specific person
     * by ID, but only if that person belongs to the authenticated client. This enforces
     * data isolation at the individual record level, preventing unauthorized access
     * to other clients' data.</p>
     *
     * <p><strong>Authorization Logic:</strong> The controller uses
     * PersonRepository.findByIdAndClientId() which implements a dual-key lookup,
     * ensuring both the ID exists AND belongs to the requesting client.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getPersonById - Returns person when ID exists and belongs to client")
    void testGetPersonById_Success() throws Exception {
        // Given: Repository finds person with matching ID and client ID
        PersonSimple person = new PersonSimple("Charlie", 75.0, 175.0,
            LocalDate.of(1992, 7, 10), "mobile-app1");
        person.setId(5L);

        when(personRepository.findByIdAndClientId(5L, "mobile-app1"))
            .thenReturn(Optional.of(person));

        // When: GET request to /api/persons/5
        // Then: Returns 200 OK with person details
        mockMvc.perform(get("/api/persons/5")
                .header("X-Client-ID", "mobile-app1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.name", is("Charlie")))
                .andExpect(jsonPath("$.weight", is(75.0)))
                .andExpect(jsonPath("$.height", is(175.0)))
                .andExpect(jsonPath("$.clientId", is("mobile-app1")));
    }

    /**
     * Tests GET /api/persons/{id} endpoint when person not found or belongs to different client.
     *
     * <p>This security-critical test verifies that attempting to access a non-existent
     * person ID or another client's person ID returns 404 Not Found. This prevents
     * information disclosure by giving identical responses for both scenarios,
     * following the security principle of "fail closed" and "constant-time comparison".</p>
     *
     * <p><strong>Security Consideration:</strong> Returning 404 for both "not found" and
     * "found but unauthorized" prevents attackers from enumerating valid IDs through
     * differential response analysis. A 403 would confirm the ID exists but is forbidden.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("getPersonById - Returns 404 when ID not found or belongs to different client")
    void testGetPersonById_NotFound() throws Exception {
        // Given: Repository returns empty (person not found or belongs to different client)
        when(personRepository.findByIdAndClientId(999L, "mobile-app1"))
            .thenReturn(Optional.empty());

        // When: GET request to /api/persons/999
        // Then: Returns 404 Not Found (security by obscurity)
        mockMvc.perform(get("/api/persons/999")
                .header("X-Client-ID", "mobile-app1"))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests POST /api/persons endpoint for creating a new person.
     *
     * <p>This test verifies that the controller correctly handles person creation by:
     * <ol>
     *   <li>Accepting JSON request body with person data</li>
     *   <li>Automatically associating the person with the authenticated client ID</li>
     *   <li>Delegating persistence to PersonRepository</li>
     *   <li>Returning 201 Created with the saved person (including generated ID)</li>
     * </ol>
     * </p>
     *
     * <p><strong>Automatic Client Association:</strong> The controller sets the client ID
     * automatically from the authentication context, preventing clients from creating
     * data for other clients by manipulating the request body.</p>
     *
     * <p><strong>HTTP 201 Created:</strong> Following REST conventions, successful resource
     * creation returns 201 (not 200) to indicate a new resource was created.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("createPerson - Creates new person and returns 201 Created")
    void testCreatePerson_Success() throws Exception {
        // Given: Client sends new person data without ID
        PersonSimple newPerson = new PersonSimple();
        newPerson.setName("David");
        newPerson.setWeight(70.0);
        newPerson.setHeight(180.0);
        newPerson.setBirthDate(LocalDate.of(1995, 1, 1));

        // Repository saves and returns person with generated ID
        PersonSimple savedPerson = new PersonSimple("David", 70.0, 180.0,
            LocalDate.of(1995, 1, 1), "mobile-app1");
        savedPerson.setId(10L);

        when(personRepository.save(any(PersonSimple.class))).thenReturn(savedPerson);

        // When: POST request to /api/persons with JSON body
        // Then: Returns 201 Created with saved person including ID and client ID
        mockMvc.perform(post("/api/persons")
                .header("X-Client-ID", "mobile-app1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPerson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("David")))
                .andExpect(jsonPath("$.clientId", is("mobile-app1")));

        verify(personRepository).save(any(PersonSimple.class));
    }

    /**
     * Tests PUT /api/persons/{id} endpoint for updating an existing person.
     *
     * <p>This test verifies the complete update workflow:
     * <ol>
     *   <li>Controller finds existing person by ID and client ID</li>
     *   <li>Updates mutable fields (name, weight, height, birthDate)</li>
     *   <li>Preserves immutable fields (ID, clientId)</li>
     *   <li>Saves updated person via repository</li>
     *   <li>Returns 200 OK with updated person</li>
     * </ol>
     * </p>
     *
     * <p><strong>Selective Field Updates:</strong> The controller explicitly sets only
     * updatable fields, protecting ID and clientId from modification. This prevents
     * clients from hijacking other clients' data or creating ID conflicts.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("updatePerson - Updates existing person and returns 200 OK")
    void testUpdatePerson_Success() throws Exception {
        // Given: Existing person in database
        PersonSimple existingPerson = new PersonSimple("Eve", 60.0, 165.0,
            LocalDate.of(1988, 8, 8), "mobile-app1");
        existingPerson.setId(15L);

        // Client sends updated data
        PersonSimple updateData = new PersonSimple();
        updateData.setName("Eve Updated");
        updateData.setWeight(62.0);
        updateData.setHeight(166.0);
        updateData.setBirthDate(LocalDate.of(1988, 8, 9));

        // Repository operations
        when(personRepository.findByIdAndClientId(15L, "mobile-app1"))
            .thenReturn(Optional.of(existingPerson));
        when(personRepository.save(any(PersonSimple.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When: PUT request to /api/persons/15 with updated JSON
        // Then: Returns 200 OK with updated person
        mockMvc.perform(put("/api/persons/15")
                .header("X-Client-ID", "mobile-app1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(15)))
                .andExpect(jsonPath("$.name", is("Eve Updated")))
                .andExpect(jsonPath("$.weight", is(62.0)))
                .andExpect(jsonPath("$.height", is(166.0)))
                .andExpect(jsonPath("$.clientId", is("mobile-app1")));

        verify(personRepository).save(any(PersonSimple.class));
    }

    /**
     * Tests PUT /api/persons/{id} endpoint when person not found or belongs to different client.
     *
     * <p>This security test ensures that update attempts on non-existent or unauthorized
     * person records fail safely with 404 Not Found, without making any database changes.
     * This prevents unauthorized modification of other clients' data.</p>
     *
     * <p><strong>No-Op on Failure:</strong> When the person isn't found, the controller
     * returns 404 immediately without calling repository.save(), preventing partial
     * updates or race conditions.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("updatePerson - Returns 404 when person not found or belongs to different client")
    void testUpdatePerson_NotFound() throws Exception {
        // Given: Person does not exist or belongs to different client
        PersonSimple updateData = new PersonSimple();
        updateData.setName("Hacker");

        when(personRepository.findByIdAndClientId(999L, "mobile-app1"))
            .thenReturn(Optional.empty());

        // When: PUT request to /api/persons/999
        // Then: Returns 404 and does not call save()
        mockMvc.perform(put("/api/persons/999")
                .header("X-Client-ID", "mobile-app1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound());

        verify(personRepository, never()).save(any(PersonSimple.class));
    }

    /**
     * Tests DELETE /api/persons/{id} endpoint for successful deletion.
     *
     * <p>This test verifies the delete workflow:
     * <ol>
     *   <li>Controller verifies person exists and belongs to client</li>
     *   <li>Calls repository.delete() to remove the record</li>
     *   <li>Returns 204 No Content (successful deletion with no body)</li>
     * </ol>
     * </p>
     *
     * <p><strong>HTTP 204 No Content:</strong> Following REST conventions, successful
     * deletion returns 204 (not 200) with an empty response body, as there's nothing
     * meaningful to return after deletion.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("deletePerson - Deletes person and returns 204 No Content")
    void testDeletePerson_Success() throws Exception {
        // Given: Person exists and belongs to client
        PersonSimple person = new PersonSimple("Frank", 85.0, 185.0,
            LocalDate.of(1980, 12, 31), "mobile-app1");
        person.setId(20L);

        when(personRepository.findByIdAndClientId(20L, "mobile-app1"))
            .thenReturn(Optional.of(person));

        // When: DELETE request to /api/persons/20
        // Then: Returns 204 No Content
        mockMvc.perform(delete("/api/persons/20")
                .header("X-Client-ID", "mobile-app1"))
                .andExpect(status().isNoContent());

        verify(personRepository).delete(person);
    }

    /**
     * Tests DELETE /api/persons/{id} endpoint when person not found or belongs to different client.
     *
     * <p>This security test ensures that delete attempts on non-existent or unauthorized
     * records fail safely with 404 Not Found, without making any database changes. This
     * prevents clients from deleting other clients' data.</p>
     *
     * <p><strong>Idempotency Consideration:</strong> While REST DELETE should ideally be
     * idempotent (returning 204 even if already deleted), returning 404 here provides
     * better security by not confirming whether an ID exists in the system.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("deletePerson - Returns 404 when person not found or belongs to different client")
    void testDeletePerson_NotFound() throws Exception {
        // Given: Person does not exist or belongs to different client
        when(personRepository.findByIdAndClientId(999L, "mobile-app1"))
            .thenReturn(Optional.empty());

        // When: DELETE request to /api/persons/999
        // Then: Returns 404 and does not call delete()
        mockMvc.perform(delete("/api/persons/999")
                .header("X-Client-ID", "mobile-app1"))
                .andExpect(status().isNotFound());

        verify(personRepository, never()).delete(any(PersonSimple.class));
    }

    // ============================================
    // /api/persons/bmi Tests
    // ============================================

    /**
     * Tests BMI calculation endpoint with valid weight and height parameters.
     *
     * <p>This test verifies that the controller correctly handles a typical BMI calculation
     * request by accepting valid numeric parameters, delegating the calculation to PersonService,
     * and returning a properly formatted JSON response with 200 OK status.</p>
     *
     * <p><strong>Test Significance:</strong> BMI calculation is a core feature used by clients
     * to assess health metrics. Ensuring accurate parameter handling and response formatting
     * is critical for client integration.</p>
     *
     * <p><strong>Expected Response Structure:</strong></p>
     * <pre>
     * {
     *   "weight": 70.0,
     *   "height": 175.0,
     *   "bmi": 22.86,
     *   "category": "Normal weight"
     * }
     * </pre>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateBMI - Valid: Returns 200 OK with BMI calculation for valid parameters")
    void testCalculateBMI_ValidParameters() throws Exception {
        // Given: PersonService returns a valid BMI calculation
        when(personService.calculateBMI(70.0, 175.0)).thenReturn(22.86);

        // When: GET request to /api/persons/bmi with valid weight and height
        // Then: Returns 200 OK with complete response including BMI and category
        mockMvc.perform(get("/api/persons/bmi")
                .header("X-Client-ID", "mobile-app1")
                .param("weight", "70.0")
                .param("height", "175.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(70.0))
                .andExpect(jsonPath("$.height").value(175.0))
                .andExpect(jsonPath("$.bmi").value(22.86))
                .andExpect(jsonPath("$.category").value("Normal weight"));
    }

    /**
     * Tests BMI endpoint with parameters that result in "Underweight" category.
     *
     * <p>This boundary test verifies that the controller's private getBMICategory() helper
     * method correctly classifies BMI values below 18.5 as "Underweight". This is important
     * for providing accurate health assessments to users.</p>
     *
     * <p><strong>Clinical Significance:</strong> BMI &lt; 18.5 indicates potential health risks
     * associated with being underweight, making accurate categorization essential for
     * health tracking applications.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateBMI - Boundary: Returns 'Underweight' category for BMI < 18.5")
    void testCalculateBMI_BoundaryUnderweight() throws Exception {
        // Given: BMI calculation returns value in underweight range
        when(personService.calculateBMI(55.0, 173.0)).thenReturn(18.4);

        // When: GET request with parameters resulting in low BMI
        // Then: Response includes "Underweight" category
        mockMvc.perform(get("/api/persons/bmi")
                .header("X-Client-ID", "mobile-app1")
                .param("weight", "55.0")
                .param("height", "173.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").value(18.4))
                .andExpect(jsonPath("$.category").value("Underweight"));
    }

    /**
     * Tests BMI endpoint with parameters that result in "Overweight" category.
     *
     * <p>This boundary test ensures proper classification of BMI values between 25 and 30,
     * which fall into the overweight category. This range represents a critical health threshold
     * where lifestyle interventions are typically recommended.</p>
     *
     * <p><strong>Health Context:</strong> BMI 25-30 indicates overweight status, a key metric
     * for fitness management and weight loss goal setting.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateBMI - Boundary: Returns 'Overweight' category for BMI 25-30")
    void testCalculateBMI_BoundaryOverweight() throws Exception {
        // Given: BMI calculation returns value in overweight range
        when(personService.calculateBMI(85.0, 175.0)).thenReturn(27.8);

        // When: GET request with parameters resulting in elevated BMI
        // Then: Response includes "Overweight" category
        mockMvc.perform(get("/api/persons/bmi")
                .header("X-Client-ID", "mobile-app1")
                .param("weight", "85.0")
                .param("height", "175.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").value(27.8))
                .andExpect(jsonPath("$.category").value("Overweight"));
    }

    /**
     * Tests BMI endpoint with parameters that result in "Obese" category.
     *
     * <p>This boundary test verifies classification of BMI values ≥ 30, which indicate obesity.
     * Accurate identification of this category is critical for medical risk assessment and
     * prioritizing health interventions.</p>
     *
     * <p><strong>Medical Importance:</strong> BMI ≥ 30 significantly increases health risks
     * and requires different fitness and nutritional approaches than lower BMI ranges.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateBMI - Boundary: Returns 'Obese' category for BMI >= 30")
    void testCalculateBMI_BoundaryObese() throws Exception {
        // Given: BMI calculation returns value in obese range
        when(personService.calculateBMI(95.0, 170.0)).thenReturn(32.9);

        // When: GET request with parameters resulting in high BMI
        // Then: Response includes "Obese" category
        mockMvc.perform(get("/api/persons/bmi")
                .header("X-Client-ID", "mobile-app1")
                .param("weight", "95.0")
                .param("height", "170.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").value(32.9))
                .andExpect(jsonPath("$.category").value("Obese"));
    }

    /**
     * Tests BMI endpoint when service returns null due to invalid input.
     *
     * <p>This test verifies graceful handling when PersonService cannot calculate BMI
     * (e.g., due to null parameters, zero height). The controller should still return
     * 200 OK but with null BMI and "Unknown" category, allowing clients to handle
     * calculation failures appropriately.</p>
     *
     * <p><strong>Error Handling Philosophy:</strong> Rather than throwing exceptions for
     * business logic failures, the service returns null, and the controller communicates
     * this to clients with a clear "Unknown" category indicator.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateBMI - Invalid: Returns 'Unknown' category when service returns null")
    void testCalculateBMI_ServiceReturnsNull() throws Exception {
        // Given: PersonService returns null (invalid calculation)
        when(personService.calculateBMI(anyDouble(), anyDouble())).thenReturn(null);

        // When: GET request with parameters that can't be calculated
        // Then: Response includes null BMI and "Unknown" category
        mockMvc.perform(get("/api/persons/bmi")
                .header("X-Client-ID", "mobile-app1")
                .param("weight", "70.0")
                .param("height", "0.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").value(nullValue()))
                .andExpect(jsonPath("$.category").value("Unknown"));
    }

    // ============================================
    // /api/persons/age Tests
    // ============================================

    /**
     * Tests age calculation endpoint with a valid birth date.
     *
     * <p>This test verifies that the controller correctly parses ISO 8601 date strings,
     * delegates age calculation to PersonService, and returns the calculated age with
     * the original birth date in the response. Age calculation is fundamental for
     * personalized fitness recommendations and BMR calculations.</p>
     *
     * <p><strong>Date Format:</strong> Uses ISO 8601 format (YYYY-MM-DD) which is the
     * standard for date interchange in REST APIs.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateAge - Valid: Returns 200 OK with age calculation for valid birth date")
    void testCalculateAge_ValidBirthDate() throws Exception {
        // Given: PersonService calculates age as 30 years
        LocalDate birthDate = LocalDate.now().minusYears(30);
        when(personService.calculateAge(birthDate)).thenReturn(30);

        // When: GET request to /api/persons/age with valid ISO date
        // Then: Returns 200 OK with calculated age
        mockMvc.perform(get("/api/persons/age")
                .header("X-Client-ID", "mobile-app1")
                .param("birthDate", birthDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthDate").value(birthDate.toString()))
                .andExpect(jsonPath("$.age").value(30));
    }

    /**
     * Tests age endpoint with boundary case of birth date being today (age 0).
     *
     * <p>This boundary test ensures the endpoint handles newborn cases correctly,
     * returning age 0 rather than throwing errors or returning negative values.
     * This edge case is important for applications supporting infant fitness tracking.</p>
     *
     * <p><strong>Edge Case Handling:</strong> Age 0 is a valid result for same-day births
     * and should be handled gracefully without special error cases.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateAge - Boundary: Returns age 0 for birth date being today")
    void testCalculateAge_BoundaryToday() throws Exception {
        // Given: Person born today has age 0
        LocalDate today = LocalDate.now();
        when(personService.calculateAge(today)).thenReturn(0);

        // When: GET request with today's date
        // Then: Returns age 0 correctly
        mockMvc.perform(get("/api/persons/age")
                .header("X-Client-ID", "mobile-app1")
                .param("birthDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.age").value(0));
    }


    // ============================================
    // /api/persons/calories Tests
    // ============================================

    /**
     * Tests daily calorie needs endpoint with valid parameters for male user.
     *
     * <p>This test verifies the complete workflow of calculating daily calorie needs:
     * the controller accepts multiple parameters, orchestrates two service calls
     * (BMR calculation followed by activity level adjustment), and returns a comprehensive
     * response including both intermediate (BMR) and final (daily calories) values.</p>
     *
     * <p><strong>Nutritional Context:</strong> Daily calorie needs are fundamental for
     * diet planning. The calculation factors in basal metabolic rate (BMR) and activity
     * level, making it essential for personalized nutrition recommendations.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateDailyCalories - Valid: Returns calorie needs for valid male parameters")
    void testCalculateDailyCalories_ValidMale() throws Exception {
        // Given: PersonService calculates BMR and daily calorie needs
        when(personService.calculateBMR(70.0, 175.0, 30, true)).thenReturn(1680.0);
        when(personService.calculateDailyCalorieNeeds(1680.0, 4)).thenReturn(2604.0);

        // When: GET request with complete valid parameters for male user
        // Then: Returns 200 OK with BMR and daily calorie needs
        mockMvc.perform(get("/api/persons/calories")
                .header("X-Client-ID", "mobile-app1")
                .param("weight", "70.0")
                .param("height", "175.0")
                .param("age", "30")
                .param("gender", "male")
                .param("weeklyTrainingFreq", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmr").value(1680.0))
                .andExpect(jsonPath("$.dailyCalories").value(2604.0))
                .andExpect(jsonPath("$.weeklyTrainingFreq").value(4));
    }

    /**
     * Tests calorie endpoint with valid parameters for female user.
     *
     * <p>This test ensures gender-specific BMR formulas are correctly invoked through
     * the controller. The Mifflin-St Jeor equation uses different constants for males
     * and females, making gender parameter handling critical for accurate calculations.</p>
     *
     * <p><strong>Gender Differences:</strong> Female BMR is typically 5-10% lower than
     * male BMR at equivalent weight/height/age, requiring separate validation to ensure
     * the correct formula path is taken.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateDailyCalories - Valid: Returns calorie needs for valid female parameters")
    void testCalculateDailyCalories_ValidFemale() throws Exception {
        // Given: PersonService calculates BMR for female
        when(personService.calculateBMR(60.0, 165.0, 25, false)).thenReturn(1380.0);
        when(personService.calculateDailyCalorieNeeds(1380.0, 3)).thenReturn(1863.0);

        // When: GET request with female gender parameter
        // Then: Returns 200 OK with gender-specific BMR calculation
        mockMvc.perform(get("/api/persons/calories")
                .header("X-Client-ID", "mobile-app1")
                .param("weight", "60.0")
                .param("height", "165.0")
                .param("age", "25")
                .param("gender", "female")
                .param("weeklyTrainingFreq", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmr").value(1380.0))
                .andExpect(jsonPath("$.dailyCalories").value(1863.0));
    }

    /**
     * Tests calorie endpoint with boundary case of sedentary lifestyle (0 training days).
     *
     * <p>This boundary test verifies handling of the minimum activity level. Users with
     * zero training days receive a 1.2x activity multiplier on their BMR, representing
     * sedentary lifestyle. This is a common scenario for users just starting their
     * fitness journey.</p>
     *
     * <p><strong>Activity Level Significance:</strong> The sedentary multiplier (1.2x)
     * is the baseline for daily calorie needs and serves as the starting point for
     * all activity-adjusted calculations.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateDailyCalories - Boundary: Handles sedentary lifestyle (0 training days)")
    void testCalculateDailyCalories_BoundarySedentary() throws Exception {
        // Given: PersonService handles sedentary activity level
        when(personService.calculateBMR(70.0, 175.0, 30, true)).thenReturn(1680.0);
        when(personService.calculateDailyCalorieNeeds(1680.0, 0)).thenReturn(2016.0);

        // When: GET request with 0 weekly training frequency
        // Then: Returns calorie needs with sedentary multiplier (1.2x BMR)
        mockMvc.perform(get("/api/persons/calories")
                .header("X-Client-ID", "mobile-app1")
                .param("weight", "70.0")
                .param("height", "175.0")
                .param("age", "30")
                .param("gender", "male")
                .param("weeklyTrainingFreq", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyCalories").value(2016.0));
    }

    /**
     * Tests calorie endpoint with case-insensitive gender parameter handling.
     *
     * <p>This test verifies that the controller normalizes gender input by accepting
     * various capitalizations (e.g., "Male", "MALE", "male"). This improves API usability
     * and prevents errors from case-sensitive parameter matching.</p>
     *
     * <p><strong>API Usability:</strong> Case-insensitive parameter handling is a best
     * practice that makes the API more forgiving and easier to integrate with various
     * client implementations.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("calculateDailyCalories - Valid: Accepts case-insensitive gender values")
    void testCalculateDailyCalories_CaseInsensitiveGender() throws Exception {
        // Given: PersonService calculates for male
        when(personService.calculateBMR(70.0, 175.0, 30, true)).thenReturn(1680.0);
        when(personService.calculateDailyCalorieNeeds(1680.0, 4)).thenReturn(2604.0);

        // When: GET request with uppercase gender parameter
        // Then: Controller handles case-insensitive gender matching
        mockMvc.perform(get("/api/persons/calories")
                .header("X-Client-ID", "mobile-app1")
                .param("weight", "70.0")
                .param("height", "175.0")
                .param("age", "30")
                .param("gender", "MALE")
                .param("weeklyTrainingFreq", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmr").value(1680.0));
    }

    // ============================================
    // /api/persons/health Tests
    // ============================================

    /**
     * Tests health check endpoint for service availability monitoring.
     *
     * <p>This test verifies the health check endpoint which is commonly used by load
     * balancers, monitoring systems, and orchestration platforms to verify service
     * availability. The endpoint requires no parameters and returns service metadata
     * including status, name, and version.</p>
     *
     * <p><strong>Operational Importance:</strong> Health checks are critical for:
     * <ul>
     *   <li>Load balancer routing decisions</li>
     *   <li>Automated service restart triggers</li>
     *   <li>Monitoring system alerts</li>
     *   <li>Deployment verification</li>
     * </ul>
     * </p>
     *
     * <p><strong>Expected Response:</strong> Always returns 200 OK with "UP" status
     * when the service is running, regardless of downstream service states.</p>
     *
     * @throws Exception if MockMvc request fails
     */
    @Test
    @DisplayName("healthCheck - Returns 200 OK with service metadata")
    void testHealthCheck() throws Exception {
        // Given: No service dependencies needed for health check

        // When: GET request to /api/persons/health
        // Then: Returns 200 OK with service status and metadata
        mockMvc.perform(get("/api/persons/health")
                .header("X-Client-ID", "mobile-app1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Personal Fitness Management Service"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
  @Mock private PersonService personService;

  @Mock private PersonRepository personRepository;

  @InjectMocks private PersonController personController;

  @AfterEach
  void clearClientContext() {
    ClientContext.clear();
  }

  /**
   * Ensures the BMI endpoint returns consistent payloads across representative inputs.
   */
  @ParameterizedTest
  @MethodSource("calculateBmiScenarios")
  @DisplayName("calculateBMI handles valid, boundary, and invalid scenarios")
  void calculateBMIHandlesScenarios(
      String description,
      Double weight,
      Double height,
      Double serviceResult,
      String expectedCategory) {

    when(personService.calculateBMI(weight, height)).thenReturn(serviceResult);

    ResponseEntity<Map<String, Object>> response = personController.calculateBMI(weight, height);

    assertEquals(HttpStatus.OK, response.getStatusCode(), description);
    Map<String, Object> body = response.getBody();
    assertNotNull(body, description);
    assertEquals(weight, body.get("weight"), description);
    assertEquals(height, body.get("height"), description);
    assertEquals(serviceResult, body.get("bmi"), description);
    assertEquals(expectedCategory, body.get("category"), description);
  }

  private static Stream<Arguments> calculateBmiScenarios() {
    return Stream.of(
        Arguments.of("Valid: normal BMI", 70.0, 175.0, 22.86, "Normal weight"),
        Arguments.of("Boundary: underweight classification", 50.0, 180.0, 15.43, "Underweight"),
        Arguments.of("Boundary: overweight classification", 85.0, 178.0, 26.82, "Overweight"),
        Arguments.of("Boundary: obese classification", 110.0, 170.0, 38.06, "Obese"),
        Arguments.of("Invalid: service returns null BMI", 70.0, 0.0, null, "Unknown"));
  }

  /**
   * Verifies the age endpoint parses dates and returns expected values for typical, boundary, and
   * invalid service responses.
   */
  @ParameterizedTest
  @MethodSource("calculateAgeScenarios")
  @DisplayName("calculateAge returns age for provided birth dates")
  void calculateAgeHandlesScenarios(String description, String birthDate, Integer expectedAge) {
    LocalDate parsedDate = LocalDate.parse(birthDate);
    when(personService.calculateAge(parsedDate)).thenReturn(expectedAge);

    ResponseEntity<Map<String, Object>> response = personController.calculateAge(birthDate);

    assertEquals(HttpStatus.OK, response.getStatusCode(), description);
    Map<String, Object> body = response.getBody();
    assertNotNull(body, description);
    assertEquals(birthDate, body.get("birthDate"), description);
    if (expectedAge == null) {
      assertNull(body.get("age"), description);
    } else {
      assertEquals(expectedAge, body.get("age"), description);
    }
  }

  private static Stream<Arguments> calculateAgeScenarios() {
    return Stream.of(
        Arguments.of(
            "Valid: adult birth date", LocalDate.of(1995, 5, 15).toString(), 29),
        Arguments.of("Boundary: born today", LocalDate.now().toString(), 0),
        Arguments.of(
            "Invalid: service returns null age", LocalDate.now().minusYears(40).toString(), null));
  }

  /**
   * Confirms the calorie endpoint composes BMR and activity factors while honoring null guards.
   */
  @ParameterizedTest
  @MethodSource("calculateDailyCalorieNeedsScenarios")
  @DisplayName("calculateDailyCalorieNeeds composes BMR and activity factor")
  void calculateDailyCalorieNeedsHandlesScenarios(
      String description,
      Double weight,
      Double height,
      Integer age,
      String gender,
      Integer weeklyTrainingFreq,
      Double bmrResult,
      Double caloriesResult) {

    boolean isMale = "male".equalsIgnoreCase(gender);
    when(personService.calculateBMR(weight, height, age, isMale)).thenReturn(bmrResult);
    when(personService.calculateDailyCalorieNeeds(bmrResult, weeklyTrainingFreq))
        .thenReturn(caloriesResult);

    ResponseEntity<Map<String, Object>> response =
        personController.calculateDailyCalories(weight, height, age, gender, weeklyTrainingFreq);

    assertEquals(HttpStatus.OK, response.getStatusCode(), description);
    Map<String, Object> body = response.getBody();
    assertNotNull(body, description);
    assertEquals(bmrResult, body.get("bmr"), description);
    assertEquals(caloriesResult, body.get("dailyCalories"), description);
    assertEquals(weeklyTrainingFreq, body.get("weeklyTrainingFreq"), description);
  }

  private static Stream<Arguments> calculateDailyCalorieNeedsScenarios() {
    return Stream.of(
        Arguments.of(
            "Valid: male moderate activity",
            70.0,
            175.0,
            30,
            "male",
            3,
            1680.0,
            2604.0),
        Arguments.of(
            "Boundary: sedentary frequency",
            70.0,
            175.0,
            30,
            "male",
            0,
            1680.0,
            2016.0),
        Arguments.of(
            "Boundary: uppercase gender handled as male",
            70.0,
            175.0,
            30,
            "MALE",
            4,
            1680.0,
            2604.0),
        Arguments.of(
            "Invalid: missing BMR prevents calorie calculation",
            70.0,
            175.0,
            30,
            "female",
            2,
            null,
            null));
  }

  @Test
  @DisplayName("getAllPersons returns client-scoped records")
  void getAllPersonsReturnsClientData() {
    String clientId = "mobile-app1";
    ClientContext.setClientId(clientId);

    List<PersonSimple> expected =
        List.of(
            new PersonSimple(
                "Alice", 65.0, 170.0, LocalDate.of(1990, 5, 15), clientId));
    when(personRepository.findByClientId(clientId)).thenReturn(expected);

    ResponseEntity<List<PersonSimple>> response = personController.getAllPersons();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expected, response.getBody());
    verify(personRepository).findByClientId(clientId);
  }

  @Test
  @DisplayName("healthCheck reports service availability metadata")
  void healthCheckReturnsMetadata() {
    ResponseEntity<Map<String, String>> response = personController.healthCheck();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    Map<String, String> body = response.getBody();
    assertNotNull(body);
    assertEquals("UP", body.get("status"));
    assertEquals("Personal Fitness Management Service", body.get("service"));
    assertEquals("1.0.0", body.get("version"));
  }
}
