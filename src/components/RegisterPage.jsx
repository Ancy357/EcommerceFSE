import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api';
import { OverlayTrigger, Tooltip } from 'react-bootstrap';
import { useForm } from 'react-hook-form';

const RegisterPage = () => {
  // Initialize react-hook-form with onChange mode for real-time validation
  const { register, handleSubmit, watch, formState: { errors } } = useForm({
    mode: 'onChange' // This makes validation run on every change
  });
  const navigate = useNavigate();

  // Watch for password and confirmPassword fields for custom validation
  const password = watch('password', '');
  // No need to watch confirmPassword explicitly here as its validation
  // depends on the `password` field's value, which `watch('password')` already provides.

  // State variables for messages and loading status (general error/success, not field-specific)
  const [message, setMessage] = useState(''); // General success message
  const [apiError, setApiError] = useState(''); // General error message (e.g., network, 409 conflict)
  const [loading, setLoading] = useState(false);

  // Define a new, lighter wood brown color for buttons (consistent with UserDetailAdminPage)
  const buttonMainBrown = '#BC8F8F'; // RosyBrown

  // Tooltip component for input field information
  const InfoTooltip = ({ message }) => (
    <OverlayTrigger
      placement="right"
      overlay={
        <Tooltip
          style={{
            backgroundColor: '#000',
            color: '#fff',
            padding: '0.5rem 0.75rem',
            borderRadius: '0.3rem',
            fontSize: '0.875rem',
          }}
        >
          {message}
        </Tooltip>
      }
    >
      <span style={{ cursor: 'pointer', color: '#000', marginLeft: '6px' }}>ⓘ</span>
    </OverlayTrigger>
  );

  // This is the function that handleSubmit from react-hook-form will call
  const onSubmit = async (data) => {
    setMessage('');
    setApiError('');
    setLoading(true);

    try {
      // The `data` object already contains validated form values
      const response = await api.post('/api/auth/register', data);

      setMessage(response.data.message || 'Registration successful! You can now log in.');
      console.log('Registration successful:', response.data);

      setTimeout(() => {
        navigate('/login'); // Redirect to login page after a short delay
      }, 2000);

    } catch (err) {
      console.error('Registration error:', err);
      if (err.response) {
        if (err.response.status === 409) {
          setApiError('An account with this email already exists.');
        } else if (err.response.status === 400) {
          if (err.response.data.fieldErrors) {
            // Optional: If you want to show specific backend errors under fields
            // you can use setValue and setError from useForm.
            // For now, we'll keep the general API error for backend validation failures.
            setApiError(err.response.data.message || 'Registration failed. Please correct the highlighted fields and try again.');
          } else if (err.response.data.message) {
            setApiError(err.response.data.message);
          } else {
            setApiError('Registration failed. Please check your input.');
          }
        } else {
          setApiError(err.response.data.message || 'An unexpected error occurred during registration.');
        }
      } else if (err.request) {
        setApiError('No response from server. Please check your network connection.');
      } else {
        setApiError('Error: ' + err.message);
      }
    } finally {
      setLoading(false);
    }
  };

  // --- Inline Styles for hover effects and transitions ---
  const transitionStyle = {
    transition: 'all 300ms cubic-bezier(0.4, 0, 0.2, 1)', // ease-in-out
  };

  const buttonHoverStyle = {
    transform: 'scale(1.05)',
    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
  };

  const linkHoverUnderlineStyle = {
    position: 'relative',
    textDecoration: 'none',
    display: 'inline-block',
  };

  const beforeHoverLinkStyle = {
    content: "''",
    position: 'absolute',
    width: '0',
    height: '2px',
    bottom: '0',
    left: '0',
    backgroundColor: '#0d6efd', // Bootstrap primary color
    transition: 'width 0.3s ease-out',
  };

  return (
    <div className="d-flex align-items-center justify-content-center bg-light animate__animated animate__fadeIn py-5">
      <div className="card shadow-lg p-4 p-md-5 rounded-lg w-100" style={{ maxWidth: '600px' }}>
        <h2 className="text-center mb-4 text-dark fw-bold border-bottom pb-3">
          <i className="bi bi-person-plus-fill me-2 text-dark"></i> Register
        </h2>

        {/* General success message alert */}
        {message && (
          <div className="alert alert-success alert-dismissible fade show text-center rounded-pill animate__animated animate__fadeIn" role="alert">
            {message}
            <button type="button" className="btn-close" data-bs-dismiss="alert" aria-label="Close" onClick={() => setMessage('')}></button>
          </div>
        )}
        {/* General error message alert */}
        {apiError && (
          <div className="alert alert-danger alert-dismissible fade show text-center rounded-pill animate__animated animate__fadeIn" role="alert">
            {apiError}
            <button type="button" className="btn-close" data-bs-dismiss="alert" aria-label="Close" onClick={() => setApiError('')}></button>
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="row g-3 mb-3">
            {/* First Name Field */}
            <div className="col-md-6">
              <label htmlFor="firstName" className="form-label text-muted fw-semibold">
                First Name:
                <InfoTooltip message="First name cannot be empty" />
              </label>
              <input
                type="text"
                id="firstName"
                {...register("firstName", {
                  required: "First name is required",
                  minLength: { value: 2, message: "First name must be at least 2 characters" },
                  maxLength: { value: 50, message: "First name cannot exceed 50 characters" },
                  pattern: {
                    value: /^[a-zA-Z\s\-]+$/,
                    message: "First name can only contain letters, spaces, or hyphens"
                  }
                })}
                className={`form-control rounded-pill focus-ring focus-ring-primary ${errors.firstName ? 'is-invalid' : ''}`}
                style={{ borderColor: '#f3ebe1' }}
                placeholder="John"
              />
              {errors.firstName && <div className="invalid-feedback d-block">{errors.firstName.message}</div>}
            </div>

            {/* Last Name Field */}
            <div className="col-md-6">
              <label htmlFor="lastName" className="form-label text-muted fw-semibold">
                Last Name:
                <InfoTooltip message="Last name cannot be empty" />
              </label>
              <input
                type="text"
                id="lastName"
                {...register("lastName", {
                  required: "Last name is required",
                  minLength: { value: 2, message: "Last name must be at least 2 characters" },
                  maxLength: { value: 50, message: "Last name cannot exceed 50 characters" },
                  pattern: {
                    value: /^[a-zA-Z\s\-]+$/,
                    message: "Last name can only contain letters, spaces, or hyphens"
                  }
                })}
                className={`form-control rounded-pill focus-ring focus-ring-primary ${errors.lastName ? 'is-invalid' : ''}`}
                style={{ borderColor: '#f3ebe1' }}
                placeholder="Doe"
              />
              {errors.lastName && <div className="invalid-feedback d-block">{errors.lastName.message}</div>}
            </div>

            {/* Email Field */}
            <div className="col-12">
              <label htmlFor="email" className="form-label text-muted fw-semibold">
                Email:
                <InfoTooltip message="Enter a valid email that hasn't been used before" />
              </label>
              <input
                type="email"
                id="email"
                {...register("email", {
                  required: "Email is required",
                  pattern: {
                    value: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/,
                    message: "Invalid email address"
                  },
                  maxLength: { value: 100, message: "Email cannot exceed 100 characters" }
                })}
                className={`form-control rounded-pill focus-ring focus-ring-primary ${errors.email ? 'is-invalid' : ''}`}
                style={{ borderColor: '#f3ebe1' }}
                placeholder="john.doe@example.com"
              />
              {errors.email && <div className="invalid-feedback d-block">{errors.email.message}</div>}
            </div>

            {/* Password Field */}
            <div className="col-md-6">
              <label htmlFor="password" className="form-label text-muted fw-semibold">
                Password:
                <InfoTooltip message="At least one uppercase, one lowercase, one digit, and one special character, at least 8 characters" />
              </label>
              <input
                type="password"
                id="password"
                {...register("password", {
                  required: "Password is required",
                  minLength: { value: 8, message: "Password must be at least 8 characters" },
                  maxLength: { value: 30, message: "Password cannot exceed 30 characters" },
                  pattern: {
                    value: /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?`~]).{8,30}$/,
                    message: "Password must contain at least one digit, one lowercase, one uppercase, and one special character."
                  }
                })}
                className={`form-control rounded-pill focus-ring focus-ring-primary ${errors.password ? 'is-invalid' : ''}`}
                style={{ borderColor: '#f3ebe1' }}
                placeholder="Enter your password"
              />
              {errors.password && <div className="invalid-feedback d-block">{errors.password.message}</div>}
            </div>

            {/* Confirm Password Field */}
            <div className="col-md-6">
              <label htmlFor="confirmPassword" className="form-label text-muted fw-semibold">
                Confirm Password:
                <InfoTooltip message="Must exactly match the password field" />
              </label>
              <input
                type="password"
                id="confirmPassword"
                {...register("confirmPassword", {
                  required: "Confirm password is required",
                  validate: (value) =>
                    value === password || "Passwords do not match" // Custom validation for match
                })}
                className={`form-control rounded-pill focus-ring focus-ring-primary ${errors.confirmPassword ? 'is-invalid' : ''}`}
                style={{ borderColor: '#f3ebe1' }}
                placeholder="Confirm your password"
              />
              {errors.confirmPassword && <div className="invalid-feedback d-block">{errors.confirmPassword.message}</div>}
            </div>

            {/* Phone Number Field */}
            <div className="col-md-6">
              <label htmlFor="phoneNumber" className="form-label text-muted fw-semibold">
                Phone Number:
                <InfoTooltip message="Use digits, spaces, hyphens or leading +" />
              </label>
              <input
                type="tel"
                id="phoneNumber"
                {...register("phoneNumber", {
                  required: "Phone number is required",
                  minLength: { value: 10, message: "Phone number must be at least 10 digits" },
                  maxLength: { value: 15, message: "Phone number cannot exceed 15 digits" },
                  pattern: {
                    value: /^\+?[0-9\s\-]+$/,
                    message: "Phone number can only contain digits, spaces, hyphens, and an optional leading '+'"
                  }
                })}
                className={`form-control rounded-pill focus-ring focus-ring-primary ${errors.phoneNumber ? 'is-invalid' : ''}`}
                style={{ borderColor: '#f3ebe1' }}
                placeholder="+15551234567"
              />
              {errors.phoneNumber && <div className="invalid-feedback d-block">{errors.phoneNumber.message}</div>}
            </div>

            {/* Date of Birth Field */}
            <div className="col-md-6">
              <label htmlFor="dateOfBirth" className="form-label text-muted fw-semibold">
                Date of Birth:
                <InfoTooltip message="Must be a date in the past" />
              </label>
              <input
                type="date"
                id="dateOfBirth"
                {...register("dateOfBirth", {
                  required: "Date of birth is required",
                  validate: (value) => {
                    const dob = new Date(value);
                    const today = new Date();
                    today.setHours(0, 0, 0, 0); // Normalize to start of day for accurate comparison
                    if (dob >= today) {
                      return "Date of birth must be in the past";
                    }
                    // Ensure user is at least 18 years old
                    const eighteenYearsAgo = new Date();
                    eighteenYearsAgo.setFullYear(eighteenYearsAgo.getFullYear() - 18);
                    if (dob > eighteenYearsAgo) {
                      return "You must be at least 18 years old.";
                    }
                    return true;
                  }
                })}
                className={`form-control rounded-pill focus-ring focus-ring-primary ${errors.dateOfBirth ? 'is-invalid' : ''}`}
                style={{ borderColor: '#f3ebe1' }}
              />
              {errors.dateOfBirth && <div className="invalid-feedback d-block">{errors.dateOfBirth.message}</div>}
            </div>

            {/* Gender Field */}
            <div className="col-12">
              <label htmlFor="gender" className="form-label text-muted fw-semibold">
                Gender:
                <InfoTooltip message="Choose from Male, Female, Other or Prefer not to say" />
              </label>
              <select
                id="gender"
                {...register("gender", {
                  required: "Gender is required",
                  pattern: {
                    value: /^(Male|Female|Other|Prefer not to say)$/,
                    message: "Invalid gender selection"
                  }
                })}
                className={`form-select rounded-pill focus-ring focus-ring-primary ${errors.gender ? 'is-invalid' : ''}`}
                style={{ borderColor: '#f3ebe1' }}
              >
                <option value="">Select Gender</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Prefer not to say">Prefer not to say</option>
                <option value="Other">Other</option>
              </select>
              {errors.gender && <div className="invalid-feedback d-block">{errors.gender.message}</div>}
            </div>
          </div>

          <div className="d-grid mt-4">
            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary btn-lg rounded-2 shadow-sm fw-semibold"
              style={{
                backgroundColor: buttonMainBrown,
                borderColor: buttonMainBrown,
                color: '#fff',
                ...transitionStyle
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = buttonHoverStyle.transform;
                e.currentTarget.style.boxShadow = buttonHoverStyle.boxShadow;
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'scale(1)';
                e.currentTarget.style.boxShadow = '0 .125rem .25rem rgba(0,0,0,.075)';
                e.currentTarget.style.backgroundColor = buttonMainBrown;
              }}
            >
              {loading ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Registering...
                </>
              ) : (
                <>
                  <i className="bi bi-person-check-fill me-2"></i> Register
                </>
              )}
            </button>
          </div>
        </form>

        <p className="text-center mt-3 text-secondary">
          Already have an account? {' '}
          <Link
            to="/login"
            className="text-decoration-none text-primary fw-semibold"
            style={linkHoverUnderlineStyle}
            onMouseEnter={(e) => {
              const styleElem = document.createElement('style');
              styleElem.innerHTML = `
                .login-link-underline::after {
                  content: '';
                  position: absolute;
                  width: 100%;
                  height: ${beforeHoverLinkStyle.height};
                  bottom: ${beforeHoverLinkStyle.bottom};
                  left: ${beforeHoverLinkStyle.left};
                  background-color: ${beforeHoverLinkStyle.backgroundColor};
                  transition: ${beforeHoverLinkStyle.transition};
                }
              `;
              e.currentTarget.appendChild(styleElem);
            }}
            onMouseLeave={(e) => {
              const styleElem = e.currentTarget.querySelector('style');
              if (styleElem) {
                styleElem.remove();
              }
            }}
          >
            <span className="login-link-underline" style={{ color: '#0d6efd' }}>Login here</span>
          </Link>
        </p>
      </div>
    </div>
  );
};

export default RegisterPage;