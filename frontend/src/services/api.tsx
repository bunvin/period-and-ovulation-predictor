import axios from 'axios';
import { User, CalendarStats, EventData } from '../types';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
});

export const userService = {
  getCurrentUser: () => api.get<User>('/api/users/me'),
  getStats: () => api.get<CalendarStats>('/api/calendar/stats')
};

export const calendarService = {
  getPeriods: () => api.get<EventData[]>('/api/calendar/periods'),
  addPeriod: (date: string) => api.post<EventData>('/api/calendar/periods', { date }),
  getPredictions: () => api.get<EventData[]>('/api/calendar/predictions'),
  generatePredictions: (latestPeriodDate: string) => 
    api.post('/api/calendar/predictions/generate', { latestPeriodDate }),
  deleteEvent: (eventId: number) => api.delete(`/api/calendar/events/${eventId}`)
};