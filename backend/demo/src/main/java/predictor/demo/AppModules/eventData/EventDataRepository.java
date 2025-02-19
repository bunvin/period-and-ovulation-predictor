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

@Query(value = "SELECT * FROM event_data WHERE id = (" +
               "SELECT MAX(id) FROM event_data " +
               "WHERE user_id = :userId " +
               "AND is_period_first_day = true AND is_predicted = false)"
            , nativeQuery = true)
EventData findMostRecentPeriodFirstDayEvent(@Param("userId") int userId);

    @Query("SELECT e FROM EventData e WHERE e.user.id = :userId AND e.isPredicted = true")
    List<EventData> getAllUserPredictedEvents(@Param("userId") int userId);

    @Query("SELECT e FROM EventData e WHERE e.user.id = :userId AND e.isPredicted = false")
    List<EventData> getAllUserActualEvents(@Param("userId") int userId);

    @Query("SELECT COUNT(e) FROM EventData e WHERE e.user.id = :userId AND e.isPredicted = false")
    long countByUserAndIsPredictedFalse(@Param("userId") int userId);

    @Query(value = "SELECT e FROM EventData e " +
        "WHERE e.user.id = :userId AND e.isPredicted = false AND e.isPeriodFirstDay = true " +
        "ORDER BY e.eventDate DESC", nativeQuery = true)
    List<EventData> findTop4RecentPeriodFirstDayEvents(@Param("userId") int userId);

    @Query(value = "WITH period_events AS ( " +
                "    SELECT event_date, " +
                "           LAG(event_date) OVER (ORDER BY event_date DESC) AS prev_event_date " +
                    "    FROM event_data " +
                    "    WHERE user_id = :userId " +
                    "    AND is_period_first_day = true " +
                    "    AND is_predicted = false " +
                    ") " +
                "SELECT AVG(DATEDIFF(prev_event_date, event_date)) " +
                "FROM period_events " +
                "WHERE prev_event_date IS NOT NULL", 
                nativeQuery = true)
Double calculateAverageCycleLength(@Param("userId") int userId);

}
