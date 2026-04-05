package predictor.demo.AppModules.scheduler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import lombok.extern.slf4j.Slf4j;
import predictor.demo.AppModules.calendar.UserCalendarClientFactory;
import predictor.demo.AppModules.eventData.EventData;
import predictor.demo.AppModules.eventData.EventDataRepository;
import predictor.demo.AppModules.eventData.EventDataServiceImp;
import predictor.demo.AppModules.eventsSeries.EventSeriesServiceImp;
import predictor.demo.AppModules.user.User;
import predictor.demo.AppModules.user.UserServiceImp;
import predictor.demo.Error.AppException;

@Service
@Slf4j
public class ScheduledPredictionService {

    @Autowired private UserServiceImp userServiceImp;
    @Autowired private EventDataRepository eventDataRepository;
    @Autowired private EventDataServiceImp eventDataServiceImp;
    @Autowired private EventSeriesServiceImp eventSeriesServiceImp;
    @Autowired private UserCalendarClientFactory calendarClientFactory;

    @Scheduled(cron = "0 0 8 * * *")
    public void checkAndConfirmPeriods() {
        LocalDate today = LocalDate.now();
        log.info("Period confirmation scheduler running for {}", today);

        List<User> users = userServiceImp.getAllUsers();
        for (User user : users) {
            if (!user.isActive() || user.getRefreshToken() == null) continue;
            try {
                processUser(user, today);
            } catch (Exception e) {
                log.error("Scheduler failed for user {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    private void processUser(User user, LocalDate today) throws Exception {
        EventData predicted = eventDataRepository.findPredictedPeriodFirstDayForDate(user.getId(), today);
        if (predicted == null) return;

        log.info("User {} has a predicted period today — checking calendar", user.getEmail());

        Calendar userCalendar = calendarClientFactory.buildForUser(user.getRefreshToken());
        String volcanoEventId = findVolcanoEvent(userCalendar, today);

        if (volcanoEventId == null) {
            log.info("No 🌋🔥 marker found for user {}", user.getEmail());
            return;
        }

        log.info("🌋🔥 event found — confirming period for user {}", user.getEmail());

        // Delete the 🌋🔥 marker
        userCalendar.events().delete("primary", volcanoEventId).execute();

        // Record actual period in DB
        EventData actualPeriod = new EventData.EventDataBuilder()
            .eventDate(today)
            .title("🌋Period🌋")
            .user(user)
            .isPeriodFirstDay(true)
            .isPredicted(false)
            .isSync(false)
            .build();
        EventData saved = eventDataServiceImp.addEvent(actualPeriod);

        // Add actual period to Google Calendar
        Event periodEvent = new Event()
            .setSummary(saved.getTitle())
            .setDescription("Period tracking event")
            .setStart(new EventDateTime()
                .setDate(new DateTime(today.toString()))
                .setTimeZone("UTC"))
            .setEnd(new EventDateTime()
                .setDate(new DateTime(today.plusDays(1).toString()))
                .setTimeZone("UTC"));
        Event created = userCalendar.events().insert("primary", periodEvent).execute();
        saved.setCalendarEventId(created.getId());
        saved.setSync(true);
        eventDataServiceImp.updateEvent(saved, saved.getId());

        // Regenerate predictions
        try {
            eventSeriesServiceImp.createNewEventsSeries(user, userCalendar);
            log.info("Predictions regenerated for user {}", user.getEmail());
        } catch (AppException e) {
            log.warn("Not enough data to regenerate predictions for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private String findVolcanoEvent(Calendar calendar, LocalDate date) throws IOException {
        DateTime timeMin = new DateTime(date + "T00:00:00Z");
        DateTime timeMax = new DateTime(date.plusDays(1) + "T00:00:00Z");

        Events events = calendar.events().list("primary")
            .setTimeMin(timeMin)
            .setTimeMax(timeMax)
            .setSingleEvents(true)
            .execute();

        if (events.getItems() == null) return null;

        return events.getItems().stream()
            .filter(e -> "🌋🔥".equals(e.getSummary()))
            .findFirst()
            .map(Event::getId)
            .orElse(null);
    }
}
