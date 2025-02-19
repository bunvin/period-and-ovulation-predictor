package predictor.demo.AppModules.eventsSeries;
import predictor.demo.AppModules.user.User;
import predictor.demo.Error.AppException;

public interface EventsSeriesService {
    EventsSeries addEventSeries(EventsSeries eventsSeries) throws AppException;
    EventsSeries getSingleEventsSeries(int eventSeriesId) throws AppException;
    void updateEventSeries(EventsSeries eventsSeries);
    void deleteEventSeries(int eventSeriesId) throws AppException;

    void deleteAllPredictions(EventsSeries eventSeries) throws AppException;

    EventsSeries predictionPeriodOvulation(EventsSeries eventsSeries) throws AppException, Exception;
    EventsSeries createNewEventsSeries(User user) throws AppException, Exception;     
    double calculateCycleLength(User user);
    boolean isUserExist(int userId);
}
