package com.readrealm.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.readrealm.auth.config.SecurityConfig;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        void When_no_jwt_provided_should_return_401() throws Exception {
                mockMvc.perform(get("/v1/payments?orderId=test-order-id"))
                                .andExpect(status().isUnauthorized());
        }
}