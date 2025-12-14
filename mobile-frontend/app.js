// API Configuration
const API_CONFIG_KEY = "fitness_api_base_url";
const CLIENT_ID_KEY = "fitness_mobile_client_id";
const DEFAULT_API_BASE_URL = "http://localhost:8080";
const REMOTE_DEFAULT_PORT = "8080";
// Common backend ports to try when auto-detecting
const COMMON_BACKEND_PORTS = [8080, 80, 3000, 5000, 8081, 8443, 443];

// Use sessionStorage for client ID (tab-specific) and localStorage for API URL (shared)
const getStorage = (key) => {
  return key === CLIENT_ID_KEY ? sessionStorage : localStorage;
};

let cachedAutoApiBaseUrl = "";
let autoDetectPromise = null;
let cachedProfile = null;

// Initialize on page load
document.addEventListener("DOMContentLoaded", () => {
  initializeApp();
});

function initializeApp() {
  // Check if using file:// protocol
  if (window.location.protocol === "file:") {
    showStatus(
      "Warning: Using file:// protocol may cause CORS issues. Please use a local web server. See frontend/README.md",
      "error"
    );
  }

  const queryApiUrl = getQueryParamApiUrl();
  if (queryApiUrl) {
    localStorage.setItem(API_CONFIG_KEY, queryApiUrl);
    showStatus(`API endpoint set to ${queryApiUrl}`, "info");
    updateApiEndpointDisplay(queryApiUrl, "query parameter");
  } else {
    const savedUrl = localStorage.getItem(API_CONFIG_KEY);
    if (savedUrl) {
      updateApiEndpointDisplay(savedUrl, "saved configuration");
    } else {
      detectAutoApiBaseUrl()
        .then((url) => updateApiEndpointDisplay(url, "auto-detected"))
        .catch(() => updateApiEndpointDisplay("", "error"));
    }
  }

  // Load saved client ID (from sessionStorage - tab-specific)
  const clientId = sessionStorage.getItem(CLIENT_ID_KEY);
  if (clientId) {
    showAuthenticatedView(clientId);
  } else {
    showRegistrationView();
  }

  // Setup event listeners
  setupEventListeners();
}

function setupEventListeners() {
  // Registration form
  document
    .getElementById("registerForm")
    .addEventListener("submit", handleRegister);

  // Plan form
  document
    .getElementById("planForm")
    .addEventListener("submit", handlePlanSubmit);

  // Action buttons
  document
    .getElementById("loadProfileBtn")
    .addEventListener("click", loadProfile);
  document.getElementById("getBmiBtn").addEventListener("click", getBMI);
  document
    .getElementById("getCaloriesBtn")
    .addEventListener("click", getCalories);
  document
    .getElementById("getRecommendationBtn")
    .addEventListener("click", getRecommendation);
  document
    .getElementById("listProfilesBtn")
    .addEventListener("click", listProfiles);

  // Clear client button
  document
    .getElementById("clearClientBtn")
    .addEventListener("click", clearClient);

  const existingBtn = document.getElementById("useExistingClientBtn");
  if (existingBtn) {
    existingBtn.addEventListener("click", useExistingClient);
  }

  const updateForm = document.getElementById("updateProfileForm");
  if (updateForm) {
    updateForm.addEventListener("submit", handleProfileUpdate);
  }

  document.querySelectorAll(".collapse-toggle").forEach((btn) => {
    btn.addEventListener("click", () => toggleSection(btn));
  });
}

async function getApiBaseUrl() {
  const savedUrl = localStorage.getItem(API_CONFIG_KEY);
  if (savedUrl) {
    updateApiEndpointDisplay(savedUrl, "saved configuration");
    return savedUrl;
  }

  const detected = await detectAutoApiBaseUrl();
  updateApiEndpointDisplay(detected, "auto-detected");
  return detected;
}

function getClientId() {
  return sessionStorage.getItem(CLIENT_ID_KEY);
}

function showRegistrationView() {
  document.getElementById("registrationSection").style.display = "block";
  document.getElementById("authenticatedSection").style.display = "none";
  document.getElementById("clientInfo").style.display = "none";
}

function showAuthenticatedView(clientId) {
  document.getElementById("registrationSection").style.display = "none";
  document.getElementById("authenticatedSection").style.display = "block";
  document.getElementById("clientInfo").style.display = "block";
  document.getElementById("clientIdDisplay").textContent = clientId;
  loadProfile();
}

function clearClient() {
  if (
    confirm(
      "Are you sure you want to clear your client ID and register a new profile?"
    )
  ) {
    sessionStorage.removeItem(CLIENT_ID_KEY);
    cachedProfile = null;
    showRegistrationView();
    showStatus("Client ID cleared. Please register a new profile.", "info");
  }
}

async function useExistingClient() {
  const input = document.getElementById("existingClientId");
  if (!input) {
    return;
  }
  const clientId = input.value.trim();
  if (!clientId) {
    showStatus("Enter a client ID to continue.", "error");
    return;
  }
  sessionStorage.setItem(CLIENT_ID_KEY, clientId);
  try {
    const response = await apiCall("GET", "/api/persons/me", null, true);
    cachedProfile = JSON.parse(response);
    showAuthenticatedView(clientId);
    showStatus("Loaded existing client ID.", "success");
  } catch (error) {
    sessionStorage.removeItem(CLIENT_ID_KEY);
    cachedProfile = null;
    input.focus();
    showStatus("Client ID not found: " + error.message, "error");
    showRegistrationView();
    return;
  } finally {
    input.value = "";
  }
}

async function handleRegister(e) {
  e.preventDefault();

  const formData = {
    name: document.getElementById("name").value.trim(),
    weight: parseFloat(document.getElementById("weight").value),
    height: parseFloat(document.getElementById("height").value),
    birthDate: document.getElementById("birthDate").value,
    gender: document.getElementById("gender").value,
    goal: document.getElementById("goal").value,
  };

  try {
    const response = await apiCall("POST", "/api/persons", formData, false);
    const data = JSON.parse(response);

    if (data.clientId) {
      sessionStorage.setItem(CLIENT_ID_KEY, data.clientId);
      showAuthenticatedView(data.clientId);
      showStatus(
        "Registration successful! Client ID: " + data.clientId,
        "success"
      );
      // Clear form
      document.getElementById("registerForm").reset();
    } else {
      showStatus("Registration failed: No client ID returned", "error");
    }
  } catch (error) {
    showStatus("Registration failed: " + error.message, "error");
  }
}

async function handlePlanSubmit(e) {
  e.preventDefault();

  const formData = {
    targetChangeKg: parseFloat(document.getElementById("targetChangeKg").value),
    durationWeeks: parseInt(document.getElementById("durationWeeks").value),
    trainingFrequencyPerWeek: parseInt(
      document.getElementById("trainingFrequencyPerWeek").value
    ),
    planStrategy: document.getElementById("planStrategy").value,
  };

  try {
    const response = await apiCall("POST", "/api/persons/plan", formData, true);
    let data = null;
    try {
      data = JSON.parse(response);
    } catch (_) {
      data = null;
    }
    displayResults("Plan Configuration", data ?? response);
    if (data) {
      cachedProfile = data;
      displayProfile(data);
      populateUpdateForm(data);
    }
    showStatus("Plan saved successfully!", "success");
  } catch (error) {
    showStatus("Failed to save plan: " + error.message, "error");
  }
}

async function loadProfile() {
  try {
    const response = await apiCall("GET", "/api/persons/me", null, true);
    const data = JSON.parse(response);
    cachedProfile = data;
    displayProfile(data);
    populateUpdateForm(data);
  } catch (error) {
    showStatus("Failed to load profile: " + error.message, "error");
    document.getElementById("profileContent").innerHTML =
      '<p class="placeholder">Failed to load profile. Make sure you are registered.</p>';
  }
}

async function getBMI() {
  try {
    const response = await apiCall("GET", "/api/persons/bmi", null, true);
    displayResults("BMI Calculation", response);
  } catch (error) {
    showStatus("Failed to get BMI: " + error.message, "error");
  }
}

async function getCalories() {
  try {
    const response = await apiCall("GET", "/api/persons/calories", null, true);
    displayResults("Calorie Calculation", response);
  } catch (error) {
    showStatus("Failed to get calories: " + error.message, "error");
  }
}

async function getRecommendation() {
  try {
    // First, fetch the profile to check if all goal plan fields are filled
    // Use cached profile if available, otherwise fetch it
    let profileData = cachedProfile;
    if (!profileData) {
      const profileResponse = await apiCall("GET", "/api/persons/me", null, true);
      profileData = JSON.parse(profileResponse);
      cachedProfile = profileData;
    }
    
    // Validate that all goal plan fields are present
    const missingFields = [];
    if (profileData.targetChangeKg === null || profileData.targetChangeKg === undefined) {
      missingFields.push("Target Weight (kg)");
    }
    if (profileData.targetDurationWeeks === null || profileData.targetDurationWeeks === undefined) {
      missingFields.push("Duration (weeks)");
    }
    if (profileData.trainingFrequencyPerWeek === null || profileData.trainingFrequencyPerWeek === undefined) {
      missingFields.push("Training Frequency (per week)");
    }
    if (!profileData.planStrategy) {
      missingFields.push("Plan Strategy");
    }
    
    if (missingFields.length > 0) {
      const fieldsList = missingFields.join(", ");
      showStatus(
        `Cannot get recommendation. Please fill in all fields in "Configure Goal Plan": ${fieldsList}`,
        "error"
      );
      return;
    }
    
    // All fields are present, proceed with recommendation
    const response = await apiCall(
      "GET",
      "/api/persons/recommendation",
      null,
      true
    );
    displayResults("Recommendation", response);
  } catch (error) {
    showStatus("Failed to get recommendation: " + error.message, "error");
  }
}

async function listProfiles() {
  try {
    const response = await apiCall("GET", "/api/persons/me", null, true);
    displayResults("Stored Profile", response);
    const data = JSON.parse(response);
    cachedProfile = data;
    populateUpdateForm(data);
  } catch (error) {
    showStatus(
      "Failed to load stored profile from /api/persons/me: " + error.message,
      "error"
    );
  }
}

function displayProfile(data) {
  const profileContent = document.getElementById("profileContent");
  let html = '<div class="profile-display">';

  if (data.name)
    html += `<div class="profile-item"><strong>Name</strong><span>${escapeHtml(
      data.name
    )}</span></div>`;
  if (data.weight)
    html += `<div class="profile-item"><strong>Weight</strong><span>${data.weight} kg</span></div>`;
  if (data.height)
    html += `<div class="profile-item"><strong>Height</strong><span>${data.height} cm</span></div>`;
  if (data.birthDate)
    html += `<div class="profile-item"><strong>Birth Date</strong><span>${data.birthDate}</span></div>`;
  if (data.gender)
    html += `<div class="profile-item"><strong>Gender</strong><span>${data.gender}</span></div>`;
  if (data.goal)
    html += `<div class="profile-item"><strong>Goal</strong><span>${data.goal}</span></div>`;
  if (data.targetChangeKg) {
    const targetWeight = Number(data.targetChangeKg);
    const currentWeight = data.weight != null ? Number(data.weight) : null;
    let displayValue = `${targetWeight} kg`;
    if (!Number.isNaN(targetWeight) && currentWeight !== null && !Number.isNaN(currentWeight)) {
      const delta = targetWeight - currentWeight;
      if (delta !== 0) {
        const sign = delta > 0 ? "+" : "-";
        const magnitude = Math.abs(delta);
        displayValue += ` (${sign}${magnitude} kg)`;
      }
    }
    html += `<div class="profile-item"><strong>Target Weight</strong><span>${displayValue}</span></div>`;
  }
  if (data.targetDurationWeeks)
    html += `<div class="profile-item"><strong>Duration</strong><span>${data.targetDurationWeeks} weeks</span></div>`;
  if (data.trainingFrequencyPerWeek)
    html += `<div class="profile-item"><strong>Training Frequency</strong><span>${data.trainingFrequencyPerWeek} per week</span></div>`;
  if (data.planStrategy)
    html += `<div class="profile-item"><strong>Plan Strategy</strong><span>${data.planStrategy}</span></div>`;

  html += "</div>";
  profileContent.innerHTML = html;
}

function displayResults(title, response) {
  const resultsContent = document.getElementById("resultsContent");
  let html = `<h4 style="margin-bottom: 15px; color: var(--text-primary); border-bottom: 2px solid var(--primary-color); padding-bottom: 10px;">${escapeHtml(
    title
  )}</h4>`;

  try {
    const data = JSON.parse(response);
    if (title === "Recommendation" && data && !Array.isArray(data)) {
      html += renderRecommendationSummary(data);
      // Display warning if plan alignment is 0
      if (data.planAlignmentWarning) {
        html += '<div class="plan-alignment-warning" style="background-color: rgba(255, 193, 7, 0.2); border: 2px solid rgba(255, 193, 7, 0.8); border-radius: 10px; padding: 15px; margin: 15px 0; color: var(--text-primary);">';
        html += '<strong style="font-size: 16px; display: block; margin-bottom: 8px;">⚠️ Plan Alignment Warning:</strong>';
        html += '<span style="font-size: 14px; line-height: 1.5;">' + escapeHtml(data.planAlignmentWarning) + '</span>';
        html += '</div>';
      }
    }
    html += formatResultsAsCards(data);
  } catch (e) {
    html +=
      '<div class="result-item"><p>' + escapeHtml(response) + "</p></div>";
  }

  resultsContent.innerHTML = html;
}

function formatResultsAsCards(data) {
  if (Array.isArray(data)) {
    // If array, display each item as a card
    return data.map((item) => formatSingleResult(item)).join("");
  } else {
    // Single object
    return formatSingleResult(data);
  }
}

function formatSingleResult(item) {
  if (typeof item !== "object" || item === null) {
    return `<div class="result-item"><p>${escapeHtml(String(item))}</p></div>`;
  }

  let html = '<div class="result-item">';

  // Create key-value pairs with styled display
  for (const [key, value] of Object.entries(item)) {
    const displayKey = formatKeyName(key);
    const displayValue = formatValue(value);
    html += `<div class="result-field"><span class="result-label">${escapeHtml(
      displayKey
    )}:</span><span class="result-value">${displayValue}</span></div>`;
  }

  html += "</div>";
  return html;
}

function formatKeyName(key) {
  // Only add spaces before capitals that follow lowercase letters (camelCase)
  return key
    .replace(/([a-z])([A-Z])/g, "$1 $2") // camelCase to spaces
    .replace(/^./, (str) => str.toUpperCase()) // capitalize first
    .trim();
}

function formatValue(value) {
  if (value === null || value === undefined) {
    return '<span style="color: var(--text-secondary); font-style: italic;">—</span>';
  }
  if (typeof value === "boolean") {
    return `<strong style="color: ${
      value ? "var(--success-color)" : "var(--danger-color)"
    }">${value ? "Yes" : "No"}</strong>`;
  }
  if (typeof value === "number") {
    // Format number to max 2 decimal places, removing trailing zeros
    const formatted = Math.round(value * 100) / 100;
    return `<strong style="color: var(--primary-color);">${formatted}</strong>`;
  }
  if (typeof value === "object") {
    return `<code style="background: var(--background); padding: 2px 6px; border-radius: 3px;">${escapeHtml(
      JSON.stringify(value)
    )}</code>`;
  }
  return escapeHtml(String(value));
}

function renderRecommendationSummary(data) {
  const metricDefinitions = [
    {
      label: "BMI",
      show: () => data.bmi !== null && data.bmi !== undefined,
      value: () => summarizeMetric(data.bmi, data.bmiCategory ? ` (${data.bmiCategory})` : ""),
      description: () => "Shows how your weight sits for your height (under/normal/over/obese)."
    },
    {
      label: "Health Index",
      show: () => true,
      value: () => summarizeMetric(data.healthIndex),
      description: () => "0-100 indicator of overall health habits (100 = strongest)."
    },
    {
      label: "Plan Alignment",
      show: () =>
        data.planAlignmentIndex !== null
        && data.planAlignmentIndex !== undefined
        && !Number.isNaN(data.planAlignmentIndex),
      value: () => summarizeMetric(data.planAlignmentIndex),
      description: () => {
        if (data.planAlignmentIndex === 0) {
          return "0 means your plan is unrealistic. This could be due to: goal contradiction, extremely aggressive targets, unrealistic timeline, insufficient training frequency, or mismatched strategy. Please review and adjust your plan configuration.";
        }
        return "0-100 gauge of how realistic your goal/pace/training combo is.";
      }
    },
    {
      label: "Trajectory Score",
      show: () => true,
      value: () => summarizeMetric(data.overallScore),
      description: () => "0-100 overall outlook for where your health and plan are heading."
    },
    {
      label: "Percentile",
      show: () => data.percentile !== null && data.percentile !== undefined,
      value: () => summarizeMetric(data.percentile, "%"),
      description: () => {
        if (data.percentile !== null && data.percentile !== undefined) {
          const percentile = data.percentile;
          if (percentile >= 90) {
            return "Excellent health condition! You rank higher than 90% of users, indicating very strong overall health habits and plan alignment.";
          } else if (percentile >= 75) {
            return "Very good health condition. You rank higher than 75% of users, showing strong health habits and a well-aligned fitness plan.";
          } else if (percentile >= 50) {
            return "Good health condition. You rank higher than 50% of users, indicating above-average health habits and plan effectiveness.";
          } else if (percentile >= 25) {
            return "Moderate health condition. You rank higher than 25% of users. Consider reviewing your plan to improve your health trajectory.";
          } else {
            return "Health condition needs attention. You rank in the bottom 25% of users. Please review your goals and plan to improve your health trajectory.";
          }
        }
        return "Comparison to other users. Higher percentile indicates better overall health condition and plan effectiveness.";
      }
    }
  ];

  let html = '<div class="result-item recommendation-summary">';
  html += `<p class="recommendation-message">${escapeHtml(data.message || 'No recommendation available')}</p>`;
  html += '<div class="recommendation-metrics" style="display:flex; flex-direction:column; gap:10px;">';
  metricDefinitions.filter((metric) => metric.show()).forEach((metric) => {
    html += `<div class="recommendation-metric" style="background: rgba(255,255,255,0.08); border-radius: 10px; padding: 10px;"><div style="display:flex; justify-content:space-between; align-items:flex-end; margin-bottom:4px;"><span class="metric-label" style="font-size: 12px; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.05em;">${escapeHtml(metric.label)}</span><span class="metric-value" style="font-size: 20px; font-weight: 600; color: var(--primary-color-light);">${metric.value()}</span></div><span class="metric-description" style="font-size: 13px; color: var(--text-secondary);">${escapeHtml(metric.description())}</span></div>`;
  });
  html += '</div>';
  if (data.cohortWarning) {
    html += `<div class="recommendation-warning">${escapeHtml(data.cohortWarning)}</div>`;
  }
  html += '</div>';
  return html;
}

function summarizeMetric(rawValue, suffix) {
  if (rawValue === null || rawValue === undefined || Number.isNaN(rawValue)) {
    return '<span class="metric-missing">—</span>';
  }
  const num = Math.round(Number(rawValue) * 10) / 10;
  const text = suffix && suffix !== true ? `${num}${suffix === '%' ? suffix : ' ' + suffix}` : `${num}`;
  return `<strong>${escapeHtml(text)}</strong>`;
}

function populateUpdateForm(data) {
  const setValue = (id, value) => {
    const el = document.getElementById(id);
    if (el) {
      el.value = value ?? "";
    }
  };
  setValue("updateName", data.name || "");
  setValue("updateWeight", data.weight ?? "");
  setValue("updateHeight", data.height ?? "");
  setValue("updateBirthDate", data.birthDate || "");
  setValue("updateGender", data.gender || "");
  setValue("updateGoal", data.goal || "");
  setValue("updateTargetChangeKg", data.targetChangeKg ?? "");
  setValue("updateDurationWeeks", data.targetDurationWeeks ?? "");
  setValue("updateTrainingFrequency", data.trainingFrequencyPerWeek ?? "");
  setValue("updatePlanStrategy", data.planStrategy || "");
}

async function handleProfileUpdate(e) {
  e.preventDefault();
  try {
    const payload = buildUpdatePayload();
    const response = await apiCall("PUT", "/api/persons/me", payload, true);
    displayResults("Profile Updated", response);
    showStatus("Profile updated successfully!", "success");
    await loadProfile();
  } catch (error) {
    showStatus("Failed to update profile: " + error.message, "error");
  }
}

function buildUpdatePayload() {
  return {
    name: document.getElementById("updateName").value.trim(),
    weight: parseFloat(document.getElementById("updateWeight").value),
    height: parseFloat(document.getElementById("updateHeight").value),
    birthDate: document.getElementById("updateBirthDate").value,
    gender: document.getElementById("updateGender").value,
    goal: document.getElementById("updateGoal").value,
    targetChangeKg: readOptionalNumber(
      "updateTargetChangeKg",
      cachedProfile?.targetChangeKg,
      parseFloat
    ),
    targetDurationWeeks: readOptionalNumber(
      "updateDurationWeeks",
      cachedProfile?.targetDurationWeeks,
      parseInt
    ),
    trainingFrequencyPerWeek: readOptionalNumber(
      "updateTrainingFrequency",
      cachedProfile?.trainingFrequencyPerWeek,
      parseInt
    ),
    planStrategy: readOptionalSelect(
      "updatePlanStrategy",
      cachedProfile?.planStrategy
    ),
  };
}

function readOptionalNumber(id, fallback, parser) {
  const el = document.getElementById(id);
  if (!el) {
    return fallback ?? null;
  }
  const raw = el.value;
  if (raw === "" || raw === null) {
    return fallback ?? null;
  }
  const parsed = parser(raw);
  return Number.isNaN(parsed) ? fallback ?? null : parsed;
}

function readOptionalSelect(id, fallback) {
  const el = document.getElementById(id);
  if (!el) {
    return fallback ?? null;
  }
  const raw = el.value;
  return raw ? raw : fallback ?? null;
}

function toggleSection(button) {
  const targetId = button.dataset.target;
  if (!targetId) {
    return;
  }
  const container = document.getElementById(targetId);
  if (!container) {
    return;
  }
  const isOpen = container.dataset.open === "true";
  container.dataset.open = isOpen ? "false" : "true";
  button.textContent = isOpen ? "Edit" : "Hide";
}

async function apiCall(method, path, body, requireAuth) {
  const baseUrl = await getApiBaseUrl();
  const url = baseUrl + path;

  // Check if we're using file:// protocol (which causes CORS issues)
  if (window.location.protocol === "file:") {
    throw new Error(
      "Cannot use file:// protocol. Please use a local web server. See frontend/README.md for instructions."
    );
  }

  const options = {
    method: method,
    headers: {},
  };

  if (requireAuth) {
    const clientId = getClientId();
    if (!clientId) {
      throw new Error("Client ID not found. Please register first.");
    }
    options.headers["X-Client-ID"] = clientId;
  }

  if (body) {
    options.headers["Content-Type"] = "application/json";
    options.body = JSON.stringify(body);
  }

  try {
    const response = await fetch(url, options);
    const text = await response.text();

    if (!response.ok) {
      let errorMsg = `HTTP ${response.status}`;
      try {
        const errorData = JSON.parse(text);
        if (errorData.message) {
          errorMsg = errorData.message;
        } else if (typeof errorData === "string") {
          errorMsg = errorData;
        }
      } catch (e) {
        if (text) errorMsg = text;
      }
      throw new Error(errorMsg);
    }

    return text;
  } catch (error) {
    if (
      error.message.includes("Failed to fetch") ||
      error.message.includes("NetworkError")
    ) {
      const diagnosticMsg =
        `Cannot connect to API via ${method} ${url}. ` +
        `Please check: 1) Backend is running (try: mvn spring-boot:run), ` +
        `2) Using a web server (not file://), ` +
        `3) API URL is correct. ` +
        `See frontend/README.md for setup instructions.`;
      throw new Error(diagnosticMsg);
    }
    throw error;
  }
}

function showStatus(message, type = "info") {
  const statusEl = document.getElementById("statusMessage");
  statusEl.textContent = message;
  statusEl.className = `status-message ${type}`;
  statusEl.style.display = "block";

  setTimeout(() => {
    statusEl.style.display = "none";
  }, 5000);
}

function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}

function getQueryParamApiUrl() {
  try {
    const params = new URLSearchParams(window.location.search);
    const queryUrl = params.get("apiBaseUrl") || params.get("api");
    return normalizeBaseUrl(queryUrl);
  } catch (error) {
    return "";
  }
}

function normalizeBaseUrl(url) {
  if (!url || typeof url !== "string") {
    return "";
  }
  return url.trim().replace(/\/+$/, "");
}

function isLocalhostHost(hostname) {
  return (
    hostname === "localhost" || hostname === "127.0.0.1" || hostname === "::1"
  );
}

async function detectAutoApiBaseUrl() {
  if (cachedAutoApiBaseUrl) {
    return cachedAutoApiBaseUrl;
  }

  if (autoDetectPromise) {
    return autoDetectPromise;
  }

  autoDetectPromise = (async () => {
    const globalOverride = readGlobalApiBaseUrlOverride();
    if (globalOverride) {
      return globalOverride;
    }

    const queryOverride = getQueryParamApiUrl();
    if (queryOverride) {
      return queryOverride;
    }

    if (!window.location.protocol.startsWith("http")) {
      return DEFAULT_API_BASE_URL;
    }

    if (isLocalhostHost(window.location.hostname)) {
      return DEFAULT_API_BASE_URL;
    }

    // For remote deployments (e.g., GCP VM), try to detect backend port dynamically
    const remotePortHint = getRemotePortHint();
    let portsToTry = COMMON_BACKEND_PORTS;
    
    // If a port hint is provided, prioritize it
    if (remotePortHint) {
      const hintPort = parseInt(remotePortHint);
      if (!isNaN(hintPort) && hintPort > 0 && hintPort < 65536) {
        portsToTry = [hintPort, ...COMMON_BACKEND_PORTS.filter(p => p !== hintPort)];
      }
    }
    
    // Try ports in parallel for faster detection
    const healthChecks = portsToTry.map(async (port) => {
      const candidateUrl = `${window.location.protocol}//${window.location.hostname}:${port}`;
      const isHealthy = await backendRespondsToHealth(candidateUrl, 1500);
      return { port, url: candidateUrl, isHealthy };
    });
    
    const results = await Promise.all(healthChecks);
    const healthyBackend = results.find(r => r.isHealthy);
    
    if (healthyBackend) {
      return healthyBackend.url;
    }
    
    // If no port responded, use the hint port or default to 8080
    // This ensures GCP deployments have a fallback even if backend isn't ready yet
    const fallbackPort = remotePortHint || REMOTE_DEFAULT_PORT;
    return `${window.location.protocol}//${window.location.hostname}:${fallbackPort}`;
  })();

  try {
    const detected = await autoDetectPromise;
    cachedAutoApiBaseUrl = detected || DEFAULT_API_BASE_URL;
    try {
      localStorage.setItem(API_CONFIG_KEY, cachedAutoApiBaseUrl);
    } catch (error) {
      // Ignore localStorage failures (e.g., restricted environments)
    }
    return cachedAutoApiBaseUrl;
  } finally {
    autoDetectPromise = null;
  }
}

function readGlobalApiBaseUrlOverride() {
  if (window.__FITNESS_API_BASE_URL__) {
    return normalizeBaseUrl(window.__FITNESS_API_BASE_URL__);
  }

  const metaTag = document.querySelector('meta[name="fitness-api-base-url"]');
  if (metaTag && metaTag.content) {
    return normalizeBaseUrl(metaTag.content);
  }

  if (
    document.body &&
    document.body.dataset &&
    document.body.dataset.apiBaseUrl
  ) {
    return normalizeBaseUrl(document.body.dataset.apiBaseUrl);
  }

  const root = document.documentElement;
  if (root && root.dataset && root.dataset.apiBaseUrl) {
    return normalizeBaseUrl(root.dataset.apiBaseUrl);
  }

  return "";
}

function getRemotePortHint() {
  if (window.__FITNESS_API_PORT__) {
    return `${window.__FITNESS_API_PORT__}`.trim();
  }

  const metaTag = document.querySelector('meta[name="fitness-api-port"]');
  if (metaTag && metaTag.content) {
    return metaTag.content.trim();
  }

  if (
    document.body &&
    document.body.dataset &&
    document.body.dataset.fitnessApiPort
  ) {
    return document.body.dataset.fitnessApiPort.trim();
  }

  const root = document.documentElement;
  if (root && root.dataset && root.dataset.fitnessApiPort) {
    return root.dataset.fitnessApiPort.trim();
  }

  return REMOTE_DEFAULT_PORT;
}

async function backendRespondsToHealth(baseUrl, timeoutMs = 2000) {
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);
    
    const response = await fetch(baseUrl + "/health", {
      method: "GET",
      cache: "no-store",
      signal: controller.signal,
    });
    
    clearTimeout(timeoutId);
    
    if (!response.ok) {
      return false;
    }
    const contentType = response.headers.get("content-type") || "";
    return !contentType.includes("text/html");
  } catch (error) {
    return false;
  }
}

function updateApiEndpointDisplay(baseUrl, source = "") {
  const banner = document.getElementById("apiEndpointBanner");
  const valueEl = document.getElementById("apiEndpointDisplay");
  if (!valueEl) {
    return;
  }

  const readableSource = formatSourceLabel(source);
  const suffix = baseUrl && readableSource ? ` (${readableSource})` : "";
  if (baseUrl) {
    valueEl.textContent = baseUrl + suffix;
    valueEl.title = readableSource
      ? `API base set via ${readableSource}`
      : "API base URL";
    if (banner) {
      banner.dataset.state = "ok";
    }
  } else {
    valueEl.textContent =
      readableSource === "error" ? "Detection failed" : "Not detected";
    valueEl.title = "Backend URL not detected yet";
    if (banner) {
      banner.dataset.state = "error";
    }
  }
}

function formatSourceLabel(source) {
  switch (source) {
    case "query parameter":
      return "query parameter";
    case "saved configuration":
      return "saved configuration";
    case "auto-detected":
      return "auto-detected";
    case "error":
      return "error";
    default:
      return source || "";
  }
}
