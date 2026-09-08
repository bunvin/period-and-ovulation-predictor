package predictor.demo.AppModules.eventsSeries;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.services.calendar.Calendar;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import predictor.demo.AppModules.calendar.GoogleCalendarService;
import predictor.demo.AppModules.eventData.EventData;
import predictor.demo.AppModules.eventData.EventDataServiceImp;
import predictor.demo.AppModules.user.User;
import predictor.demo.Error.AppException;

@Service
@Slf4j
public class EventSeriesServiceImp implements EventsSeriesService {
    @Autowired
    private EventsSeriesRepository eventsSeriesRepository;
    @Autowired
    private EventDataServiceImp eventServiceImp;
    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Override
    public EventsSeries addEventSeries(EventsSeries eventsSeries) throws AppException {
        long countUserActualEvents = this.eventServiceImp.countUserActualEvents(eventsSeries.getUser().getId());
        if (countUserActualEvents < 2L) {
            throw new AppException(EventsSeriesError.EVENTS_SERIES_NOT_ENOUGH_ACTUAL_EVENTS);
        }
        return this.eventsSeriesRepository.save(eventsSeries);
    }

    @Override
    public EventsSeries getSingleEventsSeries(int eventSeriesId) throws AppException {
        return this.eventsSeriesRepository.findById(eventSeriesId)
                .orElseThrow(() -> new AppException(EventsSeriesError.EVENTS_SERIES_NOT_FOUND));
    }

    @Override
    public void updateEventSeries(EventsSeries eventsSeries) {
        this.eventsSeriesRepository.save(eventsSeries);
    }

    @Override
    @Transactional
    public void deleteEventSeries(int eventSeriesId, Calendar calendar) throws AppException {
        try {
            EventsSeries series = getSingleEventsSeries(eventSeriesId);
            List<String> calendarEventIds = series.getPredictedEvents().stream()
                    .map(EventData::getCalendarEventId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!calendarEventIds.isEmpty()) {
                googleCalendarService.deleteCalendarEvents(calendarEventIds, calendar);
            }

            eventsSeriesRepository.deleteById(eventSeriesId);
            log.info("Deleted event series: {}", eventSeriesId);
        } catch (Exception e) {
            log.error("Error deleting event series: {}", eventSeriesId, e);
            throw new AppException(EventsSeriesError.EVENTS_SERIES_NOT_DELETED);
        }
    }

    @Override
    public void deleteAllPredictions(EventsSeries eventSeries, Calendar calendar) throws AppException {
        List<EventData> predictions = eventServiceImp.getAllUserPredictedEvents(eventSeries.getUser().getId());
        List<String> calendarEventIds = predictions.stream()
                .map(EventData::getCalendarEventId)
                .filter(id -> id != null)
                .toList();
        googleCalendarService.deleteCalendarEvents(calendarEventIds, calendar);
        eventServiceImp.deleteAllPredictedEventsByUser(eventSeries.getUser().getId());
    }

    @Override
    public EventsSeries createNewEventsSeries(User user, Calendar calendar) throws Exception {
        if (this.eventsSeriesRepository.existsByUserId(user.getId())) {
            EventsSeries existing = this.eventsSeriesRepository.findByUserId(user.getId());
            return updateExistingEventsSeries(existing, calendar);
        }

        double cycleLength = this.eventServiceImp.calculateCycleLength(user.getId());
        EventData lastPeriod = this.eventServiceImp.getLastPeriod(user.getId());

        EventsSeries eventsSeries = new EventsSeries.Builder()
                .setUser(user)
                .setCalculatedCycleLength(cycleLength)
                .setPredictionDate(lastPeriod.getEventDate())
                .build();
        eventsSeries = this.addEventSeries(eventsSeries);

        return this.predictionPeriodOvulation(eventsSeries, calendar);
    }

    @Override
    public EventsSeries predictionPeriodOvulation(EventsSeries eventsSeries, Calendar calendar) throws Exception {
        EventData lastPeriod = this.eventServiceImp.getLastPeriod(eventsSeries.getUser().getId());
        List<PredictedEventSpec> specs = buildPredictionSpecs(lastPeriod, eventsSeries.getCalculatedCycleLength());

        List<EventData> prediction = new ArrayList<>();
        for (PredictedEventSpec spec : specs) {
            EventData event = new EventData.EventDataBuilder()
                    .eventDate(spec.date())
                    .title(spec.title())
                    .user(eventsSeries.getUser())
                    .isPeriodFirstDay(spec.isPeriodFirstDay())
                    .isPredicted(true)
                    .isSync(false)
                    .eventsSeries(eventsSeries)
                    .build();
            prediction.add(this.eventServiceImp.addEvent(event));
        }

        // Sync all predictions with Google Calendar
        googleCalendarService.batchSyncEvents(prediction, calendar);
        eventsSeries.setPredictedEvents(prediction);
        return this.eventsSeriesRepository.save(eventsSeries);
    }

    /**
     * Regenerates predictions for a series that already has predicted events, updating
     * existing Google Calendar events in place instead of deleting and recreating the
     * whole series. Only the count difference (if any) is added or removed.
     */
    private EventsSeries updateExistingEventsSeries(EventsSeries eventsSeries, Calendar calendar) throws Exception {
        double cycleLength = this.eventServiceImp.calculateCycleLength(eventsSeries.getUser().getId());
        EventData lastPeriod = this.eventServiceImp.getLastPeriod(eventsSeries.getUser().getId());

        eventsSeries.setCalculatedCycleLength(cycleLength);
        eventsSeries.setPredictionDate(lastPeriod.getEventDate());

        List<PredictedEventSpec> newSpecs = buildPredictionSpecs(lastPeriod, cycleLength);
        List<EventData> existingEvents = new ArrayList<>(eventsSeries.getPredictedEvents());
        int shared = Math.min(newSpecs.size(), existingEvents.size());

        List<EventData> updatedEvents = new ArrayList<>();

        // Update the events both lists have in common in place, rather than delete+recreate
        for (int i = 0; i < shared; i++) {
            EventData event = existingEvents.get(i);
            PredictedEventSpec spec = newSpecs.get(i);
            event.setTitle(spec.title());
            event.setEventDate(spec.date());
            event.setPeriodFirstDay(spec.isPeriodFirstDay());
            this.eventServiceImp.updateEvent(event, event.getId());

            if (event.isSync() && event.getCalendarEventId() != null) {
                googleCalendarService.updateEventInGoogleCalendar(event, calendar);
            } else {
                googleCalendarService.addEventToGoogleCalendar(event, calendar);
            }
            updatedEvents.add(event);
        }

        // Old series had more events than the new one: remove the extras
        for (int i = shared; i < existingEvents.size(); i++) {
            EventData event = existingEvents.get(i);
            if (event.getCalendarEventId() != null) {
                googleCalendarService.deleteEventFromGoogleCalendar(event.getCalendarEventId(), calendar);
            }
            this.eventServiceImp.deleteEvent(event.getId());
        }

        // New series has more events than the old one: create the extras
        List<EventData> newlyAdded = new ArrayList<>();
        for (int i = shared; i < newSpecs.size(); i++) {
            PredictedEventSpec spec = newSpecs.get(i);
            EventData event = new EventData.EventDataBuilder()
                    .eventDate(spec.date())
                    .title(spec.title())
                    .user(eventsSeries.getUser())
                    .isPeriodFirstDay(spec.isPeriodFirstDay())
                    .isPredicted(true)
                    .isSync(false)
                    .eventsSeries(eventsSeries)
                    .build();
            newlyAdded.add(this.eventServiceImp.addEvent(event));
        }
        if (!newlyAdded.isEmpty()) {
            googleCalendarService.batchSyncEvents(newlyAdded, calendar);
            updatedEvents.addAll(newlyAdded);
        }

        eventsSeries.setPredictedEvents(updatedEvents);
        return this.eventsSeriesRepository.save(eventsSeries);
    }

    private List<PredictedEventSpec> buildPredictionSpecs(EventData lastPeriod, double calculatedCycleLength) {
        LocalDate ovulation = lastPeriod.getEventDate().plusDays((int) calculatedCycleLength / 2);
        LocalDate ovulationStart = ovulation.minusDays(4);
        LocalDate ovulationEnd = ovulation.plusDays(2);

        List<PredictedEventSpec> specs = new ArrayList<>();

        // Next 6 period predictions
        for (long i = 0; i < 6; i++) {
            specs.add(new PredictedEventSpec(
                    lastPeriod.getEventDate().plusDays((i + 1) * (int) calculatedCycleLength),
                    "🌋Period-Prediction🌋",
                    true));
        }

        // Ovulation window predictions
        long daysToAdd = ChronoUnit.DAYS.between(ovulationStart, ovulationEnd);
        for (long i = 0; i < daysToAdd; i++) {
            specs.add(new PredictedEventSpec(ovulationStart.plusDays(i), "⚠️Ovulation-Prediction⚠️", false));
        }

        return specs;
    }

    private record PredictedEventSpec(LocalDate date, String title, boolean isPeriodFirstDay) {}

    @Override
    public double calculateCycleLength(User user) {
        return this.eventsSeriesRepository.findTop4EventsByUserIdWithAverageCycleLength(user.getId());
    }

    @Override
    public boolean isUserExist(int userId) {
        return this.eventsSeriesRepository.existsByUserId(userId);
    }
}
