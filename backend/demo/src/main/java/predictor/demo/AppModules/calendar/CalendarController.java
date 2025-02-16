package predictor.demo.AppModules.calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import predictor.demo.AppModules.eventData.EventData;
import predictor.demo.AppModules.eventData.EventDataService;
import predictor.demo.AppModules.eventsSeries.EventsSeriesService;
import predictor.demo.AppModules.user.User;
import predictor.demo.AppModules.user.UserService;
import predictor.demo.Error.AppException;

@RestController
@RequestMapping("/api/calendar")
@CrossOrigin(origins = "http://localhost:3000") // For React frontend
public class CalendarController {
    private static final Logger logger = LoggerFactory.getLogger(CalendarController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private EventDataService eventDataService;
    @Autowired
    private EventsSeriesService eventsSeriesService;

    // Get all actual (non-predicted) period events for current user
    @GetMapping("/periods")
    public ResponseEntity<List<EventData>> getUserPeriods(@AuthenticationPrincipal OAuth2User principal)
            throws AppException {
        User user = userService.getUserByGoogleSubject(principal.getAttribute("sub"));
        List<EventData> actualEvents = eventDataService.getAllUserActualEvents(user.getId());
        return ResponseEntity.ok(actualEvents);
    }

    // Add a new period event
    @PostMapping("/periods")
    public ResponseEntity<EventData> addPeriodEvent(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody Map<String, String> payload) throws AppException {
        User user = userService.getUserByGoogleSubject(principal.getAttribute("sub"));
        LocalDate eventDate = LocalDate.parse(payload.get("date"));

        EventData newEvent = new EventData.EventDataBuilder()
                .eventDate(eventDate)
                .title("🌋Period🌋")
                .user(user)
                .isPeriodFirstDay(true)
                .isPredicted(false)
                .isSync(false)
                .build();

        EventData savedEvent = eventDataService.addEvent(newEvent);
        return ResponseEntity.ok(savedEvent);
    }

    // Get all predicted events (periods and ovulation)
    @GetMapping("/predictions")
    public ResponseEntity<List<EventData>> getPredictions(@AuthenticationPrincipal OAuth2User principal)
            throws AppException {
        User user = userService.getUserByGoogleSubject(principal.getAttribute("sub"));
        List<EventData> predictions = eventDataService.getAllUserPredictedEvents(user.getId());
        return ResponseEntity.ok(predictions);
    }

    // Generate new predictions based on the latest period
    @PostMapping("/predictions/generate")
    public ResponseEntity<?> generatePredictions(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody Map<String, String> payload) throws AppException {
        User user = userService.getUserByGoogleSubject(principal.getAttribute("sub"));
        LocalDate latestPeriodDate = LocalDate.parse(payload.get("latestPeriodDate"));

        // Create event for the latest period
        EventData latestPeriod = new EventData.EventDataBuilder()
                .eventDate(latestPeriodDate)
                .title("Period")
                .user(user)
                .isPeriodFirstDay(true)
                .isPredicted(false)
                .isSync(false)
                .build();

        // Generate new predictions
        eventsSeriesService.createNewEventsSeries(user, latestPeriod);

        return ResponseEntity.ok().build();
    }

    // Delete specific event
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<?> deleteEvent(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable int eventId) throws AppException {
        User user = userService.getUserByGoogleSubject(principal.getAttribute("sub"));
        EventData event = eventDataService.getSingleEvent(eventId);

        // Verify the event belongs to the user
        if (event.getUser().getId() != user.getId()) {
            return ResponseEntity.status(403).build();
        }

        eventDataService.deleteEvent(eventId);
        return ResponseEntity.ok().build();
    }

    // Get user's cycle statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@AuthenticationPrincipal OAuth2User principal)
            throws AppException {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).build();
            }

            String googleSubject = principal.getAttribute("sub");
            if (googleSubject == null) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Missing Google subject"
                ));
            }

            User user = userService.getUserByGoogleSubject(googleSubject);
            double cycleLength = eventsSeriesService.calculateCycleLength(user);

            Map<String, Object> stats = Map.of(
                "averageCycleLength", cycleLength,
                "eventCount", eventDataService.countUserActualEvents(user.getId())
            );

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting user stats", e);
            throw e;
        }
    }
}