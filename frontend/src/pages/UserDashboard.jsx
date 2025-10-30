import React, { useState } from 'react';

export default function UserDashboard() {
  const [personId, setPersonId] = useState('');
  const [person, setPerson] = useState(null);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const fetchPerson = async () => {
    if (!personId) return;
    setLoading(true);
    setMessage('');
    setPerson(null);
    try {
      const res = await fetch(`http://localhost:8080/api/persons/${personId}`, {
        headers: { 'X-Client-ID': 'mobile-guest' },
      });
      const data = await res.json();
      if (!res.ok) {
        setMessage(data.message || 'Failed to fetch person');
        setLoading(false);
        return;
      }
      setPerson(data);
      setLoading(false);
    } catch (e) {
      setMessage('Network error');
      setLoading(false);
    }
  };

  const computeBmi = () => {
    if (!person) return null;
    const hMeters = (person.height || 0) / 100;
    if (!hMeters) return null;
    const bmi = (person.weight || 0) / (hMeters * hMeters);
    return Number.isFinite(bmi) ? bmi.toFixed(2) : null;
  };

  const estimateCalories = () => {
    if (!person) return null;
    const calories = (person.weight || 0) * 24;
    return Number.isFinite(calories) ? Math.round(calories) : null;
  };

  return (
    <div style={{ maxWidth: 720, margin: '40px auto', padding: 24 }}>
      <h2>User Dashboard</h2>
      <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
        <input
          placeholder="Enter your person ID"
          value={personId}
          onChange={(e) => setPersonId(e.target.value)}
          style={{ padding: 8, flex: 1 }}
        />
        <button onClick={fetchPerson} disabled={loading} style={{ padding: '8px 12px' }}>
          {loading ? 'Loading…' : 'Load'}
        </button>
      </div>
      {message && <div style={{ color: 'crimson', marginBottom: 12 }}>{message}</div>}
      {person && (
        <div style={{ border: '1px solid #eee', borderRadius: 8, padding: 16 }}>
          <div><strong>Name:</strong> {person.name}</div>
          <div><strong>Weight (kg):</strong> {person.weight}</div>
          <div><strong>Height (cm):</strong> {person.height}</div>
          <div><strong>Birth date:</strong> {person.birthDate}</div>
          <div style={{ marginTop: 12 }}>
            <strong>BMI:</strong> {computeBmi() ?? 'N/A'}
          </div>
          <div>
            <strong>Estimated maintenance calories (kcal/day):</strong> {estimateCalories() ?? 'N/A'}
          </div>
        </div>
      )}
    </div>
  );
}


