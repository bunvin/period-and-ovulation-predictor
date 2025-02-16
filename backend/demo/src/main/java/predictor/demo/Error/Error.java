
package predictor.demo.Error;

public class Error {
    private int code;
    private String message;

    //constractors
    public Error(int code, String message) {
        this.code = code;
        this.message = message;
    }

    //getter setter to String

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Error{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
