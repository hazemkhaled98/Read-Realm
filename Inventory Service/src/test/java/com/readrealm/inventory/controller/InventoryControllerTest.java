package com.readrealm.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.readrealm.inventory.dto.InventoryDTO;
import com.readrealm.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.readrealm.auth.util.MockAuthorizationUtil.mockAdminJWT;
import static com.readrealm.auth.util.MockAuthorizationUtil.mockCustomerJWT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = InventoryController.class)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Inventory Controller Unit Test")
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @BeforeEach
    void setup() {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Test
    void Given_valid_isbn_should_return_200() throws Exception {
        InventoryDTO inventoryDTO = new InventoryDTO("9780553103540", 10);
        when(inventoryService.getInventoryByIsbn("9780553103540")).thenReturn(inventoryDTO);

        mockMvc.perform(get("/v1/inventory/9780553103540")
                        .with(jwt().jwt(mockCustomerJWT()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_invalid_isbn_should_return_400() throws Exception {
        mockMvc.perform(get("/v1/inventory/invalid-isbn")
                        .with(jwt().jwt(mockCustomerJWT()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void Given_nonexistent_isbn_should_return_404() throws Exception {
        when(inventoryService.getInventoryByIsbn("9780553103540"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found"));

        mockMvc.perform(get("/v1/inventory/9780553103540")
                        .with(jwt().jwt(mockCustomerJWT()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void Given_admin_with_valid_inventory_should_create_and_return_201() throws Exception {
        InventoryDTO inventoryDTO = new InventoryDTO("9780553103540", 10);
        when(inventoryService.createInventory(any(InventoryDTO.class))).thenReturn(inventoryDTO);

        String createRequest = """ 
                {"isbn": "9780553103540", "quantity": 10}
                """;

        mockMvc.perform(post("/v1/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest)
                        .with(jwt().jwt(mockAdminJWT())))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_customer_tries_to_create_inventory_should_return_403() throws Exception {
        String createRequest = """ 
                {"isbn": "9780553103540", "quantity": 10}
                """;

        when(inventoryService.createInventory(any(InventoryDTO.class)))
                .thenThrow(new AccessDeniedException("Access Denied"));

        mockMvc.perform(post("/v1/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest)
                        .with(jwt().jwt(mockCustomerJWT())))
                .andExpect(status().isForbidden());
    }

    @Test
    void Given_valid_update_request_should_return_200() throws Exception {
        InventoryDTO inventoryDTO = new InventoryDTO("9780553103540", 15);
        when(inventoryService.updateInventory(any(InventoryDTO.class))).thenReturn(inventoryDTO);

        String updateRequest = """ 
                {"isbn": "9780553103540", "quantity": 10}
                """;

        mockMvc.perform(patch("/v1/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest)
                        .with(jwt().jwt(mockAdminJWT())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_admin_deleting_inventory_should_return_204() throws Exception {
        mockMvc.perform(delete("/v1/inventory/9780553103540")
                        .with(jwt().jwt(mockAdminJWT()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void Given_customer_tries_to_delete_inventory_should_return_403() throws Exception {
        doThrow(new AccessDeniedException("Access Denied"))
                .when(inventoryService).deleteInventory("9780553103540");

        mockMvc.perform(delete("/v1/inventory/9780553103540")
                        .with(jwt().jwt(mockCustomerJWT()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void Given_valid_reserve_stock_request_should_return_201() throws Exception {
        InventoryDTO response = new InventoryDTO("9780553103540", 8);

        when(inventoryService.reserveStock(any(List.class)))
                .thenReturn(List.of(response));

        String reserveRequest = """ 
                [{"isbn": "9780553103540", "quantity": 2}]
                """;

        mockMvc.perform(post("/v1/inventory/reserve-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reserveRequest)
                        .with(jwt().jwt(mockCustomerJWT())))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_insufficient_stock_should_return_400() throws Exception {
        when(inventoryService.reserveStock(any(List.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Insufficient inventory"));

        String reserveRequest = """
                [{"isbn": "9780553103540", "quantity": 100}]
                """;

        mockMvc.perform(post("/v1/inventory/reserve-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reserveRequest)
                        .with(jwt().jwt(mockCustomerJWT())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void When_no_jwt_provided_should_return_401() throws Exception {
        mockMvc.perform(get("/v1/inventory/9780553103540"))
                .andExpect(status().isUnauthorized());
    }
}