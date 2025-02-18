package predictor.demo.AppModules.eventsSeries;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventsSeriesRepository extends JpaRepository<EventsSeries, Integer> {

    boolean existsByUserId(int userId);
    EventsSeries findByUserId(int userId);

    @Transactional
    @Query(value = "SELECT AVG(e.calculated_cycle_length) " +
            "FROM events_series e " +
            "WHERE e.user_id = :userId " +
            "AND e.calculated_cycle_length > 20 AND e.calculated_cycle_length < 60 " +
            "ORDER BY e.prediction_date DESC " +
            "LIMIT 4", nativeQuery = true)
    double findTop4EventsByUserIdWithAverageCycleLength(@Param("userId") int userId);



}
