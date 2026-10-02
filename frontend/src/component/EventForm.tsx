import React, { useState } from 'react';
import axios from 'axios';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import './EventForm.css';
import { calendarService, schedulerService } from '../services/api';

const EventForm: React.FC = () => {
  const [date, setDate] = useState<Date>(new Date());
  const [loading, setLoading] = useState<boolean>(false);
  const [predictionLoading, setPredictionLoading] = useState<boolean>(false);
  const [scanLoading, setScanLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);
  const [predictionSuccess, setPredictionSuccess] = useState<boolean>(false);
  const [scanSuccess, setScanSuccess] = useState<boolean>(false);

  const handleDateChange = (date: Date | null): void => {
    if (date) {
      setDate(date);
    }
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>): Promise<void> => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      await calendarService.addPeriod(date.toISOString().split('T')[0]);
      setDate(new Date());
      setSuccess(true);
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const errorMessage = err.response?.data?.message || 
                           err.response?.data?.error || 
                           'Failed to add period event';
        setError(errorMessage);
      } else {
        setError('An unexpected error occurred');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleGeneratePrediction = async (e: React.MouseEvent<HTMLButtonElement>): Promise<void> => {
    e.preventDefault();
    setPredictionLoading(true);
    setError(null);
    setPredictionSuccess(false);

    try {
        await calendarService.generatePredictions();
        
        setPredictionSuccess(true);
        setTimeout(() => setPredictionSuccess(false), 3000);
    } catch (err) {
        console.error(err); // Log the full error for debugging
        
        // Handle different error scenarios
        if (axios.isAxiosError(err)) {
            // Extract error message from the response
            const errorMessage = err.response?.data?.message || 
                                 err.response?.data?.error || 
                                 'Failed to generate predictions';
            setError(errorMessage);
        } else {
            setError('An unexpected error occurred');
        }
    } finally {
        setPredictionLoading(false);
    }
};

  const handleRunScanNow = async (e: React.MouseEvent<HTMLButtonElement>): Promise<void> => {
    e.preventDefault();
    setScanLoading(true);
    setError(null);
    setScanSuccess(false);

    try {
        await schedulerService.runNow();

        setScanSuccess(true);
        setTimeout(() => setScanSuccess(false), 3000);
    } catch (err) {
        console.error(err);

        if (axios.isAxiosError(err)) {
            const errorMessage = err.response?.data?.message ||
                                 err.response?.data?.error ||
                                 'Failed to run scheduler scan';
            setError(errorMessage);
        } else {
            setError('An unexpected error occurred');
        }
    } finally {
        setScanLoading(false);
    }
};

  return (
    <div className="event-form-container">
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className='period-start-label'>Period Start Date:</label>
          <DatePicker
            selected={date}
            onChange={handleDateChange}
            maxDate={new Date()}
            dateFormat="yyyy-MM-dd"
            className="date-picker"
            placeholderText="Select a date"
          />
          <button
            type="submit"
            disabled={loading}
            className="submit-button"
          >
            {loading ? 'Adding...' : 'Add Period'}
          </button>
        </div>
        
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">Event added successfully!</div>}
      </form>

      <button
        onClick={handleGeneratePrediction}
        disabled={predictionLoading}
        className="new-prediction-button"
      >
        {predictionLoading ? '✨ Generating... ✨' : '🔮✨ Add New Prediction ✨🔮'}
      </button>
      {predictionSuccess &&
        <div className="success-message prediction-success">
          ✨ New predictions generated successfully! ✨
        </div>
      }

      <br />
      <button
        onClick={handleRunScanNow}
        disabled={scanLoading}
        className="run-scan-button"
      >
        {scanLoading ? '🔍 Scanning... 🔍' : '🔍 Run Scan Now 🔍'}
      </button>
      {scanSuccess &&
        <div className="success-message scan-success">
          ✅ Scan triggered — check for a new confirmed period!
        </div>
      }
    </div>
  );
};

export default EventForm;