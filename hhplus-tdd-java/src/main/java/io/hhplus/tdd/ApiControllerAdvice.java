package io.hhplus.tdd;

import io.hhplus.tdd.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = InvalidOverPointAmountException.class)
    public ResponseEntity<ErrorResponse> invalidOverPointAmountException(Exception e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(value = InvalidUserIdException.class)
    public ResponseEntity<ErrorResponse> invalidUserIdException(InvalidUserIdException e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(value = MinusPointChargeFailedException.class)
    public ResponseEntity<ErrorResponse> minusPointChargeFailedException(Exception e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(value = MinusPointSpendFailedException.class)
    public ResponseEntity<ErrorResponse> minusPointSpendFailedException(Exception e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(value = OverPointChargeFailedException.class)
    public ResponseEntity<ErrorResponse> overPointChargeFailedException(Exception e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(value = OverPointSpendFailedException.class)
    public ResponseEntity<ErrorResponse> overPointSpendFailedException(Exception e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(value = PointChargeFailedException.class)
    public ResponseEntity<ErrorResponse> pointChargeFailedException(Exception e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(value = UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> userNotFoundException(Exception e) {
        return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
    }
}
