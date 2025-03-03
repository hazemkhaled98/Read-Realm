package com.readrealm.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;


record ErrorResponse(String message, HttpStatusCode statusCode) {

    static ErrorResponse of(ResponseStatusException e) {
        return new ErrorResponse(e.getReason(), e.getStatusCode());
    }
}
