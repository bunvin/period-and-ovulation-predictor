import React, { useState, useEffect } from 'react';
import Layout from "./component/Layout";
import Login from "./component/Login";
import { userService } from './services/api';
import axios from 'axios';
import { EventData } from './types';

const App: React.FC = () => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const checkAuthentication = async () => {
      let events: any | EventData[];
        try {
        // Try to fetch user stats as an authentication check
            events= await userService.getPeriods();
        setIsAuthenticated(true);
      } catch (error) {
        if (axios.isAxiosError(error)) {
          // If 401 or other auth-related error, user is not authenticated
          if (error.response?.status === 401) {
            setIsAuthenticated(false);
          } else {
            console.error('Unexpected error:', error);
          }
        }
        setIsAuthenticated(false);
      } finally {
        setLoading(false);
      }
    };

    checkAuthentication();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  return isAuthenticated ? <Layout /> : <Login />;
};

export default App;