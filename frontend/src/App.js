import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Register from "./pages/Register";
import Login from "./pages/Login";
import UserDashboard from "./pages/UserDashboard";
import ResearchDashboard from "./pages/ResearchDashboard";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/register" />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard-user" element={<UserDashboard />} />
        <Route path="/dashboard-research" element={<ResearchDashboard />} />
      </Routes>
    </Router>
  );
}

export default App;
