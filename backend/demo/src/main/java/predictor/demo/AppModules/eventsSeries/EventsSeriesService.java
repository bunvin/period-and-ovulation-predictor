package predictor.demo.AppModules.eventsSeries;

import com.google.api.services.calendar.Calendar;
import predictor.demo.AppModules.user.User;
import predictor.demo.Error.AppException;

public interface EventsSeriesService {
    EventsSeries addEventSeries(EventsSeries eventsSeries) throws AppException;
    EventsSeries getSingleEventsSeries(int eventSeriesId) throws AppException;
    void updateEventSeries(EventsSeries eventsSeries);
    void deleteEventSeries(int eventSeriesId, Calendar calendar) throws AppException;

    void deleteAllPredictions(EventsSeries eventSeries, Calendar calendar) throws AppException;

    EventsSeries predictionPeriodOvulation(EventsSeries eventsSeries, Calendar calendar) throws Exception;
    EventsSeries createNewEventsSeries(User user, Calendar calendar) throws Exception;
    double calculateCycleLength(User user);
    boolean isUserExist(int userId);
}
