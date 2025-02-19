package predictor.demo.AppModules.eventData;
import java.util.List;

import predictor.demo.Error.AppException;

public interface EventDataService {
    EventData addEvent(EventData event) throws AppException;
    EventData getSingleEvent(int id) throws AppException;
    void updateEvent(EventData event, int id) throws AppException;
    void deleteEvent(int id) throws AppException;

    void deleteAllPredictedEventsByUser(int userId);
    EventData getLastPeriod(int userId);
    Long countUserActualEvents(int userId);
    List<EventData> getAllUserPredictedEvents(int userId);
    List<EventData> getAllUserActualEvents(int userId);
    double calculateCycleLength(int userId);
}
