package com.readrealm.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record StripeWebhookRequest(
        String id,
        String object,
        @JsonProperty("api_version") String apiVersion,
        long created,
        Data data,
        boolean livemode,
        @JsonProperty("pending_webhooks") int pendingWebhooks,
        Request request,
        String type
) {
    public record Data(
            @JsonProperty("object") SetupIntent setupIntent
    ) {}

    public record SetupIntent(
            String id,
            String object,
            String application,
            @JsonProperty("automatic_payment_methods") Object automaticPaymentMethods,
            @JsonProperty("cancellation_reason") String cancellationReason,
            @JsonProperty("client_secret") String clientSecret,
            long created,
            String customer,
            String description,
            @JsonProperty("flow_directions") Object flowDirections,
            @JsonProperty("last_setup_error") Object lastSetupError,
            @JsonProperty("latest_attempt") Object latestAttempt,
            boolean livemode,
            String mandate,
            Map<String, String> metadata,
            @JsonProperty("next_action") Object nextAction,
            @JsonProperty("on_behalf_of") String onBehalfOf,
            @JsonProperty("payment_method") String paymentMethod,
            @JsonProperty("payment_method_options") PaymentMethodOptions paymentMethodOptions,
            @JsonProperty("payment_method_types") String[] paymentMethodTypes,
            @JsonProperty("single_use_mandate") Object singleUseMandate,
            String status,
            String usage
    ) {}

    public record PaymentMethodOptions(
            @JsonProperty("acss_debit") AcssDebit acssDebit
    ) {}

    public record AcssDebit(
            String currency,
            @JsonProperty("mandate_options") MandateOptions mandateOptions,
            @JsonProperty("verification_method") String verificationMethod
    ) {}

    public record MandateOptions(
            @JsonProperty("interval_description") String intervalDescription,
            @JsonProperty("payment_schedule") String paymentSchedule,
            @JsonProperty("transaction_type") String transactionType
    ) {}

    public record Request(
            String id,
            @JsonProperty("idempotency_key") String idempotencyKey
    ) {}
}

