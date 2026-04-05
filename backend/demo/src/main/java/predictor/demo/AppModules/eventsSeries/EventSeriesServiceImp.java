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
            deleteEventSeries(existing.getId(), calendar);
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
        LocalDate ovulation = lastPeriod.getEventDate().plusDays((int) eventsSeries.getCalculatedCycleLength() / 2);
        LocalDate ovulationStart = ovulation.minusDays(4);
        LocalDate ovulationEnd = ovulation.plusDays(2);

        List<EventData> prediction = new ArrayList<>();

        // Add next 6 periods prediction
        for (long i = 0; i < 6; i++) {
            EventData periodsPrediction = new EventData.EventDataBuilder()
                    .eventDate(lastPeriod.getEventDate().plusDays((i + 1) * (int) eventsSeries.getCalculatedCycleLength()))
                    .title("🌋Period-Prediction🌋")
                    .user(eventsSeries.getUser())
                    .isPeriodFirstDay(true)
                    .isPredicted(true)
                    .isSync(false)
                    .eventsSeries(eventsSeries)
                    .build();
            prediction.add(this.eventServiceImp.addEvent(periodsPrediction));
        }

        // Add ovulation prediction
        long daysToAdd = ChronoUnit.DAYS.between(ovulationStart, ovulationEnd);
        for (long i = 0; i < daysToAdd; i++) {
            EventData ovulationPrediction = new EventData.EventDataBuilder()
                    .eventDate(ovulationStart.plusDays(i))
                    .title("⚠️Ovulation-Prediction⚠️")
                    .user(eventsSeries.getUser())
                    .isPeriodFirstDay(false)
                    .isPredicted(true)
                    .isSync(false)
                    .eventsSeries(eventsSeries)
                    .build();
            prediction.add(this.eventServiceImp.addEvent(ovulationPrediction));
        }

        // Sync all predictions with Google Calendar
        googleCalendarService.batchSyncEvents(prediction, calendar);
        eventsSeries.setPredictedEvents(prediction);
        return this.eventsSeriesRepository.save(eventsSeries);
    }

    @Override
    public double calculateCycleLength(User user) {
        return this.eventsSeriesRepository.findTop4EventsByUserIdWithAverageCycleLength(user.getId());
    }

    @Override
    public boolean isUserExist(int userId) {
        return this.eventsSeriesRepository.existsByUserId(userId);
    }
}
