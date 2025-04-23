package com.readrealm.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.readrealm.auth.config.SecurityConfig;
import com.readrealm.order.dto.OrderResponse;
import com.readrealm.order.service.OrderService;
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

import java.util.List;

import static com.readrealm.auth.util.MockAuthorizationUtil.mockCustomerJWT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Order Controller Unit Test")
@Import(SecurityConfig.class)
class OrderControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private OrderService orderService;

        @BeforeEach
        void setup() {
                objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        }

        @Test
        void Given_valid_order_request_should_return_201() throws Exception {
                String request = """
                                {
                                    "orderItems": [
                                        {
                                            "isbn": "9780553103540",
                                            "quantity": 2,
                                            "unitPrice": 10.99
                                        }
                                    ]
                                }
                                """;

                when(orderService.createOrder(any())).thenReturn(any(String.class));

                mockMvc.perform(post("/v1/orders/create")
                                .with(jwt().jwt(mockCustomerJWT()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isCreated());
        }

        @Test
        void Given_invalid_order_request_should_return_400() throws Exception {
                String request = """
                                {
                                    "orderItems": [
                                        {
                                            "isbn": "9780439064873",
                                            "quantity": -1
                                        }
                                    ]
                                }
                                """;

                mockMvc.perform(post("/v1/orders/create")
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void Given_valid_order_id_should_return_200() throws Exception {

                when(orderService.getOrderById("test-order")).thenReturn(mock(OrderResponse.class));

                mockMvc.perform(get("/v1/orders/{orderId}", "test-order")
                                .with(jwt().jwt(mockCustomerJWT())))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void Given_nonexistent_order_id_should_return_404() throws Exception {
                when(orderService.getOrderById("nonexistent-id"))
                                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

                mockMvc.perform(get("/v1/orders/{orderId}", "nonexistent-id")
                                .with(jwt().jwt(mockCustomerJWT())))
                                .andExpect(status().isNotFound());
        }

        @Test
        void Given_valid_user_id_should_return_200() throws Exception {

                when(orderService.getOrdersByUserId("test-user")).thenReturn(List.of(mock(OrderResponse.class)));

                mockMvc.perform(get("/v1/orders")
                                .param("userId", "test-user")
                                .with(jwt().jwt(mockCustomerJWT())))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void Given_valid_cancel_request_should_return_201() throws Exception {

                when(orderService.cancelOrder("test-order")).thenReturn(any(String.class));

                mockMvc.perform(post("/v1/orders/cancel")
                                .param("orderId", "test-order")
                                .with(jwt().jwt(mockCustomerJWT())))
                                .andExpect(status().isCreated());
        }

        @Test
        void Given_valid_refund_request_should_return_201() throws Exception {

                when(orderService.refundOrder("test-order")).thenReturn(any(String.class));

                mockMvc.perform(post("/v1/orders/refund")
                                .param("orderId", "test-order")
                                .with(jwt().jwt(mockCustomerJWT())))
                                .andExpect(status().isCreated());
        }

        @Test
        void When_no_jwt_provided_should_return_401() throws Exception {
                mockMvc.perform(get("/v1/orders/test-order"))
                                .andExpect(status().isUnauthorized());
        }
}