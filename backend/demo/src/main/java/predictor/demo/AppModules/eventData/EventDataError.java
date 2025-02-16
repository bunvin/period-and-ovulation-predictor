package predictor.demo.AppModules.eventData;

import predictor.demo.Error.ErrorMessage;

public enum EventDataError implements ErrorMessage {
    EVENT_NOT_FOUND(2001, "AppException: Event not found"),
    EVENT_ALREADY_EXIST(2002, "AppException: Event already exist");

    private final int code;
    private final String message;

    EventDataError(int code, String message) {
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
