package predictor.demo.AppModules.calendar;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;
import predictor.demo.AppModules.eventData.EventData;
import predictor.demo.Error.AppException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class GoogleCalendarService {
    private final OAuth2AuthorizedClientService clientService;

    public GoogleCalendarService(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    private Calendar getCalendarService() {
        return CalendarConfig.getCalendarService(clientService);
    }

    public EventData syncEventToCalendar(EventData eventData) throws IOException, AppException {
        //EventData LocalDate into Date object
        LocalDateTime startDateTime = eventData.getEventDate().atStartOfDay();
        Date startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        LocalDateTime endDateTime = startDateTime.plusDays(1);
        Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
        // Create EventDateTime for start and end
        EventDateTime startEventDateTime = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(startDate))
                .setTimeZone(ZoneId.systemDefault().getId());
        EventDateTime endEventDateTime = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(endDate))
                .setTimeZone(ZoneId.systemDefault().getId());

        // Create the Event object
        Event event = new Event()
                .setSummary(eventData.getTitle())
                .setDescription("Predicted: " + eventData.isPredicted())
                .setStart(startEventDateTime)
                .setEnd(endEventDateTime);

        getCalendarService().events().insert("primary", event).execute();
        eventData.setSync(true);  // Mark as synced
        return eventData; //updated and synced EventData
    }

    public void deleteCalendarEvents(List<String> eventIds) throws IOException {
        for (String eventId : eventIds) {
            getCalendarService().events().delete("primary", eventId).execute();
        }
    }

    public void batchSyncEvents(List<EventData> events) throws IOException, AppException {
        for (EventData event : events) {
            if (!event.isSync()) {
                syncEventToCalendar(event);
            }
        }
    }
}