import React, { useState } from 'react';
import axios from 'axios';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import './EventForm.css';

const EventForm: React.FC = () => {
  const [date, setDate] = useState<Date>(new Date());
  const [loading, setLoading] = useState<boolean>(false);
  const [predictionLoading, setPredictionLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);
  const [predictionSuccess, setPredictionSuccess] = useState<boolean>(false);

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
      await axios.post('http://localhost:8080/api/calendar/periods', {
        date: date.toISOString().split('T')[0]
      }, {
        withCredentials: true
      });

      setDate(new Date());
      setSuccess(true);
    } catch (err) {
      setError('Failed to add period event');
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
      await axios.post('http://localhost:8080/api/calendar/predictions/generate', 
        {}, // Empty payload as backend gets latest period
        { withCredentials: true }
      );

      setPredictionSuccess(true);
      setTimeout(() => setPredictionSuccess(false), 3000); // Clear success message after 3 seconds
    } catch (err) {
      setError('Failed to generate predictions. Please make sure you have at least two period events.');
    } finally {
      setPredictionLoading(false);
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
    </div>
  );
};

export default EventForm;