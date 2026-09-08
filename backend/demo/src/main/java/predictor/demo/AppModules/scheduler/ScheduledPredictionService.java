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

    // Runs every Monday and Thursday at 18:00
    @Scheduled(cron = "0 0 18 * * MON,THU")
    public void checkAndConfirmPeriods() {
        LocalDate today = LocalDate.now();
        log.info("Period confirmation scheduler running for week ending {}", today);

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
        // Look back to the start of the current ovulation window — no point scanning the calendar further than that
        LocalDate lookbackStart = eventDataRepository.findEarliestOvulationPredictionDate(user.getId());
        if (lookbackStart == null) {
            log.info("No ovulation predictions for user {} — skipping", user.getEmail());
            return;
        }

        Calendar userCalendar = calendarClientFactory.buildForUser(user.getRefreshToken());

        // Source of truth is the user's calendar, not our predicted dates — they may mark the
        // 🌋 on a day we didn't predict
        Event volcanoEvent = findVolcanoEvent(userCalendar, lookbackStart, today);
        if (volcanoEvent == null) {
            log.info("No 🌋 found for user {} between {} and {}", user.getEmail(), lookbackStart, today);
            return;
        }

        LocalDate date = eventDate(volcanoEvent);
        log.info("🌋 found on {} — confirming period for user {}", date, user.getEmail());

        // Rename the marker in place instead of deleting it and creating a new event
        volcanoEvent.setSummary("🌋Period🌋");
        volcanoEvent.setDescription("Period tracking event");
        Event confirmedEvent = userCalendar.events().update("primary", volcanoEvent.getId(), volcanoEvent).execute();

        // Record actual period in DB, linked to the renamed calendar event
        EventData actualPeriod = new EventData.EventDataBuilder()
            .eventDate(date)
            .title("🌋Period🌋")
            .user(user)
            .isPeriodFirstDay(true)
            .isPredicted(false)
            .isSync(true)
            .build();
        EventData saved = eventDataServiceImp.addEvent(actualPeriod);
        saved.setCalendarEventId(confirmedEvent.getId());
        eventDataServiceImp.updateEvent(saved, saved.getId());

        // Regenerate predictions automatically
        try {
            eventSeriesServiceImp.createNewEventsSeries(user, userCalendar);
            log.info("Predictions regenerated for user {}", user.getEmail());
        } catch (AppException e) {
            log.warn("Not enough data to regenerate predictions for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private Event findVolcanoEvent(Calendar calendar, LocalDate from, LocalDate to) throws IOException {
        DateTime timeMin = new DateTime(from + "T00:00:00Z");
        DateTime timeMax = new DateTime(to.plusDays(1) + "T00:00:00Z");

        Events events = calendar.events().list("primary")
            .setTimeMin(timeMin)
            .setTimeMax(timeMax)
            .setSingleEvents(true)
            .setOrderBy("startTime")
            .execute();

        if (events.getItems() == null) return null;

        return events.getItems().stream()
            .filter(e -> "🌋".equals(e.getSummary()))
            .findFirst()
            .orElse(null);
    }

    private LocalDate eventDate(Event event) {
        DateTime date = event.getStart().getDate();
        String rfc3339 = date != null ? date.toStringRfc3339() : event.getStart().getDateTime().toStringRfc3339();
        return LocalDate.parse(rfc3339.substring(0, 10));
    }
}
