package com.readrealm.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;


record ErrorResponse(HttpStatusCode statusCode, String message) {

    static ErrorResponse of(ResponseStatusException e) {
        return new ErrorResponse(e.getStatusCode(), e.getReason());
    }
}
