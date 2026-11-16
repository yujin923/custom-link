package kr.co.shortenurlservice.presentation;

import kr.co.shortenurlservice.domain.DuplicateShortenUrlKeyException;
import kr.co.shortenurlservice.domain.LackOfShortenUrlKeyException;
import kr.co.shortenurlservice.domain.ReservedShortenUrlKeyException;
import kr.co.shortenurlservice.domain.NotFoundShortenUrlException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundShortenUrlException.class)
    public ResponseEntity<String> handleNotFoundShortenUrlException(
            NotFoundShortenUrlException ex
    ) {
        return new ResponseEntity<>("단축 URL을 찾지 못했습니다.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LackOfShortenUrlKeyException.class)
    public ResponseEntity<String> handleLackOfShortenUrlKeyException(
            LackOfShortenUrlKeyException ex
    ) {
        return new ResponseEntity<>("단축 URL 자원이 부족합니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateShortenUrlKeyException.class)
    public ResponseEntity<String> handleDuplicateShortenUrlKeyException(
            DuplicateShortenUrlKeyException ex
    ) {
        return new ResponseEntity<>("이미 사용 중인 단축 URL 키입니다.", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ReservedShortenUrlKeyException.class)
    public ResponseEntity<String> handleReservedShortenUrlKeyException(
            ReservedShortenUrlKeyException ex
    ) {
        return new ResponseEntity<>("사용할 수 없는 단축 URL 키입니다.", HttpStatus.BAD_REQUEST);
    }
}
