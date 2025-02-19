import './EventList.css';
import React, { useState, useEffect } from 'react';
import { EventData } from '../types';
import { userService, calendarService } from '../services/api';
import axios from 'axios';

const EventList: React.FC = () => {
  const [events, setEvents] = useState<EventData[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async (): Promise<void> => {
    try {
      const response = await userService.getPeriods();
      setEvents(response.data);
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const errorMessage = err.response?.data?.message || 
                           err.response?.data?.error || 
                           'Failed to fetch events';
        setError(errorMessage);
      } else {
        setError('Failed to fetch events');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (eventId: number): Promise<void> => {
    try {
      await calendarService.deleteEvent(eventId);
      await fetchEvents(); // Refresh the list
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const errorMessage = err.response?.data?.message || 
                           err.response?.data?.error ||
                           'Failed to delete event';
        setError(errorMessage);
      } else {
        setError('Failed to delete event');
      }
    }
  };

  if (loading) {
    return <div>Loading events...</div>;
  }

  if (error) {
    return <div className="error-message">{error}</div>;
  }

  return (
    <div className="event-list">
      <h2>Your Period Events</h2>
      {events.length === 0 ? (
        <p>No events found. <br /> Add your first period event!</p>
      ) : (
        <div className="events-container">
          {events.map((event) => (
            <div key={event.id} className="event-item">
              <span className="event-date">
                {new Date(event.eventDate).toLocaleDateString()}
              </span>
              {!event.isPredicted && (
                <button
                  onClick={() => handleDelete(event.id)}
                  className="delete-button"
                >
                  Delete
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default EventList;