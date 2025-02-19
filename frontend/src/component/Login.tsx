import React from 'react';
import './Login.css';
import { NavLink } from 'react-router-dom';

function termAndAgreement(): void {
  alert("I know nothing, this is just for fun")
}

const GoogleIcon = () => (
  <svg 
    xmlns="http://www.w3.org/2000/svg" 
    width="24" 
    height="24" 
    viewBox="0 0 24 24" 
    className="mr-3"
  >
    <path 
      fill="#4285F4" 
      d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.75h3.57c2.08-1.92 3.28-4.74 3.28-8.07z"
    />
    <path 
      fill="#34A853" 
      d="M12 23c2.97 0 5.46-1 7.28-2.69l-3.57-2.75c-.99.66-2.26 1.04-3.71 1.04-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
    />
    <path 
      fill="#FBBC05" 
      d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.66-2.84z"
    />
    <path 
      fill="#EA4335" 
      d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.46 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
    />
  </svg>
);

const Login: React.FC = () => {
  const handleGoogleLogin = () => {
    // Redirect to Spring Boot OAuth2 authorization endpoint
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h1 className="login-title">
            ✨ Period Predictor ✨
          </h1>
          <p className="login-subtitle">
            Track and predict your menstrual cycle with ease
          </p>
        </div>

        <div className="login-button-container">
          <button 
            onClick={handleGoogleLogin}
            className="login-button"
          >
            <GoogleIcon />
            <span className="login-button-text">
              Continue with Google
            </span>
          </button>
        </div>

        <div className="login-footer">
          <p>
            By logging in, you agree to our Terms of Service and Privacy Policy
          </p>
        </div>
      </div>

      <div className="login-background-decoration" />
    </div>
  );
};

export default Login;