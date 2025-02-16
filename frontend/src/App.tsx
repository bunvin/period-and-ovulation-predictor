// App.tsx
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useEffect, useState } from 'react';
import axios from 'axios';

// Define interfaces for our data types
interface User {
  id: number;
  email: string;
  name: string;
  picture?: string;
}

interface CalendarStats {
  averageCycleLength: number;
  eventCount: number;
}

const App: React.FC = () => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    // Check if user is authenticated
    axios.get<CalendarStats>('http://localhost:8080/api/calendar/stats', { 
      withCredentials: true 
    })
      .then(response => {
        // Fetch user details after confirming authentication
        return axios.get<User>('http://localhost:8080/api/users/me', {
          withCredentials: true
        });
      })
      .then(response => {
        setUser(response.data);
        setLoading(false);
      })
      .catch((error: Error) => {
        console.error('Authentication error:', error);
        setUser(null);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <Router>
      <Routes>
        <Route path="/" element={user ? <Dashboard user={user} /> : <Login />} />
        <Route path="/login" element={<Login />} />
      </Routes>
    </Router>
  );
};

// Login component
const Login: React.FC = () => {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
      <div className="p-8 bg-white rounded-lg shadow-md">
        <h1 className="text-2xl font-bold mb-6 text-center">
          Welcome to Period & Ovulation Predictor
        </h1>
        <a
          href="http://localhost:8080/oauth2/authorization/google"
          className="flex items-center justify-center px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors">
          <img 
            src="https://www.google.com/favicon.ico" 
            alt="Google" 
            className="w-5 h-5 mr-2"
          />
          Login with Google
        </a>
      </div>
    </div>
  );
};

// Dashboard component
interface DashboardProps {
  user: User;
}

const Dashboard: React.FC<DashboardProps> = ({ user }) => {
  // Will implement later
  return (
    <div>
      <h1>Welcome, {user.name}!</h1>
      {/* Add dashboard content */}
    </div>
  );
};

export default App;