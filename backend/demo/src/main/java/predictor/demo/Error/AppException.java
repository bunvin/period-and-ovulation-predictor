
package predictor.demo.Error;

public class AppException extends Exception{
    private final ErrorMessage errorMessage;

    public AppException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }


}