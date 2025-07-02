import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const { login: authLogin } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); // Clear any previous errors
    setLoading(true);

    try {
      await authLogin(email, password);
      // If login is successful, useAuth context should handle navigation
    } catch (err) {
      setLoading(false); // Ensure loading is off even if error parsing fails
      console.error('Login error:', err);

      if (err.response) {
        // The Auth Service is now designed to return a LoginResponse even for errors,
        // and set the appropriate HTTP status code.
        // The LoginResponse now has a 'message' field for errors.

        // Check for specific HTTP status codes and use the message from the response data
        if (err.response.status === 401) {
            // 401 Unauthorized: This typically means invalid credentials,
            // or an account that is disabled/locked, as handled by your User Microservice's
            // GlobalExceptionHandler and then propagated by Auth Service.
            setError(err.response.data?.message || 'Invalid email or password.');
        } else if (err.response.status === 400) {
            // 400 Bad Request: Might indicate malformed request or other client-side issues
            setError(err.response.data?.message || 'Invalid login request. Please check your input.');
        } else if (err.response.status === 404) {
            // 404 Not Found: Could indicate user not found if the service explicitly sends this
            setError(err.response.data?.message || 'User not found or service unavailable.');
        } else if (err.response.status >= 500) {
            // 5xx Server Errors: Generic server-side issues
            setError(err.response.data?.message || 'An internal server error occurred. Please try again later.');
        } else {
            // Catch any other unexpected HTTP error status codes
            setError(err.response.data?.message || 'An unexpected error occurred during login. Please try again.');
        }
      } else if (err.request) {
        // The request was made but no response was received (e.g., network error, CORS issue)
        setError('No response from server. Please check your network connection.');
      } else {
        // Something happened in setting up the request that triggered an Error
        setError('Error: ' + err.message);
      }
    }
  };

  return (
    <>
      <style>{`
        .custom-input::placeholder {
          color: #f3ebe1;
          opacity: 1;
        }
      `}</style>

      <div
        className="d-flex align-items-center justify-content-center min-vh-100 bg-light"
        style={{ fontFamily: 'Georgia, serif' }}
      >
        <div
          className="card shadow-lg p-4 p-md-5"
          style={{ maxWidth: '450px', width: '100%', borderRadius: '0.5rem' }}
        >
          <h2 className="card-title text-center mb-4 fw-bold" style={{ color: 'black' }}>
            Login
          </h2>
          <p className="text-center text-muted mb-4">Access your account</p>

          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label htmlFor="email" className="form-label text-secondary">Email:</label>
              <input
                type="email"
                id="email"
                placeholder="name@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="form-control rounded p-2 custom-input"
                style={{ backgroundColor: '#f3ebe1', fontFamily: 'Georgia, serif', color: 'black' }}
              />
            </div>

            <div className="mb-4">
              <label htmlFor="password" className="form-label text-secondary">Password:</label>
              <input
                type="password"
                id="password"
                placeholder="Enter password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="form-control rounded p-2 custom-input"
                style={{ backgroundColor: '#f3ebe1', fontFamily: 'Georgia, serif', color: 'black' }}
              />
            </div>

            {error && (
              <div className="alert alert-danger alert-dismissible fade show" style={{ borderRadius: '0.25rem' }} role="alert">
                {error}
                <button
                  type="button"
                  className="btn-close"
                  data-bs-dismiss="alert"
                  aria-label="Close"
                  onClick={() => setError('')}
                ></button>
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="btn w-100 py-2 rounded fw-bold mt-3"
              style={{
                backgroundColor: '#B57B5B',
                borderColor: '#B57B5B',
                color: '#fff',
                fontFamily: 'Georgia, serif',
                transition: 'background-color 0.2s ease-in-out, border-color 0.2s ease-in-out',
              }}
            >
              {loading ? 'Logging In...' : 'Login'}
            </button>
          </form>

          <div className="text-center mt-4">
            <p className="mb-2">
              Don't have an account?{' '}
              <Link to="/register" className="text-decoration-none fw-semibold" style={{ color: 'black' }}>
                Register here
              </Link>
            </p>
            <p className="mb-0">
              <Link to="/forgotpassword" className="text-decoration-none fw-semibold" style={{ color: 'black' }}>
                Forgot Password?
              </Link>
            </p>
          </div>
        </div>
      </div>
    </>
  );
};

export default LoginPage;
