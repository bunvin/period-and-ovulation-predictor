package predictor.demo.AppModules.calendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import lombok.extern.slf4j.Slf4j;
import predictor.demo.AppModules.eventData.EventData;
import predictor.demo.AppModules.eventData.EventDataServiceImp;

@Service
@Slf4j
public class GoogleCalendarService {

    private final EventDataServiceImp eventDataService;

    public GoogleCalendarService(EventDataServiceImp eventDataService) {
        this.eventDataService = eventDataService;
    }

    public Event addEventToGoogleCalendar(EventData eventData, Calendar calendar) throws Exception {
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

            Event createdEvent = calendar.events().insert("primary", googleEvent).execute();

            eventData.setSync(true);
            eventDataService.updateEvent(eventData, eventData.getId());

            return createdEvent;
        } catch (IOException e) {
            log.error("Failed to create Google Calendar event", e);
            throw new Exception("Failed to sync with Google Calendar", e);
        }
    }

    public void deleteEventFromGoogleCalendar(String eventId, Calendar calendar) throws Exception {
        try {
            if (eventId != null) {
                calendar.events().delete("primary", eventId).execute();
                log.info("Deleted Google Calendar event: {}", eventId);
            }
        } catch (IOException e) {
            log.error("Failed to delete Google Calendar event", e);
            throw new Exception("Failed to delete from Google Calendar", e);
        }
    }

    public void deleteEventsFromGoogleCalendar(List<String> eventIds, Calendar calendar) throws Exception {
        if (eventIds == null || eventIds.isEmpty()) return;

        List<Exception> errors = new ArrayList<>();
        for (String eventId : eventIds) {
            try {
                if (eventId != null) {
                    calendar.events().delete("primary", eventId).execute();
                    log.info("Deleted Google Calendar event: {}", eventId);
                }
            } catch (IOException e) {
                log.error("Failed to delete Google Calendar event: {}", eventId, e);
                errors.add(new Exception("Failed to delete event: " + eventId, e));
            }
        }
        if (!errors.isEmpty()) {
            throw new Exception("Failed to delete some Google Calendar events: " + errors.size() + " errors occurred");
        }
    }

    public void batchSyncEvents(List<EventData> events, Calendar calendar) {
        if (events == null || events.isEmpty()) return;

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

                Event createdEvent = calendar.events().insert("primary", googleEvent).execute();
                event.setCalendarEventId(createdEvent.getId());
                event.setSync(true);
                log.info("Synced event {} to Google Calendar", event.getTitle());
            } catch (IOException e) {
                log.error("Failed to sync event to Google Calendar: {}", event.getTitle(), e);
            }
        }
    }

    public void deleteCalendarEvents(List<String> eventIds, Calendar calendar) {
        if (eventIds == null || eventIds.isEmpty()) return;

        for (String eventId : eventIds) {
            try {
                calendar.events().delete("primary", eventId).execute();
                log.info("Deleted calendar event: {}", eventId);
            } catch (IOException e) {
                log.error("Failed to delete calendar event {}: {}", eventId, e.getMessage());
            }
        }
    }
}
