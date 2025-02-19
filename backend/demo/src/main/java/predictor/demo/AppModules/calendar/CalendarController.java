package predictor.demo.AppModules.calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import predictor.demo.AppModules.eventData.EventData;
import predictor.demo.AppModules.eventData.EventDataError;
import predictor.demo.AppModules.eventData.EventDataServiceImp;
import predictor.demo.AppModules.eventsSeries.EventSeriesServiceImp;
import predictor.demo.AppModules.eventsSeries.EventsSeries;
import predictor.demo.AppModules.user.User;
import predictor.demo.AppModules.user.UserServiceImp;
import predictor.demo.Error.AppException;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/calendar")
@Slf4j
public class CalendarController {

    private final UserServiceImp userService;
    private final EventDataServiceImp eventDataService;
    private final GoogleCalendarService googleCalendarService;
    private final EventSeriesServiceImp eventsSeriesService;

    @Autowired
    public CalendarController(
            UserServiceImp userService,
            EventDataServiceImp eventDataService,
            GoogleCalendarService googleCalendarService,
            EventSeriesServiceImp eventsSeriesService) {
        this.userService = userService;
        this.eventDataService = eventDataService;
        this.googleCalendarService = googleCalendarService;
        this.eventsSeriesService = eventsSeriesService;
    }

    /**
     * Get all actual (non-predicted) period events for current user
     */
    @GetMapping("/periods")
    public ResponseEntity<List<EventData>> getUserPeriods(@AuthenticationPrincipal OAuth2User principal) {
        try {
            log.info("Fetching period events for user");
            User user = userService.getCurrentUser(principal);
            List<EventData> events = eventDataService.getAllUserActualEvents(user.getId());
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error fetching period events", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add a new period event and sync with Google Calendar
     */
    @PostMapping("/periods")
    public ResponseEntity<EventData> addPeriodEvent(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody Map<String, String> payload) {
        try {
            log.info("Adding new period event");
            User user = userService.getCurrentUser(principal);
            LocalDate eventDate = LocalDate.parse(payload.get("date"));

            // Create local event
            EventData newEvent = new EventData.EventDataBuilder()
                    .eventDate(eventDate)
                    .title("🌋Period🌋")
                    .user(user)
                    .isPeriodFirstDay(true)
                    .isPredicted(false)
                    .isSync(false)
                    .build();

            // Save to local DB
            EventData savedEvent = eventDataService.addEvent(newEvent);

            // Sync with Google Calendar
            googleCalendarService.addEventToGoogleCalendar(savedEvent);

            return ResponseEntity.ok(savedEvent);
        } catch (Exception e) {
            log.error("Error adding period event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all predicted events (periods and ovulation)
     */
    @GetMapping("/predictions")
    public ResponseEntity<List<EventData>> getPredictions(@AuthenticationPrincipal OAuth2User principal) {
        try {
            log.info("Fetching predictions");
            User user = userService.getCurrentUser(principal);
            List<EventData> predictions = eventDataService.getAllUserPredictedEvents(user.getId());
            return ResponseEntity.ok(predictions);
        } catch (Exception e) {
            log.error("Error fetching predictions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate new predictions based on the latest period
     */
    @PostMapping("/predictions/generate")
    public ResponseEntity<?> generatePredictions(
            @AuthenticationPrincipal OAuth2User principal) {
        try {
            log.info("Generating new predictions");
            User user = userService.getCurrentUser(principal);
            EventData latestPeriod = eventDataService.getLastPeriod(user.getId());
            if (latestPeriod == null) {
                return ResponseEntity
                    .badRequest()
                    .body("No recent period found to generate predictions");
            }

            EventsSeries newPrediction = this.eventsSeriesService.createNewEventsSeries(user);

            return ResponseEntity.ok("Predictions generated successfully");

        } catch (Exception e) {
            log.error("Error generating predictions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete specific event and remove from Google Calendar if synced
     */
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<?> deleteEvent(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable int eventId) {
        try {
            log.info("Deleting event: {}", eventId);
            User user = userService.getCurrentUser(principal);
            EventData event = eventDataService.getSingleEvent(eventId);

            // Verify the event belongs to the user
            if (event.getUser().getId() != user.getId()) {
                log.warn("Unauthorized attempt to delete event: {}", eventId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // If event was synced with Google Calendar, delete it there first
            if (event.isSync()) {
                googleCalendarService.deleteEventFromGoogleCalendar(event.getCalendarEventId());
            }

            // Delete from local database
            eventDataService.deleteEvent(eventId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting event: {}", eventId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user's cycle statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@AuthenticationPrincipal OAuth2User principal) {
        try {
            log.info("Fetching user stats");
            User user = userService.getCurrentUser(principal);
            
            double cycleLength = eventsSeriesService.calculateCycleLength(user);
            long eventCount = eventDataService.countUserActualEvents(user.getId());

            Map<String, Object> stats = Map.of(
                "averageCycleLength", cycleLength,
                "eventCount", eventCount
            );

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching user stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/series/{seriesId}/delete")
    public ResponseEntity<?> deleteEventSeries(@PathVariable int seriesId) throws AppException {
        eventsSeriesService.deleteEventSeries(seriesId);
        return ResponseEntity.ok().build();
    }
}