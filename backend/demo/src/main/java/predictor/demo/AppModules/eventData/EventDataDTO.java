package predictor.demo.AppModules.eventData;


import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EventDataDTO {
    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    // to allow overriding default values
    private String title = "Period";
    private boolean isPeriodFirstDay = true;
    private boolean isPredicted = false;
    private boolean isSync = false;

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
}
