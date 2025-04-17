package com.readrealm.payment.integration;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;


public class StripeStubs {

    private final WireMockServer wireMock;

    public StripeStubs(WireMockServer wireMock) {
        this.wireMock = wireMock;
    }


    public void stubPaymentIntentCreation() {
        wireMock.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "pi_test",
                                    "client_secret": "test_secret",
                                    "status": "requires_payment_method"
                                }
                                """)));
    }


    public void stubPaymentIntentCreationError() {
        wireMock.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "error": {
                                        "message": "Invalid request"
                                    }
                                }
                                """)));
    }


    public void stubPaymentIntentRetrieval(String status) {
        wireMock.stubFor(get(urlEqualTo("/v1/payment_intents/pi_test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "pi_test",
                                    "status": "%s"
                                }
                                """.formatted(status))));
    }

    public void stubPaymentIntentCancellation() {
        wireMock.stubFor(post(urlEqualTo("/v1/payment_intents/pi_test/cancel"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "pi_test",
                                    "status": "canceled"
                                }
                                """)));
    }

    public void stubRefundCreation() {
        wireMock.stubFor(post(urlEqualTo("/v1/refunds"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": "3214328912312312",
                                  "object": "refund",
                                  "amount": 1000,
                                  "balance_transaction": "txn_1Nispe2eZvKYlo2CYezqFhEx",
                                  "charge": "ch_1NirD82eZvKYlo2CIvbtLWuY",
                                  "created": 1692942318,
                                  "currency": "usd",
                                  "destination_details": {
                                    "card": {
                                      "reference": "123456789012",
                                      "reference_status": "available",
                                      "reference_type": "acquirer_reference_number",
                                      "type": "refund"
                                    },
                                    "type": "card"
                                  },
                                  "metadata": {},
                                  "payment_intent": "pi_test",
                                  "reason": null,
                                  "receipt_number": null,
                                  "source_transfer_reversal": null,
                                  "status": "succeeded",
                                  "transfer_reversal": null
                                }
                                """)));
    }

    public String getValidStripeWebhookPayload(String orderId) {
        return String.format("""
                {
                  "id": "evt_3RD7BtJyYNn6LMIK1K8jEXVI",
                  "object": "event",
                  "api_version": "2025-02-24.acacia",
                  "created": 1744903683,
                  "data": {
                    "object": {
                      "id": "ch_3RD7BtJyYNn6LMIK1juGcY1z",
                      "object": "charge",
                      "amount": 1000,
                      "amount_captured": 1000,
                      "amount_refunded": 0,
                      "balance_transaction": "txn_3RD7BtJyYNn6LMIK1UnP9G0P",
                      "billing_details": {
                        "address": {}
                      },
                      "calculated_statement_descriptor": "Stripe",
                      "captured": true,
                      "created": 1744903683,
                      "currency": "usd",
                      "disputed": false,
                      "fraud_details": {},
                      "livemode": false,
                      "metadata": {
                        "orderId": "%s"
                      },
                      "outcome": {
                        "network_status": "approved_by_network",
                        "risk_level": "normal",
                        "risk_score": 36,
                        "seller_message": "Payment complete.",
                        "type": "authorized"
                      },
                      "paid": true,
                      "payment_intent": "pi_3RD7BtJyYNn6LMIK1fmJVc5t",
                      "payment_method": "pm_1REuMBJyYNn6LMIKMJ159ZSw",
                      "payment_method_details": {
                        "card": {
                          "amount_authorized": 1000,
                          "brand": "visa",
                          "checks": {
                            "cvc_check": "pass"
                          },
                          "country": "US",
                          "exp_month": 4,
                          "exp_year": 2026,
                          "extended_authorization": {
                            "status": "disabled"
                          },
                          "fingerprint": "9tglRkmTKUH3QARh",
                          "funding": "credit",
                          "incremental_authorization": {
                            "status": "unavailable"
                          },
                          "last4": "4242",
                          "multicapture": {
                            "status": "unavailable"
                          },
                          "network": "visa",
                          "network_token": {
                            "used": false
                          },
                          "network_transaction_id": "571161031088210",
                          "overcapture": {
                            "maximum_amount_capturable": 1000,
                            "status": "unavailable"
                          },
                          "regulated_status": "unregulated"
                        },
                        "type": "card"
                      },
                      "radar_options": {},
                      "receipt_url": "https://pay.stripe.com/receipts/payment/CAcaFwoVYWNjdF8xUXk3dm5KeVlObjZMTUlLKIS8hMAGMgYl7QbkXiU6LBZ1HXqBx-Dm2cfND1ewZKnwTKk0SxnmckDEMEywLbajT4C_nFRSE6zEZ_CP",
                      "refunded": false,
                      "status": "succeeded"
                    }
                  },
                  "livemode": false,
                  "pending_webhooks": 2,
                  "request": {
                    "id": "req_DvSJaDhcroGJq5",
                    "idempotency_key": "2ddc622c-2324-46ab-8d51-b5d23eafa6de"
                  },
                  "type": "payment_intent.succeeded"
                }
                
                """, orderId);
    }
}