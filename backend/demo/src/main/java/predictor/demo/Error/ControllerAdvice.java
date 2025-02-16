package predictor.demo.Error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ControllerAdvice {
    @ExceptionHandler(value = {AppException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)

    public Error handleError(AppException e) {
        return new Error(e.getErrorMessage().getCode(), e.getErrorMessage().getMessage());
    }

}