package com.readrealm.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException e) {
        return new ResponseEntity<>(ErrorResponse.of(e), e.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleConstraintViolation(MethodArgumentNotValidException ex) {

        Map<String, Object> response = new HashMap<>();

        Map<String, List<String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(
                                this::getErrorMessage,
                                Collectors.toList()
                        )
                ));

        List<String> globalErrors = ex.getBindingResult()
                .getGlobalErrors()
                .stream()
                .map(this::getErrorMessage)
                .toList();

        if (!globalErrors.isEmpty()) {
            response.put("globalErrors", globalErrors);
        }

        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>("Server has issues fulfilling your request", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getErrorMessage(ObjectError error) {
        String message = error.getDefaultMessage();

        return message != null ? message : "Validation failed";
    }

}
