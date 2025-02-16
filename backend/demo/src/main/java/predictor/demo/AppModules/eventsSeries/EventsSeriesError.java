package predictor.demo.AppModules.eventsSeries;

import predictor.demo.Error.ErrorMessage;

public enum EventsSeriesError implements ErrorMessage {
    EVENTS_SERIES_NO_NEW_ACTUAL_EVENTS(3001, "FAILED: no new actual event to predict by"),
    EVENTS_SERIES_NOT_ENOUGH_ACTUAL_EVENTS(3002, "FAILED: user needs more than 2 actual events to make prediction"),
    CALENDAR_SYNC_ERROR(3003, "FAILED: sync with Google Calendar"),
    EVENTS_SERIES_NOT_FOUND(3004, "FAILED: eventsSeries not found");

    private final int code;
    private final String message;

    EventsSeriesError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }


}
