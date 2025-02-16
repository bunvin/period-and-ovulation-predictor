package predictor.demo.AppModules.eventData;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventDataRepository extends JpaRepository<EventData, Integer> {

    @Modifying
    @Transactional
    @Query("DELETE FROM EventData e WHERE e.user.id = :userId AND e.isPredicted = true")
    void deleteAllPredictedEventsByUserId(@Param("userId") int userId);

    @Query("SELECT e FROM EventData e WHERE e.isPeriodFirstDay = true AND e.isPredicted = false" +
            " AND e.user.id = :userId ORDER BY e.eventDate DESC")
    EventData findMostRecentPeriodFirstDayEvent(@Param("userId") int userId);

    @Query("SELECT e FROM EventData e WHERE e.user.id = :userId AND e.isPredicted = true")
    List<EventData> getAllUserPredictedEvents(@Param("userId") int userId);

    @Query("SELECT e FROM EventData e WHERE e.user.id = :userId AND e.isPredicted = false")
    List<EventData> getAllUserActualEvents(@Param("userId") int userId);

    @Query("SELECT COUNT(e) FROM EventData e WHERE e.user.id = :userId AND e.isPredicted = false")
    long countByUserAndIsPredictedFalse(@Param("userId") int userId);

}
