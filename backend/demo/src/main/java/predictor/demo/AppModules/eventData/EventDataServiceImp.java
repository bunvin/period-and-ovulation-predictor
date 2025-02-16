package predictor.demo.AppModules.eventData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import predictor.demo.Error.AppException;

import java.util.List;

@Service
public class EventDataServiceImp implements EventDataService {
    @Autowired
    private EventDataRepository eventRepository;

    @Override
    public EventData addEvent(EventData event) throws AppException {
        if(this.eventRepository.existsById(event.getId())){
            throw new AppException(EventDataError.EVENT_ALREADY_EXIST);
        }
        return this.eventRepository.save(event);
    }

    @Override
    public EventData getSingleEvent(int id) throws AppException {
        return this.eventRepository.findById(id)
                .orElseThrow(() -> new AppException(EventDataError.EVENT_NOT_FOUND));
    }

    @Override
    public void updateEvent(EventData event, int id) throws AppException {
        EventData dbEvent = this.getSingleEvent(id); //throws not exist
        event.setId(dbEvent.getId());
        event.setUser(dbEvent.getUser());//making sure user is unchanged
        this.eventRepository.save(event);
    }

    @Override
    public void deleteEvent(int id) throws AppException {
        EventData dbEvent = this.getSingleEvent(id);//throws nor exist
        this.eventRepository.deleteById(id);
    }

    @Override
    public void deleteAllPredictedEventsByUser(int userId) {
        this.eventRepository.deleteAllPredictedEventsByUserId(userId);
    }

    @Override
    public EventData getLastPeriod(int userId) {
        return this.eventRepository.findMostRecentPeriodFirstDayEvent(userId);
    }

    @Override
    public Long countUserActualEvents(int userId) {
        return this.eventRepository.countByUserAndIsPredictedFalse(userId);
    }

    @Override
    public List<EventData> getAllUserPredictedEvents(int userId) {
        return this.eventRepository.getAllUserPredictedEvents(userId);
    }

    @Override
    public List<EventData> getAllUserActualEvents(int userId) {
        return this.eventRepository.getAllUserActualEvents(userId);
    }

}
