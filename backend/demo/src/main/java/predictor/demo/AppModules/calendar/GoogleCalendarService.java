package predictor.demo.AppModules.calendar;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import predictor.demo.AppModules.eventData.EventData;
import predictor.demo.AppModules.eventData.EventDataServiceImp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GoogleCalendarService {
    
    private final Calendar googleCalendar;
    private final EventDataServiceImp eventDataService;

    public GoogleCalendarService(Calendar googleCalendar, EventDataServiceImp eventDataService) {
        this.googleCalendar = googleCalendar;
        this.eventDataService = eventDataService;
    }

    public Event addEventToGoogleCalendar(EventData eventData) throws Exception {
        try {
            Event googleEvent = new Event()
                .setSummary(eventData.getTitle())
                .setDescription("Period tracking event")
                .setStart(new EventDateTime()
                    .setDate(new DateTime(eventData.getEventDate().toString()))
                    .setTimeZone("UTC"))
                .setEnd(new EventDateTime()
                    .setDate(new DateTime(eventData.getEventDate().plusDays(1).toString()))
                    .setTimeZone("UTC"));

            Event createdEvent = googleCalendar.events().insert("primary", googleEvent).execute();
            
            // Update sync status in local database
            eventData.setSync(true);
            eventDataService.updateEvent(eventData, eventData.getId());
            
            return createdEvent;
        } catch (IOException e) {
            log.error("Failed to create Google Calendar event", e);
            throw new Exception("Failed to sync with Google Calendar", e);
        }
    }

    public void deleteEventFromGoogleCalendar(String eventId) throws Exception {
        try {
            if (eventId != null) {
                googleCalendar.events().delete("primary", eventId).execute();
                log.info("Deleted Google Calendar event: {}", eventId);
            }
        } catch (IOException e) {
            log.error("Failed to delete Google Calendar event", e);
            throw new Exception("Failed to delete from Google Calendar", e);
        }
    }

    public void deleteEventsFromGoogleCalendar(List<String> eventIds) throws Exception {
        if (eventIds == null || eventIds.isEmpty()) {
            log.info("No Google Calendar events to delete");
            return;
        }

        List<Exception> errors = new ArrayList<>();
        
        for (String eventId : eventIds) {
            try {
                if (eventId != null) {
                    googleCalendar.events().delete("primary", eventId).execute();
                    log.info("Deleted Google Calendar event: {}", eventId);
                }
            } catch (IOException e) {
                log.error("Failed to delete Google Calendar event: {}", eventId, e);
                errors.add(new Exception("Failed to delete event: " + eventId, e));
            }
        }

        if (!errors.isEmpty()) {
            throw new Exception("Failed to delete some Google Calendar events: " + 
                errors.size() + " errors occurred");
        }
    }

    public void batchSyncEvents(List<EventData> events) {
        if (events == null || events.isEmpty()) {
            log.info("No events to sync with Google Calendar");
            return;
        }

        List<Exception> errors = new ArrayList<>();
        
        for (EventData event : events) {
            try {
                Event googleEvent = new Event()
                    .setSummary(event.getTitle())
                    .setDescription("Period tracking event")
                    .setStart(new EventDateTime()
                        .setDate(new DateTime(event.getEventDate().toString()))
                        .setTimeZone("UTC"))
                    .setEnd(new EventDateTime()
                        .setDate(new DateTime(event.getEventDate().plusDays(1).toString()))
                        .setTimeZone("UTC"));

                Event createdEvent = googleCalendar.events().insert("primary", googleEvent).execute();
                
                // Update the event with the Google Calendar ID
                event.setCalendarEventId(createdEvent.getId());
                event.setSync(true);
                
                log.info("Successfully synced event: {} to Google Calendar", event.getTitle());
            } catch (IOException e) {
                log.error("Failed to sync event to Google Calendar: {}", event.getTitle(), e);
                errors.add(e);
            }
        }

        if (!errors.isEmpty()) {
            log.error("Failed to sync {} events with Google Calendar", errors.size());
            // You might want to handle these errors appropriately
        }
    }

    public void deleteCalendarEvents(List<String> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            log.info("No calendar events to delete");
            return;
        }

        List<Exception> errors = new ArrayList<>();
        
        for (String eventId : eventIds) {
            try {
                googleCalendar.events().delete("primary", eventId).execute();
                log.info("Successfully deleted event from Google Calendar: {}", eventId);
            } catch (IOException e) {
                log.error("Failed to delete event from Google Calendar: {}", eventId, e);
                errors.add(e);
            }
        }

        if (!errors.isEmpty()) {
            log.error("Failed to delete {} events from Google Calendar", errors.size());
        }
    }

}