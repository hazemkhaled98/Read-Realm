package com.readrealm.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

public record StripeWebhookRequest(
        String id,
        String object,
        @JsonProperty("api_version") String apiVersion,
        Instant created,
        String type,
        Data data
) {
    public record Data(ObjectData object) {}

    public record ObjectData(
            String id,
            @JsonProperty("payment_status") String paymentStatus,
            @JsonProperty("customer_email") String customerEmail,
            Map<String, Object> metadata
    ) {}
}

