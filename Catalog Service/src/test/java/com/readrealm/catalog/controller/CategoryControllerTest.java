package com.readrealm.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.readrealm.catalog.dto.category.CategoryRequest;
import com.readrealm.catalog.dto.category.CategoryResponse;
import com.readrealm.catalog.dto.category.UpdateCategoryRequest;
import com.readrealm.catalog.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static com.readrealm.auth.util.MockAuthorizationUtil.mockAdminJWT;
import static com.readrealm.auth.util.MockAuthorizationUtil.mockCustomerJWT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CategoryController.class)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Category Controller Unit Test")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @BeforeEach
    void setup() {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Test
    void Given_customer_authorization_when_requesting_all_categories_returns_200() throws Exception {

        CategoryResponse categoryResponse = Mockito.mock(CategoryResponse.class);

        when(categoryService.findAllCategories())
                .thenReturn(Collections.singletonList(categoryResponse));

        mockMvc.perform(get("/v1/categories")
                .with(jwt().jwt(mockCustomerJWT())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_customer_authorization_when_requesting_valid_category_id_should_return_200() throws Exception {
        CategoryResponse categoryResponse = Mockito.mock(CategoryResponse.class);

        when(categoryService.findCategoryById(1L))
                .thenReturn(categoryResponse);

        mockMvc.perform(get("/v1/categories/1")
                        .with(jwt().jwt(mockCustomerJWT())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_admin_authorization_when_creating_valid_category_should_return_201() throws Exception {
        CategoryResponse categoryResponse = Mockito.mock(CategoryResponse.class);
        when(categoryService.addCategory(any(CategoryRequest.class))).thenReturn(categoryResponse);

        String categoryCreateRequest = """
                {
                    "name": "fantasy"
                }
                """;

        mockMvc.perform(post("/v1/categories")
                        .with(jwt().jwt(mockAdminJWT()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryCreateRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_admin_authorization_when_updating_valid_category_should_return_200() throws Exception {
        CategoryResponse categoryResponse = Mockito.mock(CategoryResponse.class);
        when(categoryService.updateCategory(any(UpdateCategoryRequest.class))).thenReturn(categoryResponse);

        String categoryUpdateRequest = """
                {
                    "id": 1,
                    "details":{
                    "name": "fantasy"
                    }
                }
                """;

        mockMvc.perform(put("/v1/categories")
                        .with(jwt().jwt(mockAdminJWT()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryUpdateRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_admin_authorization_when_deleting_category_should_return_204() throws Exception {
        mockMvc.perform(delete("/v1/categories/1234567890")
                .with(jwt().jwt(mockAdminJWT())))
                .andExpect(status().isNoContent());
    }

    @Test
    void Given_customer_authorization_when_creating_category_should_return_403() throws Exception {

        when(categoryService.addCategory(any(CategoryRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer cannot add category"));

        String categoryCreateRequest = """
                {
                    "name": "fantasy"
                }
                """;

        mockMvc.perform(post("/v1/categories")
                        .with(jwt().jwt(mockCustomerJWT()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryCreateRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    void Given_customer_authorization_when_updating_category_should_return_403() throws Exception {

        when(categoryService.updateCategory(any(UpdateCategoryRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer cannot update category"));

        String categoryUpdateRequest = """
                {
                    "id": 1,
                    "details":{
                    "name": "fantasy"
                    }
                }
                """;

        mockMvc.perform(put("/v1/categories")
                .with(jwt().jwt(mockCustomerJWT()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryUpdateRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    void Given_customer_authorization_when_deleting_category_should_return_403() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer cannot delete category"))
                .when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/v1/categories/1")
                        .with(jwt().jwt(mockCustomerJWT())))
                .andExpect(status().isForbidden());
    }

    @Test
    void Given_admin_authorization_when_requesting_non_existing_category_should_return_404() throws Exception {

        when(categoryService.findCategoryById(1000L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/v1/categories/1000")
                .with(jwt().jwt(mockAdminJWT())))
                .andExpect(status().isNotFound());
    }

    @Test
    void Given_admin_authorization_when_creating_category_with_same_name_should_return_400() throws Exception {
        when(categoryService.addCategory(any(CategoryRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category with same name already exists"));

        String categoryCreateRequest = """
                {
                    "name": "fantasy"
                }
                """;

        mockMvc.perform(post("/v1/categories")
                        .with(jwt().jwt(mockAdminJWT()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryCreateRequest))
                .andExpect(status().isBadRequest());
    }
}
