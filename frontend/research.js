// API Configuration
const CLIENT_ID_KEY = 'fitness_research_client_id';

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
});

function initializeApp() {
    // Check if using file:// protocol
    if (window.location.protocol === 'file:') {
        showStatus('Warning: Using file:// protocol may cause CORS issues. Please use a local web server. See frontend/README.md', 'error');
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
}

function setupEventListeners() {
    // Registration form
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    
    // Action buttons
    document.getElementById('getDemographicsBtn').addEventListener('click', getDemographics);
    document.getElementById('getPopulationHealthBtn').addEventListener('click', getPopulationHealth);
    
    // Clear client button
    document.getElementById('clearClientBtn').addEventListener('click', clearClient);
}

function getApiBaseUrl() {
    return 'http://localhost:8080';
}

function getClientId() {
    return localStorage.getItem(CLIENT_ID_KEY);
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
        email: document.getElementById('email').value.trim().toLowerCase()
    };

    try {
        const response = await apiCall('POST', '/api/research', formData, false);
        const data = JSON.parse(response);
        
        if (data.clientId) {
            localStorage.setItem(CLIENT_ID_KEY, data.clientId);
            showAuthenticatedView(data.clientId);
            showStatus('Registration successful! Client ID: ' + data.clientId, 'success');
            // Clear form
            document.getElementById('registerForm').reset();
            // Display profile
            displayResearcherProfile(data);
        } else {
            showStatus('Registration failed: No client ID returned', 'error');
        }
    } catch (error) {
        showStatus('Registration failed: ' + error.message, 'error');
    }
}

async function getDemographics() {
    try {
        const response = await apiCall('GET', '/api/research/demographics', null, true);
        displayResults('Demographics', response);
    } catch (error) {
        showStatus('Failed to get demographics: ' + error.message, 'error');
    }
}

async function getPopulationHealth() {
    try {
        const response = await apiCall('GET', '/api/research/population-health', null, true);
        displayResults('Population Health', response);
    } catch (error) {
        showStatus('Failed to get population health: ' + error.message, 'error');
    }
}

function displayResearcherProfile(data) {
    const profileContent = document.getElementById('profileContent');
    let html = '<div class="profile-display">';
    html += `<div class="profile-item"><strong>Client ID</strong><span>${escapeHtml(data.clientId)}</span></div>`;
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

