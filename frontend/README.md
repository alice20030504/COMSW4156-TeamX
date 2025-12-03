# Frontend Web Client

A simple, modern web-based client for the Personal Fitness Management Service API. This client provides a user-friendly interface to interact with the fitness service, allowing users to register profiles, configure goal plans, and retrieve fitness metrics.

## Features

- **User Registration**: Register new fitness profiles with personal information
- **Profile Management**: View and manage your fitness profile
- **Goal Plan Configuration**: Set up personalized fitness plans with target changes, duration, and training frequency
- **Fitness Metrics**:
  - Calculate and view BMI (Body Mass Index)
  - Get daily calorie recommendations
  - Receive personalized fitness recommendations
- **Multiple Client Support**: Each browser tab/window can maintain its own client session independently

## How It Works

### Client Identification

The frontend uses the `X-Client-ID` header to identify each client instance. Here's how it works:

1. **Registration**: When you register a new profile, the API returns a unique `clientId` (e.g., `mobile-id1`, `mobile-id2`)
2. **Storage**: The client ID is stored in the browser's `localStorage` for persistence across page refreshes
3. **Authentication**: All authenticated API calls include the `X-Client-ID` header with your stored client ID
4. **Isolation**: Each client ID maintains its own isolated data - you can only see and modify profiles associated with your client ID

### Multiple Client Instances

The service supports multiple simultaneous client instances through the `X-Client-ID` header mechanism:

- **Different Browser Tabs**: Each tab can have a different client ID stored in its own localStorage
- **Different Browsers**: Each browser maintains its own localStorage, allowing multiple users to access the service simultaneously
- **Different Devices**: Each device/browser combination can maintain separate client sessions
- **Service Isolation**: The backend's `ClientIdInterceptor` intercepts every request and uses the `X-Client-ID` header to:
  - Store the client ID in `ClientContext` for the request lifecycle
  - Filter database queries to only return data belonging to that client ID
  - Ensure data isolation between different clients

This means you can:

- Open multiple browser tabs, each with a different client ID
- Run multiple instances of the web client on different machines
- All instances can run simultaneously without interfering with each other

## Prerequisites

- A modern web browser (Chrome, Firefox, Safari, Edge)
- The backend service running (see main README for setup instructions)
- No additional build tools or dependencies required (pure HTML/CSS/JavaScript)

## Setup and Running

### Option 1: Direct File Access

1. Ensure the backend service is running (default: `http://localhost:8080`)
2. Open `index.html` in your web browser:
   - **Windows**: Double-click `index.html` or right-click → "Open with" → your browser

### Option 2: Run with Docker (Backend + Frontend)

From the project root:

```bash
docker-compose up --build
# Backend: http://localhost:8080
# Frontend: http://localhost:3000
```

### Option 3: Test with GCP-Deployed Server

The frontend can also connect to the service deployed on Google Cloud Platform:

**GCP Server Details:**

- IP Address: `35.188.26.134`
- Backend Port: `8080`
- Frontend Port: `3000`

**Option 3a: Access Frontend Directly on GCP**

Open your web browser and navigate to:

```
http://35.188.26.134:3000
```

The frontend will automatically connect to the backend service at `http://35.188.26.134:8080`.

**Option 3b: Run Local Frontend Connected to GCP Backend**

1. Run the local frontend web server:

```bash
cd frontend
python -m http.server 3000
# Or: http-server -p 3000
```

2. Open your web browser to:

```
http://localhost:3000
```

3. The frontend will automatically detect the GCP backend, or you can manually configure it:
   - Click on the API Configuration area
   - Set the API Base URL to: `http://35.188.26.134:8080`
   - Your setting will be saved and reused on refresh

## Configuration

### API Base URL

The frontend now determines which backend to talk to in the following order:

1. **URL override** – append `?apiBaseUrl=<url>` (or `?api=<url>`) to `mobile.html`/`research.html`/`landing.html`. Example: `mobile.html?apiBaseUrl=http://35.188.26.134:8080`. The value is normalized, stored in localStorage, and reused on refresh.
2. **Saved configuration** – if you previously saved a URL (via the "API Configuration" panel on the main dashboard) it is loaded from localStorage.
3. **HTML/JS override** – set `window.__FITNESS_API_BASE_URL__ = '<url>'` before the client script runs, or add `<meta name="fitness-api-base-url" content="https://api.example.com">` (optionally pair with `data-fitness-api-port` / `window.__FITNESS_API_PORT__`). This is handy when templating HTML during deployment.
4. **Auto-detection** – when no override exists, the client pings `/health` on the current origin. If that endpoint responds, the origin is used; otherwise it automatically retries the same hostname on port `8080` (or the provided port hint) which covers split static/frontend hosting on a VM such as `http://35.188.26.134:8080`.
5. **Fallback** – if none of the above apply, the client uses `http://localhost:8080` for local development.

You can still change the value at any time from the "API Configuration" area, which updates localStorage for that browser/tab.

Each dashboard displays the currently resolved backend URL (and how it was derived) directly under the page header so you can immediately confirm which host/port is in use when debugging VM deployments.

### Connecting to Remote Services

If your backend is deployed remotely (GCP VM, Cloud Run, etc.), either rely on the auto-detection (same host, different port) or set one of the overrides above (`?apiBaseUrl=…`, `<meta name="fitness-api-base-url">`, `window.__FITNESS_API_BASE_URL__`). Port-specific hints can be supplied with `<meta name="fitness-api-port" content="8081">` or `data-fitness-api-port="9090"` on the `<body>`/`<html>` tags. Every option persists per browser, so you only need to configure it once.

## Usage

### 1. Register a New Profile

1. Fill out the registration form with:
   - Name
   - Weight (in kg)
   - Height (in cm)
   - Birth Date
   - Gender (Male/Female)
   - Fitness Goal (Cut/Bulk)
2. Click "Register"
3. Your client ID will be displayed at the top of the page
4. The authenticated dashboard will appear

### 2. Configure Your Goal Plan

1. In the "Configure Goal Plan" section, enter:
   - Target Change (kg): How much weight you want to gain/lose
   - Duration (weeks): Timeline for your goal
   - Training Frequency: Workouts per week (1-14)
   - Plan Strategy: Diet only, Workout only, or Both
2. Click "Save Plan"

### 3. View Your Profile

Click "Load Profile" to see your complete profile information.

### 4. Get Fitness Metrics

Use the quick action buttons:

- **Get BMI**: Calculate your Body Mass Index
- **Get Calories**: Get daily calorie recommendations based on your plan
- **Get Recommendation**: Receive personalized motivational recommendations
- **View Stored Profile**: Retrieve and inspect the saved profile JSON for the active client ID

## Multiple Client Testing

To test multiple simultaneous clients:

1. **Different Browser Tabs**:

   - Open the frontend in one tab and register a profile
   - Open a new tab (or incognito window) and register another profile
   - Each tab maintains its own client ID

2. **Different Browsers**:

   - Open the frontend in Chrome and register
   - Open the same URL in Firefox and register
   - Each browser maintains separate localStorage

3. **Different Machines**:
   - Run the frontend on multiple machines
   - Each machine can register and use different client IDs
   - All can connect to the same backend simultaneously

## End-to-End Testing

For comprehensive end-to-end (E2E) testing documentation covering all client and service workflows, see **[`docs/E2E_TESTING.md`](../docs/E2E_TESTING.md)**. This includes:

- Step-by-step test procedures for mobile and research clients
- Verification checklists for all functionality
- Multi-client isolation testing
- API error handling validation
- Complete testing scenarios with expected outcomes

## F. Instructions for Third-Party Developers

**Authentication:**
- All API requests (except `/health`, `/swagger-ui.html`, `/api-docs`) require `X-Client-ID` header
- Client ID format: `mobile-<identifier>` or `research-<identifier>`
- Obtain client ID by registering via:
  - Mobile: `POST /api/persons` (returns `clientId` in response)
  - Research: `POST /api/research/register` (returns `clientId` in response)

**Required Headers:**
```
X-Client-ID: mobile-abc123
Content-Type: application/json
```

**Endpoints:**
See **[`docs/API_REFERENCE.md`](docs/API_REFERENCE.md)** for complete API documentation.

**Request/Response Formats:**
- All requests and responses use JSON
- Dates: `YYYY-MM-DD` format (e.g., `"1990-04-15"`)
- Numbers: Standard JSON numbers (no quotes)
- Enums: String values (e.g., `"MALE"`, `"FEMALE"`, `"CUT"`, `"BULK"`)

**Example Mobile Client Registration:**
```bash
curl -v -X POST http://localhost:8080/api/persons \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "weight": 75.5,
    "height": 180.0,
    "birthDate": "1990-01-15",
    "gender": "MALE",
    "goal": "CUT"
  }'


Response: 201 Created
{
  "id": 1,
  "clientId": "mobile-abc123",
  "name": "John Doe",
  ...
}
```

**Error Responses:**
- `400 Bad Request`: Missing/invalid `X-Client-ID`, invalid request body, validation errors
- `403 Forbidden`: Mobile client accessing research endpoint
- `404 Not Found`: Resource not found for the client ID
- `500 Internal Server Error`: Server-side errors

## Troubleshooting

### "Cannot connect to API" Error

- Ensure the backend service is running
- Check that the API Base URL is correct
- Verify CORS is enabled on the backend (it should be with `@CrossOrigin(origins = "*")`)

### "Client ID not found" Error

- Make sure you've registered a profile first
- Check browser console for localStorage errors
- Try clearing your browser's localStorage and registering again

### Data Not Appearing

- Verify your client ID is correct
- Check that you're using the same client ID that was used to create the data
- Ensure the backend service is running and accessible

## Browser Compatibility

- Chrome/Edge: Full support
- Firefox: Full support
- Safari: Full support
- Internet Explorer: Not supported (uses modern JavaScript features)

## Technical Details

- **No Build Step**: Pure HTML, CSS, and JavaScript - no compilation needed
- **LocalStorage**: Client IDs and API URLs are stored in browser localStorage
- **Fetch API**: Uses modern Fetch API for HTTP requests
- **Responsive Design**: Works on desktop and mobile devices
- **CORS**: Backend must have CORS enabled (already configured with `@CrossOrigin(origins = "*")`)

## File Structure

```
frontend/
├── index.html      # Main HTML structure
├── styles.css      # Styling and layout
├── app.js          # Application logic and API calls
└── README.md       # This file
```
