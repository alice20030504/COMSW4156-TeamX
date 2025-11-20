// API Configuration
const API_CONFIG_KEY = 'fitness_api_base_url';
const CLIENT_ID_KEY = 'fitness_client_id';

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
});

function initializeApp() {
    // Check if using file:// protocol
    if (window.location.protocol === 'file:') {
        showStatus('Warning: Using file:// protocol may cause CORS issues. Please use a local web server. See frontend/README.md', 'error');
    }
    
    // Load saved API URL
    const savedApiUrl = localStorage.getItem(API_CONFIG_KEY);
    if (savedApiUrl) {
        document.getElementById('apiBaseUrl').value = savedApiUrl;
    }

    // Load saved client ID
    const clientId = localStorage.getItem(CLIENT_ID_KEY);
    if (clientId) {
        showAuthenticatedView(clientId);
    } else {
        showRegistrationView();
    }

    // Setup event listeners
    setupEventListeners();
    
    // Test connection on load
    testConnection();
}

function setupEventListeners() {
    // Registration form
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    
    // Plan form
    document.getElementById('planForm').addEventListener('submit', handlePlanSubmit);
    
    // Action buttons
    document.getElementById('loadProfileBtn').addEventListener('click', loadProfile);
    document.getElementById('getBmiBtn').addEventListener('click', getBMI);
    document.getElementById('getCaloriesBtn').addEventListener('click', getCalories);
    document.getElementById('getRecommendationBtn').addEventListener('click', getRecommendation);
    document.getElementById('listProfilesBtn').addEventListener('click', listProfiles);
    
    // Clear client button
    document.getElementById('clearClientBtn').addEventListener('click', clearClient);
    
    // Save API URL button
    document.getElementById('saveApiUrlBtn').addEventListener('click', saveApiUrl);
    
    // Test connection button
    const testBtn = document.getElementById('testConnectionBtn');
    if (testBtn) {
        testBtn.addEventListener('click', testConnection);
    }
}

function getApiBaseUrl() {
    return document.getElementById('apiBaseUrl').value || 'http://localhost:8080';
}

function getClientId() {
    return localStorage.getItem(CLIENT_ID_KEY);
}

function saveApiUrl() {
    const url = document.getElementById('apiBaseUrl').value;
    localStorage.setItem(API_CONFIG_KEY, url);
    showStatus('API URL saved!', 'success');
}

function showRegistrationView() {
    document.getElementById('registrationSection').style.display = 'block';
    document.getElementById('authenticatedSection').style.display = 'none';
    document.getElementById('clientInfo').style.display = 'none';
}

function showAuthenticatedView(clientId) {
    document.getElementById('registrationSection').style.display = 'none';
    document.getElementById('authenticatedSection').style.display = 'block';
    document.getElementById('clientInfo').style.display = 'block';
    document.getElementById('clientIdDisplay').textContent = clientId;
}

function clearClient() {
    if (confirm('Are you sure you want to clear your client ID and register a new profile?')) {
        localStorage.removeItem(CLIENT_ID_KEY);
        showRegistrationView();
        showStatus('Client ID cleared. Please register a new profile.', 'info');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    
    const formData = {
        name: document.getElementById('name').value.trim(),
        weight: parseFloat(document.getElementById('weight').value),
        height: parseFloat(document.getElementById('height').value),
        birthDate: document.getElementById('birthDate').value,
        gender: document.getElementById('gender').value,
        goal: document.getElementById('goal').value
    };

    try {
        const response = await apiCall('POST', '/api/persons', formData, false);
        const data = JSON.parse(response);
        
        if (data.clientId) {
            localStorage.setItem(CLIENT_ID_KEY, data.clientId);
            showAuthenticatedView(data.clientId);
            showStatus('Registration successful! Client ID: ' + data.clientId, 'success');
            // Clear form
            document.getElementById('registerForm').reset();
        } else {
            showStatus('Registration failed: No client ID returned', 'error');
        }
    } catch (error) {
        showStatus('Registration failed: ' + error.message, 'error');
    }
}

async function handlePlanSubmit(e) {
    e.preventDefault();
    
    const formData = {
        targetChangeKg: parseFloat(document.getElementById('targetChangeKg').value),
        durationWeeks: parseInt(document.getElementById('durationWeeks').value),
        trainingFrequencyPerWeek: parseInt(document.getElementById('trainingFrequencyPerWeek').value),
        planStrategy: document.getElementById('planStrategy').value
    };

    try {
        const response = await apiCall('POST', '/api/persons/plan', formData, true);
        displayResults('Plan Configuration', response);
        showStatus('Plan saved successfully!', 'success');
    } catch (error) {
        showStatus('Failed to save plan: ' + error.message, 'error');
    }
}

async function loadProfile() {
    try {
        const response = await apiCall('GET', '/api/persons/me', null, true);
        const data = JSON.parse(response);
        displayProfile(data);
    } catch (error) {
        showStatus('Failed to load profile: ' + error.message, 'error');
        document.getElementById('profileContent').innerHTML = 
            '<p class="placeholder">Failed to load profile. Make sure you are registered.</p>';
    }
}

async function getBMI() {
    try {
        const response = await apiCall('GET', '/api/persons/bmi', null, true);
        displayResults('BMI Calculation', response);
    } catch (error) {
        showStatus('Failed to get BMI: ' + error.message, 'error');
    }
}

async function getCalories() {
    try {
        const response = await apiCall('GET', '/api/persons/calories', null, true);
        displayResults('Calorie Calculation', response);
    } catch (error) {
        showStatus('Failed to get calories: ' + error.message, 'error');
    }
}

async function getRecommendation() {
    try {
        const response = await apiCall('GET', '/api/persons/recommendation', null, true);
        displayResults('Recommendation', response);
    } catch (error) {
        showStatus('Failed to get recommendation: ' + error.message, 'error');
    }
}

async function listProfiles() {
    try {
        const response = await apiCall('GET', '/api/persons', null, true);
        displayResults('All Profiles', response);
    } catch (error) {
        showStatus('Failed to list profiles: ' + error.message, 'error');
    }
}

function displayProfile(data) {
    const profileContent = document.getElementById('profileContent');
    let html = '<div class="profile-display">';
    
    if (data.name) html += `<div class="profile-item"><strong>Name</strong><span>${escapeHtml(data.name)}</span></div>`;
    if (data.weight) html += `<div class="profile-item"><strong>Weight</strong><span>${data.weight} kg</span></div>`;
    if (data.height) html += `<div class="profile-item"><strong>Height</strong><span>${data.height} cm</span></div>`;
    if (data.birthDate) html += `<div class="profile-item"><strong>Birth Date</strong><span>${data.birthDate}</span></div>`;
    if (data.gender) html += `<div class="profile-item"><strong>Gender</strong><span>${data.gender}</span></div>`;
    if (data.goal) html += `<div class="profile-item"><strong>Goal</strong><span>${data.goal}</span></div>`;
    if (data.targetChangeKg) html += `<div class="profile-item"><strong>Target Change</strong><span>${data.targetChangeKg} kg</span></div>`;
    if (data.targetDurationWeeks) html += `<div class="profile-item"><strong>Duration</strong><span>${data.targetDurationWeeks} weeks</span></div>`;
    if (data.trainingFrequencyPerWeek) html += `<div class="profile-item"><strong>Training Frequency</strong><span>${data.trainingFrequencyPerWeek} per week</span></div>`;
    if (data.planStrategy) html += `<div class="profile-item"><strong>Plan Strategy</strong><span>${data.planStrategy}</span></div>`;
    
    html += '</div>';
    profileContent.innerHTML = html;
}

function displayResults(title, response) {
    const resultsContent = document.getElementById('resultsContent');
    let html = `<h4 style="margin-bottom: 10px;">${title}</h4>`;
    
    try {
        const data = JSON.parse(response);
        html += '<div class="json-display">' + JSON.stringify(data, null, 2) + '</div>';
    } catch (e) {
        html += '<div class="json-display">' + escapeHtml(response) + '</div>';
    }
    
    resultsContent.innerHTML = html;
}

async function apiCall(method, path, body, requireAuth) {
    const baseUrl = getApiBaseUrl();
    const url = baseUrl + path;
    
    // Check if we're using file:// protocol (which causes CORS issues)
    if (window.location.protocol === 'file:') {
        throw new Error('Cannot use file:// protocol. Please use a local web server. See frontend/README.md for instructions.');
    }
    
    const options = {
        method: method,
        headers: {}
    };
    
    if (requireAuth) {
        const clientId = getClientId();
        if (!clientId) {
            throw new Error('Client ID not found. Please register first.');
        }
        options.headers['X-Client-ID'] = clientId;
    }
    
    if (body) {
        options.headers['Content-Type'] = 'application/json';
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
                } else if (typeof errorData === 'string') {
                    errorMsg = errorData;
                }
            } catch (e) {
                if (text) errorMsg = text;
            }
            throw new Error(errorMsg);
        }
        
        return text;
    } catch (error) {
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            const diagnosticMsg = `Cannot connect to API at ${baseUrl}. ` +
                `Please check: 1) Backend is running (try: mvn spring-boot:run), ` +
                `2) Using a web server (not file://), ` +
                `3) API URL is correct. ` +
                `See frontend/README.md for setup instructions.`;
            throw new Error(diagnosticMsg);
        }
        throw error;
    }
}

function showStatus(message, type = 'info') {
    const statusEl = document.getElementById('statusMessage');
    statusEl.textContent = message;
    statusEl.className = `status-message ${type}`;
    statusEl.style.display = 'block';
    
    setTimeout(() => {
        statusEl.style.display = 'none';
    }, 5000);
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

async function testConnection() {
    const statusEl = document.getElementById('connectionStatus');
    const statusText = document.getElementById('connectionStatusText');
    
    if (!statusEl || !statusText) return;
    
    statusEl.style.display = 'block';
    statusText.textContent = 'Checking...';
    
    const baseUrl = getApiBaseUrl();
    
    try {
        // Try to connect to root health endpoint (doesn't require auth)
        const response = await fetch(baseUrl + '/health');
        if (response.ok) {
            statusText.textContent = '✓ Connected to ' + baseUrl;
            statusText.style.color = 'var(--success-color)';
        } else {
            statusText.textContent = '✗ Backend responded with error (HTTP ' + response.status + ')';
            statusText.style.color = 'var(--danger-color)';
        }
    } catch (error) {
        statusText.textContent = '✗ Cannot connect to ' + baseUrl + '. Is the backend running?';
        statusText.style.color = 'var(--danger-color)';
        
        if (window.location.protocol === 'file:') {
            statusText.textContent += ' (Also: Use a web server, not file://)';
        }
    }
}

