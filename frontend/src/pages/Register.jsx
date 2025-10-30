import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'USER',
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    try {
      const res = await fetch('http://localhost:8080/api/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      const data = await res.json();
      if (!res.ok) {
        setMessage(data.message || 'Registration failed');
        setLoading(false);
        return;
      }
      setMessage('Registration successful. Redirecting to login...');
      setTimeout(() => navigate('/login'), 800);
    } catch (err) {
      setMessage('Network error. Please try again.');
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 460, margin: '40px auto', padding: 24, border: '1px solid #eee', borderRadius: 8 }}>
      <h2 style={{ marginTop: 0 }}>Create your account</h2>
      {message && (
        <div style={{ marginBottom: 12, color: message.includes('successful') ? 'green' : 'crimson' }}>{message}</div>
      )}
      <form onSubmit={onSubmit}>
        <div style={{ marginBottom: 12 }}>
          <label>Username</label>
          <input name="username" value={form.username} onChange={onChange} required style={{ width: '100%', padding: 8 }} />
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>Email</label>
          <input type="email" name="email" value={form.email} onChange={onChange} required style={{ width: '100%', padding: 8 }} />
        </div>
        <div style={{ display: 'flex', gap: 12 }}>
          <div style={{ flex: 1, marginBottom: 12 }}>
            <label>Password</label>
            <input type="password" name="password" value={form.password} onChange={onChange} required style={{ width: '100%', padding: 8 }} />
          </div>
          <div style={{ flex: 1, marginBottom: 12 }}>
            <label>Confirm Password</label>
            <input type="password" name="confirmPassword" value={form.confirmPassword} onChange={onChange} required style={{ width: '100%', padding: 8 }} />
          </div>
        </div>
        <div style={{ marginBottom: 16 }}>
          <label>Role</label>
          <select name="role" value={form.role} onChange={onChange} style={{ width: '100%', padding: 8 }}>
            <option value="USER">User</option>
            <option value="RESEARCHER">Researcher</option>
          </select>
        </div>
        <button type="submit" disabled={loading} style={{ width: '100%', padding: 10 }}>
          {loading ? 'Registering…' : 'Register'}
        </button>
      </form>
      <div style={{ marginTop: 12 }}>
        <Link to="/login">Already have an account? Log in</Link>
      </div>
    </div>
  );
}


