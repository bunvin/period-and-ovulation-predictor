package predictor.demo.AppModules.eventsSeries;

import jakarta.persistence.*;
import predictor.demo.AppModules.eventData.EventData;
import predictor.demo.AppModules.user.User;

import java.time.LocalDate;
import java.util.List;

@Entity
public class EventsSeries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate predictionDate;
    private double calculatedCycleLength;

    @OneToMany(mappedBy = "eventsSeries", cascade = CascadeType.ALL, orphanRemoval = true) //prediction dates removed with EventSeries
    private List<EventData> predictedEvents;

    public EventsSeries() {
    }

    public EventsSeries(int id, User user, LocalDate predictionDate, double calculatedCycleLength) {
        this.id = id;
        this.user = user;
        this.predictionDate = predictionDate;
        this.calculatedCycleLength = calculatedCycleLength;
    }

    public EventsSeries(int id, User user, LocalDate predictionDate, double calculatedCycleLength, List<EventData> predictedEvents) {
        this.id = id;
        this.user = user;
        this.predictionDate = predictionDate;
        this.calculatedCycleLength = calculatedCycleLength;
        this.predictedEvents = predictedEvents;
    }

    //builder getter setter to String

    // Builder Class
    public static class Builder {

        private int id;
        private User user;
        private LocalDate predictionDate;
        private double calculatedCycleLength;

        // Setters for each field, returning the Builder itself for chaining
        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        public Builder setPredictionDate(LocalDate predictionDate) {
            this.predictionDate = predictionDate;
            return this;
        }

        public Builder setCalculatedCycleLength(double calculatedCycleLength) {
            this.calculatedCycleLength = calculatedCycleLength;
            return this;
        }

        // Build method to return the final EventsSeries object
        public EventsSeries build() {
            // Validation logic can be added here if necessary
            return new EventsSeries(this.id, this.user, this.predictionDate, this.calculatedCycleLength);
        }
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getPredictionDate() {
        return predictionDate;
    }

    public void setPredictionDate(LocalDate predictionDate) {
        this.predictionDate = predictionDate;
    }

    public double getCalculatedCycleLength() {
        return calculatedCycleLength;
    }

    public void setCalculatedCycleLength(double calculatedCycleLength) {
        this.calculatedCycleLength = calculatedCycleLength;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<EventData> getPredictedEvents() {
        return predictedEvents;
    }

    public void setPredictedEvents(List<EventData> predictedEvents) {
        this.predictedEvents = predictedEvents;
    }

    @Override
    public String toString() {
        return "EventsSeries{" +
                "id=" + id +
                ", predictionDate=" + predictionDate +
                ", calculatedCycleLength=" + calculatedCycleLength +
                '}';
    }
}
