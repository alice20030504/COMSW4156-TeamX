import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Register from './pages/Register';
import Login from './pages/Login';
import UserDashboard from './pages/UserDashboard';
import ResearchDashboard from './pages/ResearchDashboard';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/register" replace />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard-user" element={<UserDashboard />} />
        <Route path="/dashboard-research" element={<ResearchDashboard />} />
      </Routes>
    </BrowserRouter>
  );
}


