package predictor.demo.AppModules.user;
import predictor.demo.Error.ErrorMessage;

public enum UserError implements ErrorMessage {

    USER_NOT_FOUND(1001, "FAILED: user not found"),
    USER_EMAIL_ALREADY_EXISTS(1002, "FAILED: Email already exists"),
    USER_GOOGLE_SUBJECT_ALREADY_EXISTS(1003, "FAILED: Google subject already exists"),
    USER_INVALID_CREDENTIALS(1004, "FAILED: Invalid credentials");

    private final int code;
    private final String message;

    UserError(int code, String message) {
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
