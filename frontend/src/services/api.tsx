import axios from 'axios';
import { User, CalendarStats, EventData } from '../types';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/calendar',
  withCredentials: true,
});

export const userService = {
  getPeriods: () => api.get<EventData[]>('/periods')
};

export const calendarService = {
  getStats: () => api.get<CalendarStats>('/stats'),
  addPeriod: (date: string) => api.post<EventData>('/api/calendar/periods', { date }),
  getPredictions: () => api.get<EventData[]>('/api/calendar/predictions'),
  generatePredictions: (latestPeriodDate: string) => 
    api.post('/api/calendar/predictions/generate', { latestPeriodDate }),
  deleteEvent: (eventId: number) => api.delete(`/api/calendar/events/${eventId}`)
};