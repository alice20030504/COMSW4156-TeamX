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
   - **Mac/Linux**: Open from file manager or use `open index.html` (Mac) / `xdg-open index.html` (Linux)

### Option 2: Local Web Server (Recommended)

For better security and CORS handling, use a local web server:

#### Using Python (if installed):

```bash
# Python 3
cd frontend
python -m http.server 3000

# Then open: http://localhost:3000
```

#### Using Node.js (if installed):

```bash
# Install http-server globally (one time)
npm install -g http-server

# Run the server
cd frontend
http-server -p 3000

# Then open: http://localhost:3000
```

#### Using PHP (if installed):

```bash
cd frontend
php -S localhost:3000

# Then open: http://localhost:3000
```

## Configuration

### API Base URL

The frontend now determines which backend to talk to in the following order:

1. **URL override** – append `?apiBaseUrl=<url>` (or `?api=<url>`) to `mobile.html`/`research.html`/`landing.html`. Example: `mobile.html?apiBaseUrl=http://35.188.26.134:8080`. The value is normalized, stored in localStorage, and reused on refresh.
2. **Saved configuration** – if you previously saved a URL (via the "API Configuration" panel on the main dashboard) it is loaded from localStorage.
3. **Auto-detection** – when the page is hosted on anything other than `localhost`/`127.0.0.1`, the frontend automatically targets the same origin (`window.location.origin`). This covers VM deployments such as `http://35.188.26.134:8080` with no manual steps.
4. **Fallback** – if none of the above apply, the client uses `http://localhost:8080` for local development.

You can still change the value at any time from the "API Configuration" area, which updates localStorage for that browser/tab.

### Connecting to Remote Services

If your backend is deployed remotely (GCP VM, Cloud Run, etc.), either rely on the auto-detection (when the static files are served from the same host/port) or pass the `apiBaseUrl` query parameter when serving the static files from another domain. The override is persistent, so you only need to set it once per browser.

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
- **List All Profiles**: View all profiles associated with your client ID

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
