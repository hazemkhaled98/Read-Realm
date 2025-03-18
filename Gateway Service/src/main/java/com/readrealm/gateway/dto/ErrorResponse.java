package com.readrealm.gateway.dto;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;


public record ErrorResponse(HttpStatusCode statusCode, String message) {

    static ErrorResponse of(ResponseStatusException e) {
        return new ErrorResponse(e.getStatusCode(), e.getReason());
    }
}
