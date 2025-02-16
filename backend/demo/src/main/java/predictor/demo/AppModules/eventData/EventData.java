package predictor.demo.AppModules.eventData;

import jakarta.persistence.*;
import predictor.demo.AppModules.eventsSeries.EventsSeries;
import predictor.demo.AppModules.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class EventData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private int id;

    private String title;
    private LocalDate eventDate;
    private boolean isPeriodFirstDay;
    private boolean isPredicted;
    private boolean isSync = false;

    @ManyToOne
    @JoinColumn(name = "user", nullable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "eventsSeries")
    private EventsSeries eventsSeries;

    private String calendarEventId;  // To store Google Calendar event ID

    @Column(updatable = false)
    private LocalDateTime createdDateTime = LocalDateTime.now();
    private LocalDateTime modifiedDateTime = LocalDateTime.now();

    public EventData() {
    }

    public EventData(int id, User user, String title, LocalDate eventDate, boolean isPeriodFirstDay, boolean isPredicted, boolean isSync) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.eventDate = eventDate;
        this.isPeriodFirstDay = isPeriodFirstDay;
        this.isPredicted = isPredicted;
        this.isSync = isSync;
    }

    @PreUpdate
    public void updateModifiedDateTime() {
        this.modifiedDateTime = LocalDateTime.now();
    }

    //builder
    public static class EventDataBuilder {
        private int id;
        private String title;
        private LocalDate eventDate;
        private boolean isPeriodFirstDay;
        private boolean isPredicted;
        private boolean isSync = false;
        private User user;
        private EventsSeries eventsSeries;

        public EventDataBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public EventDataBuilder title(String title) {
            this.title = title;
            return this;
        }

        public EventDataBuilder eventDate(LocalDate eventDate) {
            this.eventDate = eventDate;
            return this;
        }

        public EventDataBuilder isPeriodFirstDay(boolean isPeriodFirstDay) {
            this.isPeriodFirstDay = isPeriodFirstDay;
            return this;
        }

        public EventDataBuilder isPredicted(boolean isPredicted) {
            this.isPredicted = isPredicted;
            return this;
        }

        public EventDataBuilder isSync(boolean isSync) {
            this.isSync = isSync;
            return this;
        }

        public EventDataBuilder user(User user) {
            this.user = user;
            return this;
        }

        public EventDataBuilder eventsSeries(EventsSeries eventsSeries) {
            this.eventsSeries = eventsSeries;
            return this;
        }

        public EventData build() {
            EventData event = new EventData();
            event.id = this.id;
            event.title = this.title;
            event.eventDate = this.eventDate;
            event.isPeriodFirstDay = this.isPeriodFirstDay;
            event.isPredicted = this.isPredicted;
            event.isSync = this.isSync;
            event.user = this.user;
            event.eventsSeries = this.eventsSeries;
            return event;
        }
    }

    //getter setter to String


    public String getCalendarEventId() {
        return calendarEventId;
    }

    public void setCalendarEventId(String calendarEventId) {
        this.calendarEventId = calendarEventId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public boolean isPeriodFirstDay() {
        return isPeriodFirstDay;
    }

    public void setPeriodFirstDay(boolean periodFirstDay) {
        isPeriodFirstDay = periodFirstDay;
    }

    public boolean isPredicted() {
        return isPredicted;
    }

    public void setPredicted(boolean predicted) {
        isPredicted = predicted;
    }

    public boolean isSync() {
        return isSync;
    }

    public void setSync(boolean sync) {
        isSync = sync;
    }

    //only getter
    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public LocalDateTime getModifiedDateTime() {
        return modifiedDateTime;
    }

    //toString

    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                ", eventDate=" + eventDate +
                ", isPeriodFirstDay=" + isPeriodFirstDay +
                ", isPredicted=" + isPredicted +
                ", isSync=" + isSync +
                '}';
    }
}
