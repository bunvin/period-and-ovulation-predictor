package predictor.demo.AppModules.eventsSeries;

import predictor.demo.AppModules.eventData.EventData;
import predictor.demo.AppModules.user.User;
import predictor.demo.Error.AppException;

public interface EventsSeriesService {
    EventsSeries addEventSeries(EventsSeries eventsSeries) throws AppException;
    EventsSeries getSingleEventsSeries(int eventSeriesId) throws AppException;
    void updateEventSeries(EventsSeries eventsSeries);
    void deleteEventSeries(int eventSeriesId) throws AppException;

    void deleteAllPredictions(EventsSeries eventSeries) throws AppException;

    EventsSeries createNewEventsSeries(User user, EventData periodStart) throws AppException; //by last actual preduct and saveAll
    double calculateCycleLength(User user);
}
