package predictor.demo.AppModules.eventsSeries;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        if(countUserActualEvents < 2L){
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
public void deleteEventSeries(int eventSeriesId) throws AppException {
    try {
        // Get the series first
        EventsSeries series = getSingleEventsSeries(eventSeriesId);
        if (series == null) {
            throw new AppException(EventsSeriesError.EVENTS_SERIES_NOT_FOUND);
        }

        // Extract Google Calendar event IDs
        List<String> calendarEventIds = series.getPredictedEvents().stream()
                .map(EventData::getCalendarEventId)  // Changed from getGoogleCalendarEventId
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Delete from Google Calendar first
        if (!calendarEventIds.isEmpty()) {
            try {
                googleCalendarService.deleteCalendarEvents(calendarEventIds);
            } catch (Exception e) {
                log.error("Failed to delete some Google Calendar events", e);
                // Continue with database deletion even if Google Calendar deletion failed
            }
        }

        // Delete from database
        eventsSeriesRepository.deleteById(eventSeriesId);
        log.info("Successfully deleted event series: {}", eventSeriesId);
        
    } catch (Exception e) {
        log.error("Error deleting event series: {}", eventSeriesId, e);
        throw new AppException(EventsSeriesError.EVENTS_SERIES_NOT_DELETED);
    }
}

    @Override
    public void deleteAllPredictions(EventsSeries eventSeries) throws AppException {
        // Delete from Google Calendar
        List<EventData> predictions = eventServiceImp.getAllUserPredictedEvents(eventSeries.getUser().getId());
        List<String> calendarEventIds = predictions.stream()
                .map(EventData::getCalendarEventId)
                .filter(id -> id != null)
                .toList();
        googleCalendarService.deleteCalendarEvents(calendarEventIds);
        // Delete from database
        eventServiceImp.deleteAllPredictedEventsByUser(eventSeries.getUser().getId());
    }

    @Override
    public EventsSeries createNewEventsSeries(User user, EventData periodStart) throws AppException {
        if(periodStart.isPredicted()){
            throw new AppException(EventsSeriesError.EVENTS_SERIES_NO_NEW_ACTUAL_EVENTS);
        }

        // If user exists, delete old predictions from both Calendar and DB
        if(this.eventsSeriesRepository.existsByUserId(user.getId())){
            EventsSeries existingSeries = this.eventsSeriesRepository.findByUserId(user.getId());
            deleteAllPredictions(existingSeries);
        }

        // Create new eventSeries
        EventsSeries eventsSeries = new EventsSeries.Builder()
                .setUser(user)
                .setCalculatedCycleLength(this.calculateCycleLength(user))
                .setPredictionDate(periodStart.getEventDate())
                .build();

        // Add new predictions and sync with calendar
        eventsSeries = this.predictionPeriodOvulation(eventsSeries);

        return eventsSeries;
    }

    public EventsSeries createFirstNewEventsSeries(User user) throws AppException {
        double cycleLength = this.eventServiceImp.calculateCycleLength(user.getId());
        EventData lastPeriod = this.eventServiceImp.getLastPeriod(user.getId());
        
        // Create new eventSeries
        EventsSeries eventsSeries = new EventsSeries.Builder()
                .setUser(user)
                .setCalculatedCycleLength(cycleLength)
                .setPredictionDate(lastPeriod.getEventDate())
                .build();

        // Add new predictions and sync with calendar
        eventsSeries = this.predictionPeriodOvulation(eventsSeries);

        return eventsSeries;
    }

    public EventsSeries predictionPeriodOvulation(EventsSeries eventsSeries) throws AppException {
        EventData lastPeriod = this.eventServiceImp.getLastPeriod(eventsSeries.getUser().getId());
        LocalDate ovulation = lastPeriod.getEventDate().plusDays((int) eventsSeries.getCalculatedCycleLength()/2);
        LocalDate ovulationStart = ovulation.minusDays(4);
        LocalDate ovulationEnd = ovulation.plusDays(2);
        LocalDate nextPeriod = lastPeriod.getEventDate().plusDays((int) eventsSeries.getCalculatedCycleLength());

        List<EventData> prediction = new ArrayList<>();

        // Add next 12 periods prediction
        for(long i = 0; i < 12; i++ ){
            EventData periodsPrediction = new EventData.EventDataBuilder()
                    .eventDate(nextPeriod.plusDays((i+1) * (int) eventsSeries.getCalculatedCycleLength()))
                    .title("🌋Period-Prediction🌋")
                    .user(eventsSeries.getUser())
                    .isPeriodFirstDay(true)
                    .isPredicted(true)
                    .isSync(false)
                    .eventsSeries(eventsSeries)
                    .build();
            EventData savedEvent = this.eventServiceImp.addEvent(periodsPrediction);
            prediction.add(savedEvent);
        }

        // Add ovulation prediction
        long daysToAdd = ChronoUnit.DAYS.between(ovulationStart, ovulationEnd);
        for(long i = 0; i < daysToAdd; i++ ){
            EventData ovulationPrediction = new EventData.EventDataBuilder()
                    .eventDate(ovulationStart.plusDays(i))
                    .title("⚠️Ovulation-Prediction⚠️")
                    .user(eventsSeries.getUser())
                    .isPeriodFirstDay(false)
                    .isPredicted(true)
                    .isSync(false)
                    .eventsSeries(eventsSeries)
                    .build();
            EventData savedEvent = this.eventServiceImp.addEvent(ovulationPrediction);
            prediction.add(savedEvent);
        }

        // Sync all predictions with Google Calendar
        googleCalendarService.batchSyncEvents(prediction); // If calendar sync fails, we should delete the predictions from our database

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
        };
    }
