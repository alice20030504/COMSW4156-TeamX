import React, { useEffect, useState } from 'react';

export default function ResearchDashboard() {
  const [population, setPopulation] = useState(null);
  const [demographics, setDemographics] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchAll = async () => {
      try {
        const [res1, res2] = await Promise.all([
          fetch('http://localhost:8080/api/research/population-health', {
            headers: { 'X-Client-ID': 'research-guest' },
          }),
          fetch('http://localhost:8080/api/research/demographics', {
            headers: { 'X-Client-ID': 'research-guest' },
          }),
        ]);
        const data1 = await res1.json();
        const data2 = await res2.json();
        if (!res1.ok) throw new Error(data1.message || 'Failed to fetch population health');
        if (!res2.ok) throw new Error(data2.message || 'Failed to fetch demographics');
        setPopulation(data1);
        setDemographics(data2);
      } catch (e) {
        setError(e.message || 'Network error');
      }
    };
    fetchAll();
  }, []);

  return (
    <div style={{ maxWidth: 900, margin: '40px auto', padding: 24 }}>
      <h2>Research Dashboard</h2>
      {error && <div style={{ color: 'crimson', marginBottom: 12 }}>{error}</div>}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
        <div style={{ border: '1px solid #eee', borderRadius: 8, padding: 16 }}>
          <h3 style={{ marginTop: 0 }}>Population Health</h3>
          <pre style={{ whiteSpace: 'pre-wrap' }}>{population ? JSON.stringify(population, null, 2) : 'Loading…'}</pre>
        </div>
        <div style={{ border: '1px solid #eee', borderRadius: 8, padding: 16 }}>
          <h3 style={{ marginTop: 0 }}>Demographics</h3>
          <pre style={{ whiteSpace: 'pre-wrap' }}>{demographics ? JSON.stringify(demographics, null, 2) : 'Loading…'}</pre>
        </div>
      </div>
    </div>
  );
}


