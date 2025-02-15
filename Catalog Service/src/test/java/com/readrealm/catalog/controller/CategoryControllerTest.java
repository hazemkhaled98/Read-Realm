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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
    public void setup() {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Test
    void Requesting_all_categories_returns_200() throws Exception {

        CategoryResponse categoryResponse = Mockito.mock(CategoryResponse.class);

        when(categoryService.findAllCategories())
                .thenReturn(Collections.singletonList(categoryResponse));

        mockMvc.perform(get("/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_a_valid_Id_of_category_should_return_200() throws Exception {
        CategoryResponse categoryResponse = Mockito.mock(CategoryResponse.class);

        when(categoryService.findCategoryById(1L))
                .thenReturn(categoryResponse);

        mockMvc.perform(get("/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_a_valid_category_body_to_create_should_return_201() throws Exception {
        CategoryResponse categoryResponse = Mockito.mock(CategoryResponse.class);
        when(categoryService.addCategory(any(CategoryRequest.class))).thenReturn(categoryResponse);

        String categoryCreateRequest = """
                {
                    "name": "fantasy"
                }
                """;

        mockMvc.perform(post("/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryCreateRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_a_valid_author_body_to_update_should_return_200() throws Exception {
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryUpdateRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_a_category_id_to_delete_should_return_204() throws Exception {
        mockMvc.perform(delete("/v1/categories/1234567890"))
                .andExpect(status().isNoContent());
    }
}
