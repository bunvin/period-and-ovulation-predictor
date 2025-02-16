export interface User {
    id: number;
    email: string;
    name: string;
    picture?: string;
  }
  
  export interface CalendarStats {
    averageCycleLength: number;
    eventCount: number;
  }
  
  export interface EventData {
    id: number;
    eventDate: string;
    title: string;
    isPeriodFirstDay: boolean;
    isPredicted: boolean;
    isSync: boolean;
    calendarEventId?: string;
  }