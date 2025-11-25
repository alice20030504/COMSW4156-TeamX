// API Configuration
const API_CONFIG_KEY = "fitness_api_base_url";
const CLIENT_ID_KEY = "fitness_research_client_id";
const DEFAULT_API_BASE_URL = "http://localhost:8080";
const REMOTE_DEFAULT_PORT = "8080";

// Use sessionStorage for client ID (tab-specific) and localStorage for API URL (shared)
const getStorage = (key) => {
  return key === CLIENT_ID_KEY ? sessionStorage : localStorage;
};

let cachedAutoApiBaseUrl = "";
let autoDetectPromise = null;

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

  // Action buttons
  document
    .getElementById("getDemographicsBtn")
    .addEventListener("click", getDemographics);
  document
    .getElementById("getPopulationHealthBtn")
    .addEventListener("click", getPopulationHealth);

  // Clear client button
  document
    .getElementById("clearClientBtn")
    .addEventListener("click", clearClient);
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
}

function clearClient() {
  if (
    confirm(
      "Are you sure you want to clear your client ID and register a new profile?"
    )
  ) {
    sessionStorage.removeItem(CLIENT_ID_KEY);
    showRegistrationView();
    showStatus("Client ID cleared. Please register a new profile.", "info");
  }
}

async function handleRegister(e) {
  e.preventDefault();

  const formData = {
    name: document.getElementById("name").value.trim(),
    email: document.getElementById("email").value.trim().toLowerCase(),
  };

  try {
    const response = await apiCall("POST", "/api/research", formData, false);
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
      // Display profile
      displayResearcherProfile(data);
    } else {
      showStatus("Registration failed: No client ID returned", "error");
    }
  } catch (error) {
    showStatus("Registration failed: " + error.message, "error");
  }
}

async function getDemographics() {
  try {
    const response = await apiCall(
      "GET",
      "/api/research/demographics",
      null,
      true
    );
    displayResults("Demographics", response);
  } catch (error) {
    showStatus("Failed to get demographics: " + error.message, "error");
  }
}

async function getPopulationHealth() {
  try {
    const response = await apiCall(
      "GET",
      "/api/research/population-health",
      null,
      true
    );
    displayResults("Population Health", response);
  } catch (error) {
    showStatus("Failed to get population health: " + error.message, "error");
  }
}

function displayResearcherProfile(data) {
  const profileContent = document.getElementById("profileContent");
  let html = '<div class="profile-display">';
  html += `<div class="profile-item"><strong>Client ID</strong><span>${escapeHtml(
    data.clientId
  )}</span></div>`;
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
    const displayValue = formatValue(value, key);
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

function formatValue(value, key = "") {
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
    // For objects/arrays, create a readable nested display without JSON formatting
    if (Array.isArray(value)) {
      return `<div style="margin-top: 8px;">${value
        .map(
          (v) =>
            `<div style="padding: 4px 8px; background: var(--background); border-radius: 3px; margin-bottom: 4px;">${
              typeof v === "object" ? formatValue(v) : escapeHtml(String(v))
            }</div>`
        )
        .join("")}</div>`;
    } else {
      // Check if this is a breakdown object (all values are numbers)
      const allNumbers = Object.values(value).every(
        (v) => typeof v === "number"
      );
      const isCohortSummary = key && key.toLowerCase().includes("cohort");

      if (allNumbers && Object.keys(value).length > 0 && !isCohortSummary) {
        // Create a horizontal bar chart for demographic breakdowns (but not cohort summary)
        return createBarChart(value, key);
      } else {
        // For nested objects, create readable key-value pairs
        let objHtml = '<div style="margin-top: 8px;">';
        for (const [k, v] of Object.entries(value)) {
          const readableKey = formatKeyName(k);
          // Recursively format nested values, pass the key
          const formattedVal =
            typeof v === "object" ? formatValue(v, k) : escapeHtml(String(v));
          objHtml += `<div style="padding: 4px 8px; background: var(--background); border-radius: 3px; margin-bottom: 4px;"><strong>${escapeHtml(
            readableKey
          )}:</strong> ${formattedVal}</div>`;
        }
        objHtml += "</div>";
        return objHtml;
      }
    }
  }
  return escapeHtml(String(value));
}

function createBarChart(data, categoryLabel = "") {
  const entries = Object.entries(data);
  const total = entries.reduce((sum, [_, count]) => sum + count, 0);

  let html = `<div style="display: flex; flex-direction: column; gap: 8px; margin-top: 8px;">`;

  // Bar container (full width, no left label)
  html += `<div style="display: flex; height: 50px; background: #f0f0f0; border-radius: 4px; overflow: hidden; gap: 1px;">`;

  const colors = [
    "#4a90e2",
    "#e74c3c",
    "#2ecc71",
    "#f39c12",
    "#9b59b6",
    "#1abc9c",
  ];

  entries.forEach(([label, count], index) => {
    const percentage = (count / total) * 100;
    const color = colors[index % colors.length];
    const readableLabel = formatKeyName(label);

    html += `<div 
      style="flex: ${percentage}; background: ${color}; display: flex; align-items: center; justify-content: center; color: white; font-weight: 600; font-size: 0.85em; text-align: center; padding: 0 4px; cursor: pointer; position: relative; group: hover;"
      title="${readableLabel}: ${count} (${Math.round(percentage)}%)"
    >${percentage > 15 ? `${readableLabel}: ${count}` : ""}</div>`;
  });

  html += `</div></div>`;

  // Add legend below
  html += `<div style="display: flex; flex-wrap: wrap; gap: 12px; margin-top: 8px; font-size: 0.85em;">`;
  entries.forEach(([label, count], index) => {
    const percentage = (count / total) * 100;
    const color = colors[index % colors.length];
    const readableLabel = formatKeyName(label);
    html += `<div style="display: flex; align-items: center; gap: 6px;">
      <div style="width: 12px; height: 12px; background: ${color}; border-radius: 2px;"></div>
      <span>${readableLabel}: ${count} (${Math.round(percentage)}%)</span>
    </div>`;
  });
  html += `</div>`;

  return html;
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
        `Cannot connect to API at ${baseUrl}. ` +
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

    const sameOrigin = window.location.origin;
    if (await backendRespondsToHealth(sameOrigin)) {
      return sameOrigin;
    }

    const remotePort = getRemotePortHint();
    if (remotePort) {
      const candidate = `${window.location.protocol}//${window.location.hostname}:${remotePort}`;
      if (await backendRespondsToHealth(candidate)) {
        return candidate;
      }
      return candidate;
    }

    return DEFAULT_API_BASE_URL;
  })();

  try {
    const detected = await autoDetectPromise;
    cachedAutoApiBaseUrl = detected || DEFAULT_API_BASE_URL;
    try {
      localStorage.setItem(API_CONFIG_KEY, cachedAutoApiBaseUrl);
    } catch (error) {
      // ignore storage errors (private mode, etc.)
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

async function backendRespondsToHealth(baseUrl) {
  try {
    const response = await fetch(baseUrl + "/health", {
      method: "GET",
      cache: "no-store",
    });
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
