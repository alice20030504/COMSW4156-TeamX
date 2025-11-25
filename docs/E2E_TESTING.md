# End-to-End Client/Service Testing

This document describes the end-to-end tests that exercise the client application against the backend service, including manual test checklists and validation outcomes.

## Overview

End-to-end (E2E) testing verifies that the complete workflow from user interaction through the client to backend service processing returns correct results. This document covers both the **Mobile Client** and **Research Client** workflows.

---

## Test Environment Setup

### Prerequisites

- Docker Desktop running
- Backend and frontend services started: `docker compose up -d --build`
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:3000`
- Modern web browser (Chrome, Firefox, Safari, or Edge)

### Starting Fresh

```powershell
# Hard clean (removes DB data)
docker compose -f docker-compose.yml -f docker-compose.tests.yml down -v --remove-orphans

# Build and start
docker compose up -d --build

# Verify health
# Check: http://localhost:8080/health
```

---

## Mobile Client E2E Tests

### Test 1: User Registration and Profile Creation

**Objective:** Verify that a mobile user can register and create a fitness profile.

| Step | Action                 | Input                                                                                               | Expected Outcome                                                                     | Actual Outcome                   | Status |
| ---- | ---------------------- | --------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------ | -------------------------------- | ------ |
| 1    | Open mobile client     | Navigate to `http://localhost:3000` → click "Mobile User"                                           | Landing page loads, redirects to mobile.html                                         | ✓ Mobile app loaded              | ✓ PASS |
| 2    | Fill registration form | Name: "Alice Smith", Weight: 65kg, Height: 172cm, Birth Date: 1995-03-18, Gender: FEMALE, Goal: CUT | Form filled with valid data                                                          | ✓ Form accepts input             | ✓ PASS |
| 3    | Submit registration    | Click "Register" button                                                                             | Response shows client ID (e.g., `mobile-abc123`) displayed or stored in localStorage | ✓ Client ID generated and stored | ✓ PASS |
| 4    | Verify profile saved   | Refresh page or navigate away and back                                                              | Profile data persists in localStorage, client ID remains                             | ✓ Profile data persisted         | ✓ PASS |

**Manual Verification Checklist:**

- [ ] Browser console shows no errors
- [ ] localStorage contains `clientId` and `apiUrl`
- [ ] Response status is 200 OK
- [ ] Client ID format is valid (starts with `mobile-`)

---

### Test 2: Goal Plan Configuration

**Objective:** Verify that a user can configure a personalized fitness goal plan.

| Step | Action                    | Input                                                                                    | Expected Outcome                            | Actual Outcome             | Status |
| ---- | ------------------------- | ---------------------------------------------------------------------------------------- | ------------------------------------------- | -------------------------- | ------ |
| 1    | Access plan configuration | Click "Configure Goal Plan" button after registration                                    | Plan configuration form appears             | ✓ Form displayed           | ✓ PASS |
| 2    | Fill plan details         | Target Change: 3.5kg, Duration: 6 weeks, Training Frequency: 4 days/week, Strategy: BOTH | Form accepts all inputs                     | ✓ Form filled successfully | ✓ PASS |
| 3    | Submit plan               | Click "Save Plan" button                                                                 | Response confirms plan saved with client ID | ✓ Plan saved successfully  | ✓ PASS |
| 4    | Verify plan persists      | Refresh page                                                                             | Plan data retrieved from backend            | ✓ Plan data persisted      | ✓ PASS |

**Manual Verification Checklist:**

- [ ] Browser console shows no errors
- [ ] Response includes plan ID and configuration details
- [ ] Status message displays "Plan saved successfully"
- [ ] Plan details match input values

---

### Test 3: BMI Calculation

**Objective:** Verify that BMI is correctly calculated and displayed with proper formatting.

| Step | Action                       | Input                                                          | Expected Outcome                                            | Actual Outcome          | Status |
| ---- | ---------------------------- | -------------------------------------------------------------- | ----------------------------------------------------------- | ----------------------- | ------ |
| 1    | Register with known metrics  | Name: "Bob Johnson", Weight: 75kg, Height: 180cm, Gender: MALE | User registered                                             | ✓ Registration complete | ✓ PASS |
| 2    | Request BMI calculation      | Click "Get BMI" button                                         | BMI calculated: 75 / (1.8²) = 23.15 kg/m²                   | ✓ BMI: 23.15 displayed  | ✓ PASS |
| 3    | Verify number formatting     | Check displayed BMI value                                      | BMI shows max 2 decimal places (e.g., 23.15, not 23.148888) | ✓ Formatted correctly   | ✓ PASS |
| 4    | Verify result display format | Check result rendering                                         | Result displays in card format with labels (not raw JSON)   | ✓ Card format displayed | ✓ PASS |

**Manual Verification Checklist:**

- [ ] BMI value displayed in user-friendly card (not JSON)
- [ ] Number has maximum 2 decimal places
- [ ] Card includes labels like "BMI" and "Status"
- [ ] Result is specific to the registered user (client ID verified)

---

### Test 4: Calorie Recommendations

**Objective:** Verify that daily calorie recommendations are calculated and displayed with correct formatting.

| Step | Action                      | Input                                                            | Expected Outcome                                                    | Actual Outcome             | Status |
| ---- | --------------------------- | ---------------------------------------------------------------- | ------------------------------------------------------------------- | -------------------------- | ------ |
| 1    | Register and configure plan | User: "Carol White", Goal: BULK, Training Frequency: 5 days/week | User and plan created                                               | ✓ Setup complete           | ✓ PASS |
| 2    | Request calorie calculation | Click "Get Calories" button                                      | Daily calorie target calculated based on BMR and goal               | ✓ Calories displayed       | ✓ PASS |
| 3    | Verify formatting           | Check calorie value                                              | Shows max 2 decimal places (e.g., 2150.50, not 2150.5234)           | ✓ Formatted to 2 decimals  | ✓ PASS |
| 4    | Verify result display       | Check rendering                                                  | Result in card format with breakdown (Maintenance, Surplus/Deficit) | ✓ Card format with details | ✓ PASS |

**Manual Verification Checklist:**

- [ ] Calorie value formatted to max 2 decimal places
- [ ] Card displays multiple fields (Maintenance Calories, Target Calories, etc.)
- [ ] Goal and training frequency considered in calculation
- [ ] All-caps labels preserved (e.g., "BMR" not "B M R")

---

### Test 5: Fitness Recommendations

**Objective:** Verify that personalized fitness recommendations are provided based on user profile and plan.

| Step | Action                          | Input                                                | Expected Outcome                                                   | Actual Outcome               | Status |
| ---- | ------------------------------- | ---------------------------------------------------- | ------------------------------------------------------------------ | ---------------------------- | ------ |
| 1    | Setup user with goal plan       | User with goal: CUT, Training Frequency: 3 days/week | User and plan configured                                           | ✓ Setup complete             | ✓ PASS |
| 2    | Request recommendations         | Click "Get Recommendation" button                    | Recommendations displayed for CUT goal (cardio, strength training) | ✓ Recommendations displayed  | ✓ PASS |
| 3    | Verify recommendation relevance | Check recommendation text                            | Recommendations match the user's goal (CUT or BULK)                | ✓ Goal-specific advice shown | ✓ PASS |
| 4    | Verify formatting               | Check result display                                 | Result in card format with readable text (not JSON)                | ✓ Card format displayed      | ✓ PASS |

**Manual Verification Checklist:**

- [ ] Recommendations are specific to user's goal
- [ ] Text is formatted as card, not JSON
- [ ] No raw object notation (`[object Object]`)
- [ ] Labels are properly formatted (spaces preserved in readable form)

---

### Test 6: Multiple Client Instances (Same Application)

**Objective:** Verify that two users can use the mobile app simultaneously without data interference.

**Implementation Notes:**

- Client IDs are now stored in **sessionStorage** (tab-specific) instead of localStorage (shared across tabs)
- This ensures each browser tab maintains its own independent client session
- Multiple tabs can open simultaneously with different registrations

| Step | Action                       | Input                                                                 | Expected Outcome                                            | Actual Outcome          | Status |
| ---- | ---------------------------- | --------------------------------------------------------------------- | ----------------------------------------------------------- | ----------------------- | ------ |
| 1    | Open Tab 1                   | New browser tab, navigate to `http://localhost:3000` → Mobile         | Mobile app loaded in Tab 1                                  | ✓ App loaded            | ✓ PASS |
| 2    | Register User A in Tab 1     | Name: "Alice", Weight: 65kg, Height: 172cm, Gender: FEMALE, Goal: CUT | User A registered, client ID stored in Tab 1 sessionStorage | ✓ User A registered     | ✓ PASS |
| 3    | Open Tab 2                   | New browser tab, navigate to `http://localhost:3000` → Mobile         | Mobile app loaded in Tab 2 (independent sessionStorage)     | ✓ App loaded in Tab 2   | ✓ PASS |
| 4    | Register User B in Tab 2     | Name: "Bob", Weight: 75kg, Height: 180cm, Gender: MALE, Goal: BULK    | User B registered with different client ID in Tab 2         | ✓ User B registered     | ✓ PASS |
| 5    | Verify Tab 1 isolation       | Return to Tab 1, click "View Stored Profile"                          | Tab 1 shows only User A's profile (Alice, 65kg, CUT)        | ✓ User A data correct   | ✓ PASS |
| 6    | Verify Tab 2 isolation       | In Tab 2, click "View Stored Profile"                                 | Tab 2 shows only User B's profile (Bob, 75kg, BULK)         | ✓ User B data correct   | ✓ PASS |
| 7    | Verify simultaneous requests | Make BMI request in both tabs at same time                            | Each tab gets correct BMI for its user                      | ✓ Both requests correct | ✓ PASS |

**Manual Verification Checklist:**

- [ ] Each tab has independent sessionStorage (verify in DevTools: Application → Session Storage)
- [ ] Tab 1 and Tab 2 have different `clientId` values in their respective sessionStorage
- [ ] Profile data in each tab matches registered user
- [ ] No data leakage between tabs
- [ ] Both tabs can make simultaneous requests without errors
- [ ] Refreshing Tab 1 still shows Alice's data (sessionStorage persists during session)
- [ ] Closing Tab 1 and reopening doesn't restore Alice's client ID (sessionStorage cleared)

---

## Research Client E2E Tests

### Test 7: Researcher Registration and Access

**Objective:** Verify that a researcher can register and access population analytics.

| Step | Action                 | Input                                                    | Expected Outcome                                     | Actual Outcome        | Status |
| ---- | ---------------------- | -------------------------------------------------------- | ---------------------------------------------------- | --------------------- | ------ |
| 1    | Open research client   | Navigate to `http://localhost:3000` → click "Researcher" | Landing page loads, redirects to research.html       | ✓ Research app loaded | ✓ PASS |
| 2    | Fill registration form | Name: "Dr. Smith", Email: "smith@research.edu"           | Form accepts researcher credentials                  | ✓ Form filled         | ✓ PASS |
| 3    | Submit registration    | Click "Register" button                                  | Response shows client ID (format: `research-xyz789`) | ✓ Client ID generated | ✓ PASS |
| 4    | Verify access          | Researcher can view research dashboard                   | Dashboard loads without errors                       | ✓ Dashboard loaded    | ✓ PASS |

**Manual Verification Checklist:**

- [ ] Client ID format is valid (starts with `research-`)
- [ ] No authentication errors
- [ ] Research dashboard visible
- [ ] localStorage contains researcher client ID

---

### Test 8: Demographics and Population Health API

**Objective:** Verify that researcher can retrieve and visualize both available research API endpoints.

**Implementation Notes:**

- Researcher client has two primary API endpoints: Demographics and Population Health
- Demographics displays gender/goal breakdowns as bar charts
- Population Health displays overall population health metrics

#### Test 8a: Demographics Breakdown

| Step | Action                       | Input                        | Expected Outcome                                                        | Actual Outcome         | Status |
| ---- | ---------------------------- | ---------------------------- | ----------------------------------------------------------------------- | ---------------------- | ------ |
| 1    | Access demographics          | Click "Demographics" button  | Demographics data retrieved from backend                                | ✓ Data retrieved       | ✓ PASS |
| 2    | Verify bar chart display     | Check rendered visualization | Demographics display as horizontal bar chart (not JSON)                 | ✓ Bar chart rendered   | ✓ PASS |
| 3    | Verify data breakdown        | Examine chart segments       | Chart shows gender breakdown (MALE, FEMALE) with counts and percentages | ✓ Breakdown correct    | ✓ PASS |
| 4    | Verify all-caps preservation | Check label formatting       | Labels show "MALE" and "FEMALE" (not "M A L E", "F E M A L E")          | ✓ Labels correct       | ✓ PASS |
| 5    | Verify color coding          | Check visual elements        | Different demographic categories have different colors in legend        | ✓ Color coding visible | ✓ PASS |

**Manual Verification Checklist:**

- [ ] Bar chart renders without JavaScript errors
- [ ] All demographic categories visible
- [ ] Percentages sum to ~100%
- [ ] Labels are readable (no extra spaces)
- [ ] Color legend matches bar segments

#### Test 8b: Population Health

| Step | Action                   | Input                            | Expected Outcome                                    | Actual Outcome        | Status |
| ---- | ------------------------ | -------------------------------- | --------------------------------------------------- | --------------------- | ------ |
| 1    | Access population health | Click "Population Health" button | Population health data retrieved from backend       | ✓ Data retrieved      | ✓ PASS |
| 2    | Verify data display      | Check rendered output            | Health data displayed as formatted output (no JSON) | ✓ Formatted output    | ✓ PASS |
| 3    | Verify data completeness | Examine results                  | All population metrics displayed                    | ✓ Complete data shown | ✓ PASS |
| 4    | Verify number formatting | Check numerical values           | Numbers display with max 2 decimal places           | ✓ Formatted correctly | ✓ PASS |

**Manual Verification Checklist:**

- [ ] All population health metrics rendered
- [ ] Numbers properly formatted to max 2 decimal places
- [ ] No JSON syntax visible
- [ ] No `[object Object]` errors in display
- [ ] Data is readable and organized

---

### Test 9: Multiple Researcher Instances

**Objective:** Verify that two researchers can access analytics simultaneously without data interference.

**Implementation Notes:**

- Client IDs are now stored in **sessionStorage** (tab-specific) instead of localStorage (shared across tabs)
- This ensures each browser tab maintains its own independent client session
- Researchers see their profile after registration, then can access analytics endpoints

| Step | Action                        | Input                                                             | Expected Outcome                                                             | Actual Outcome        | Status |
| ---- | ----------------------------- | ----------------------------------------------------------------- | ---------------------------------------------------------------------------- | --------------------- | ------ |
| 1    | Open Tab 1                    | New browser tab, navigate to `http://localhost:3000` → Researcher | Research app in Tab 1                                                        | ✓ App loaded          | ✓ PASS |
| 2    | Register Researcher A         | Name: "Dr. Alice", Email: "alice@research.com"                    | Researcher A registered with client ID `research-id` in Tab 1 sessionStorage | ✓ Registered          | ✓ PASS |
| 3    | Verify Tab 1 profile          | Check researcher profile display                                  | Tab 1 shows researcher profile with client ID `research-id`                  | ✓ Profile shown       | ✓ PASS |
| 4    | Open Tab 2                    | New browser tab, navigate to `http://localhost:3000` → Researcher | Research app in Tab 2 (independent sessionStorage)                           | ✓ App loaded          | ✓ PASS |
| 5    | Register Researcher B         | Name: "Dr. Bob", Email: "bob@research.com"                        | Researcher B registered with client ID `research-id` in Tab 2 sessionStorage | ✓ Registered          | ✓ PASS |
| 6    | Verify Tab 2 profile          | Check researcher profile display                                  | Tab 2 shows researcher profile with client ID `research-id`                  | ✓ Profile shown       | ✓ PASS |
| 7    | Request demographics in Tab 1 | Click "Demographics" button in Tab 1                              | Tab 1 displays aggregate population demographics (same for all researchers)  | ✓ Demographics loaded | ✓ PASS |
| 8    | Request demographics in Tab 2 | Click "Demographics" button in Tab 2                              | Tab 2 displays same aggregate population demographics as Tab 1               | ✓ Demographics loaded | ✓ PASS |
| 9    | Verify data consistency       | Compare demographics displayed in Tab 1 and Tab 2                 | Both tabs show identical demographics data (cohort summary, gender, goal)    | ✓ Data matches        | ✓ PASS |
| 10   | Verify simultaneous requests  | Make simultaneous Demographics requests in both tabs              | Both requests complete without errors                                        | ✓ Both succeed        | ✓ PASS |

**Manual Verification Checklist:**

- [ ] Each tab has independent sessionStorage (verify in DevTools: Application → Session Storage)
- [ ] Tab 1 and Tab 2 have different `clientId` values in their respective sessionStorage
- [ ] Each tab shows its own researcher profile after registration
- [ ] Both tabs display identical demographics data (aggregate population statistics)
- [ ] Demographics includes: Cohort Summary, By Gender breakdown, By Goal breakdown
- [ ] All-caps labels preserved (MALE, FEMALE, CUT, BULK)
- [ ] Percentages sum to ~100% for each breakdown category
- [ ] Both tabs can make concurrent API requests
- [ ] No errors in browser console
- [ ] Refreshing Tab 1 preserves researcher A's session (sessionStorage persists)
- [ ] Closing Tab 1 and reopening doesn't restore researcher A's client ID

---

## Cross-Client E2E Test

### Test 10: Mobile User + Researcher Data Consistency

**Objective:** Verify that researcher analytics include data from mobile users in the aggregate population demographics.

| Step | Action                            | Input                                                                                  | Expected Outcome                                                         | Actual Outcome           | Status |
| ---- | --------------------------------- | -------------------------------------------------------------------------------------- | ------------------------------------------------------------------------ | ------------------------ | ------ |
| 1    | Register mobile user              | Mobile Tab: Register "Test User", Weight: 70kg, Height: 175cm, Goal: CUT, Gender: MALE | Mobile user created with profile data                                    | ✓ Mobile user registered | ✓ PASS |
| 2    | Configure mobile plan             | Mobile Tab: Set goal plan (Target: 5kg, Duration: 8 weeks, Frequency: 4/week)          | Goal plan saved to backend                                               | ✓ Plan configured        | ✓ PASS |
| 3    | Access demographics as researcher | Research Tab: Register researcher, click "Demographics" button                         | Demographics display aggregate population data including the mobile user | ✓ Demographics loaded    | ✓ PASS |
| 4    | Verify user in cohort             | Check Cohort Summary (Sample Size, Avg Height, Avg Weight, Avg Age)                    | Cohort statistics reflect all registered users including new mobile user | ✓ Cohort updated         | ✓ PASS |
| 5    | Verify gender breakdown           | Check "By Gender" breakdown in demographics                                            | Gender breakdown includes MALE count from mobile user                    | ✓ MALE visible           | ✓ PASS |
| 6    | Verify goal breakdown             | Check "By Goal" breakdown in demographics                                              | Goal breakdown includes CUT count from mobile user                       | ✓ CUT visible            | ✓ PASS |

**Manual Verification Checklist:**

- [ ] Mobile user data persisted to backend
- [ ] Researcher Demographics endpoint returns aggregate population data
- [ ] Cohort Summary shows Sample Size includes the new mobile user
- [ ] Gender breakdown reflects the mobile user's registered gender
- [ ] Goal breakdown shows CUT count includes the mobile user's goal
- [ ] No API errors
- [ ] Data consistency between mobile client registration and research analytics
- [ ] All-caps labels preserved (MALE, FEMALE, CUT, BULK)
- [ ] Percentages are correctly calculated

---

## API Integration Tests

### Test 11: API Error Responses

**Objective:** Verify that API error responses are handled gracefully by the client.

#### Test 11a: Request Without Client ID

| Step | Action                      | Input                                                                                                                                                                                                                                                                                    | Expected Outcome                       | Actual Outcome   | Status |
| ---- | --------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------- | ---------------- | ------ |
| 1    | Open browser DevTools       | Press `F12` or right-click → **Inspect** → Go to **Console** tab                                                                                                                                                                                                                         | Console tab opens                      | ✓ Console open   | ✓ PASS |
| 2    | Register a user (if needed) | Navigate to `http://localhost:3000` → Mobile → Register with any valid credentials, or use existing client ID from sessionStorage                                                                                                                                                        | User registered or client ID available | ✓ Setup complete | ✓ PASS |
| 3    | Send request without header | Paste in Console: `fetch('http://localhost:8080/api/user/profile', { method: 'GET', headers: { 'Content-Type': 'application/json' } }).then(r => { console.log('Status:', r.status); return r.json(); }).then(d => console.log('Response:', d)).catch(e => console.error('Error:', e));` | Request executes in console            | ✓ Request sent   | ✓ PASS |
| 4    | Verify 400 response         | Check console output for `Status: 400` and response message about missing X-Client-ID header                                                                                                                                                                                             | Console shows `Status: 400`            | ✓ 400 received   | ✓ PASS |
| 5    | Verify error message        | Check response includes error details (e.g., "Missing required header: X-Client-ID" or similar)                                                                                                                                                                                          | Error message present in response      | ✓ Error msg OK   | ✓ PASS |

**Manual Verification Checklist for Test 11a:**

- [ ] Console command executes without syntax errors
- [ ] Response status is exactly `400`
- [ ] Response body contains meaningful error message about missing header
- [ ] No unhandled JavaScript exceptions thrown
- [ ] Error message is informative (not generic "Bad Request")

---

#### Test 11b: Request With Invalid Client ID

| Step | Action                    | Input                                                                                                                                                                                                                                                                                                                           | Expected Outcome                                      | Actual Outcome     | Status |
| ---- | ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------- | ------------------ | ------ |
| 1    | Open browser DevTools     | Press `F12` or right-click → **Inspect** → Go to **Console** tab                                                                                                                                                                                                                                                                | Console tab opens                                     | ✓ Console open     | ✓ PASS |
| 2    | Send request with bad ID  | Paste in Console: `fetch('http://localhost:8080/api/user/profile', { method: 'GET', headers: { 'Content-Type': 'application/json', 'X-Client-ID': 'invalid-format-12345' } }).then(r => { console.log('Status:', r.status); return r.json(); }).then(d => console.log('Response:', d)).catch(e => console.error('Error:', e));` | Request executes with invalid client ID               | ✓ Request sent     | ✓ PASS |
| 3    | Verify 400 response       | Check console output for `Status: 400` and response message about invalid client ID format                                                                                                                                                                                                                                      | Console shows `Status: 400`                           | ✓ 400 received     | ✓ PASS |
| 4    | Verify validation message | Check response includes validation error (e.g., "Invalid client ID format" or "Client not found")                                                                                                                                                                                                                               | Error message present in response                     | ✓ Validation msg   | ✓ PASS |
| 5    | Verify UI handles error   | Return to the mobile/research app UI. Make any request (e.g., click "Get BMI" or "Demographics"). Check if error is displayed gracefully in UI (not crashing or showing JSON dumps)                                                                                                                                             | UI remains functional, displays error or re-registers | ✓ UI handles error | ✓ PASS |

**Manual Verification Checklist for Test 11b:**

- [ ] Console command executes without syntax errors
- [ ] Response status is `400`
- [ ] Response body contains error message about invalid client ID
- [ ] UI does not crash or show raw JSON errors
- [ ] User can recover (refresh page, re-register, retry)
- [ ] Browser console has no unhandled exceptions

---

#### Alternative Method: Using Postman (Recommended for Repeated Testing)

If you prefer a GUI approach instead of console commands:

1. **Open Postman** (install from [postman.com](https://www.postman.com/downloads/) if needed)
2. **Create a GET request** to `http://localhost:8080/api/user/profile`
3. **Go to Headers tab:**
   - **Test 11a:** Leave headers empty (no `X-Client-ID`)
   - **Test 11b:** Add `X-Client-ID: invalid-format-12345`
4. **Send request** and observe response status and body
5. **Expected:** Status `400` with error message

This method is easier to repeat and document with screenshots.

---

## Summary

### Test Results Overview

| Test Category   | Total Tests | Passed | Failed | Status         |
| --------------- | ----------- | ------ | ------ | -------------- |
| Mobile Client   | 6           | 6      | 0      | ✓ ALL PASS     |
| Research Client | 3           | 3      | 0      | ✓ ALL PASS     |
| Cross-Client    | 1           | 1      | 0      | ✓ ALL PASS     |
| API Integration | 1           | 1      | 0      | ✓ ALL PASS     |
| **TOTAL**       | **11**      | **11** | **0**  | **✓ ALL PASS** |

---

## End-to-End Client/Service Test Matrix

This table provides a comprehensive summary of all tested client actions and their outcomes:

| Test # | Test Name                                         | Client Type     | Action                                               | Expected Outcome                                    | Matched with Expectation | Status                                    | Notes |
| ------ | ------------------------------------------------- | --------------- | ---------------------------------------------------- | --------------------------------------------------- | ------------------------ | ----------------------------------------- | ----- |
| 1      | User Registration and Profile Creation            | Mobile          | Register user with fitness profile data              | Client ID generated, profile stored                 | ✓ YES                    | Profile persists in backend               |
| 2      | Goal Plan Configuration                           | Mobile          | Configure personalized fitness plan                  | Plan saved with user's client ID                    | ✓ YES                    | Plan associated with correct user         |
| 3      | BMI Calculation                                   | Mobile          | Calculate BMI from registered metrics                | BMI displayed with max 2 decimal places in card     | ✓ YES                    | Numbers formatted correctly               |
| 4      | Calorie Recommendations                           | Mobile          | Get calorie targets for goal                         | Calories displayed in card format with breakdown    | ✓ YES                    | All fields displayed properly             |
| 5      | Fitness Recommendations                           | Mobile          | Get goal-specific recommendations                    | Recommendations match user's goal (CUT/BULK)        | ✓ YES                    | Goal-specific advice provided             |
| 6      | Multiple Mobile Client Instances                  | Mobile          | Register two users in different tabs                 | Each tab shows only its own profile data            | ✓ YES                    | sessionStorage provides tab isolation     |
| 7      | Researcher Registration and Access                | Research        | Register researcher, access analytics                | Researcher client ID generated, dashboard loads     | ✓ YES                    | Researcher profile stored                 |
| 8a     | Demographics and Population Health - Demographics | Research        | Display gender/goal breakdown via Demographics API   | Data shown as horizontal bar chart with percentages | ✓ YES                    | All-caps labels preserved (MALE, FEMALE)  |
| 8b     | Demographics and Population Health - Pop Health   | Research        | Display population metrics via Population Health API | Health data displayed as formatted cards            | ✓ YES                    | Numbers properly formatted                |
| 9      | Multiple Researcher Instances                     | Research        | Register two researchers in different tabs           | Each tab shows only its own analytics               | ✓ YES                    | sessionStorage provides tab isolation     |
| 10     | Mobile User + Researcher Data Consistency         | Cross-Client    | Mobile user registers, researcher queries data       | Researcher analytics include mobile user data       | ✓ YES                    | Data flows correctly between client types |
| 11     | API Error Responses                               | API Integration | Request without client ID / invalid ID               | Client displays user-friendly error message         | ✓ YES                    | Error handling works properly             |

---

## How to Re-Run Tests

1. **Ensure services are running:**

   ```powershell
   docker compose up -d --build
   ```

2. **For each test:**

   - Follow steps in the table sequentially
   - Use exact inputs provided
   - Verify all checklist items
   - Record actual outcome

3. **Troubleshooting:**
   - Check browser console for JavaScript errors
   - Verify backend health: `http://localhost:8080/health`
   - Check API response: Open DevTools → Network tab → review failed requests
   - Reset and restart: `docker compose down -v; docker compose up -d --build`
