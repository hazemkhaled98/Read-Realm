package com.readrealm.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.readrealm.auth.config.SecurityConfig;
import com.readrealm.payment.dto.PaymentRequest;
import com.readrealm.payment.dto.PaymentResponse;
import com.readrealm.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static com.readrealm.auth.util.MockAuthorizationUtil.mockCustomerJWT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = PaymentController.class)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Payment Controller Unit Test")
@Import(SecurityConfig.class)
class PaymentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private PaymentService paymentService;

        @BeforeEach
        void setup() {
                objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        }

        @Test
        void Given_valid_payment_request_should_return_201() throws Exception {

                when(paymentService.createPayment(any(PaymentRequest.class))).thenReturn(mock(PaymentResponse.class));

                String paymentRequest = """
                                {
                                    "orderId": "test-order-id",
                                    "amount": 10,
                                    "currency": "USD"
                                }
                                """;

                mockMvc.perform(post("/v1/payments")
                                .with(jwt().jwt(mockCustomerJWT()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(paymentRequest))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void Given_duplicate_order_id_should_return_400() throws Exception {
                when(paymentService.createPayment(any(PaymentRequest.class)))
                                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                "Payment already exists"));

                String paymentRequest = """
                                {
                                    "orderId": "test-order-id",
                                    "amount": 10,
                                    "currency": "USD"
                                }
                                """;

                mockMvc.perform(post("/v1/payments")
                                .with(jwt().jwt(mockCustomerJWT()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(paymentRequest))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void Given_valid_order_id_should_return_200() throws Exception {

                when(paymentService.getPaymentByOrderId(anyString())).thenReturn(mock(PaymentResponse.class));

                mockMvc.perform(get("/v1/payments?orderId=test-order-id")
                                .with(jwt().jwt(mockCustomerJWT()))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void Given_non_existent_order_id_should_return_404() throws Exception {
                when(paymentService.getPaymentByOrderId(anyString()))
                                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

                mockMvc.perform(get("/v1/payments?orderId=non-existent-id")
                                .with(jwt().jwt(mockCustomerJWT()))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

        @Test
        void Given_valid_webhook_request_should_return_200() throws Exception {
                String webhookPayload = """
                                {
                                    "data": {
                                        "setupIntent": {
                                            "metadata": {
                                                "orderId": "test-order-id"
                                            }
                                        }
                                    }
                                }
                                """;

                mockMvc.perform(post("/v1/payments/webhook")
                                .header("Stripe-Signature", "test-signature")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(webhookPayload))
                                .andExpect(status().isOk());
        }

        @Test
        void Given_invalid_webhook_signature_should_return_400() throws Exception {

                doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe signature"))
                        .when(paymentService)
                        .handleStripeWebhook(anyString(), anyString());


                String webhookPayload = """
                                {
                                    "data": {
                                        "setupIntent": {
                                            "metadata": {
                                                "orderId": "test-order-id"
                                            }
                                        }
                                    }
                                }
                                """;

                mockMvc.perform(post("/v1/payments/webhook")
                                .header("Stripe-Signature", "invalid-signature")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(webhookPayload))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void Given_valid_cancel_request_should_return_200() throws Exception {

                when(paymentService.cancelPayment(anyString())).thenReturn(mock(PaymentResponse.class));

                mockMvc.perform(post("/v1/payments/cancel?orderId=test-order-id")
                                .with(jwt().jwt(mockCustomerJWT()))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void Given_valid_refund_request_should_return_200() throws Exception {

                when(paymentService.refundPayment(anyString())).thenReturn(mock(PaymentResponse.class));

                mockMvc.perform(post("/v1/payments/refund?orderId=test-order-id")
                                .with(jwt().jwt(mockCustomerJWT()))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void When_no_jwt_provided_should_return_401() throws Exception {
                mockMvc.perform(get("/v1/payments?orderId=test-order-id"))
                                .andExpect(status().isUnauthorized());
        }
}