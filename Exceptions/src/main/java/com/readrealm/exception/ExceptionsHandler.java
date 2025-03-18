package com.readrealm.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Profile("!test")
public class ExceptionsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionsHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException e) {
        LOGGER.warn("Response status exception: {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.of(e), e.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleConstraintViolation(MethodArgumentNotValidException ex) {

        LOGGER.warn("Validation Exception: {}", ex.getMessage());
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

    @ExceptionHandler({
            AuthenticationException.class,
            AuthenticationCredentialsNotFoundException.class,
            BadCredentialsException.class,
            InsufficientAuthenticationException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<?> handleAuthenticationException(Exception e) {
        LOGGER.warn("Authentication failed: {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.of(new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Authentication required. No valid credentials were provided.")), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        LOGGER.warn("Access denied: {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.of(new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Access denied. You don't have permission to access this resource.")), HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    public ResponseEntity<?> handleHttpClientErrorException(HttpStatusCodeException e) {
        LOGGER.warn("HTTP Client error: {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.of(new ResponseStatusException(
                e.getStatusCode(), formatHttpClientErrorMessages(e.getMessage()))), e.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        LOGGER.error("Unexpected error: {}", e.getMessage(), e);
        return new ResponseEntity<>("Server has issues fulfilling your request", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getErrorMessage(ObjectError error) {
        String message = error.getDefaultMessage();

        return message != null ? message : "Validation failed";
    }

    // An example message before formatting: // 400 : "{"message":"Insufficient inventory for ISBN: 9780553103540","statusCode":"BAD_REQUEST"}
    private String formatHttpClientErrorMessages(String message) {
        int startIndex = message.indexOf("\"message\":\"") + 11;
        int endIndex = message.indexOf("\",\"statusCode\"");
        return startIndex >= 11 && endIndex > startIndex ? message.substring(startIndex, endIndex) : message;
    }

}
