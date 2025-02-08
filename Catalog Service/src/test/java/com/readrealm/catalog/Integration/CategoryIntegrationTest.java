package com.readrealm.catalog.Integration;


import com.readrealm.catalog.dto.category.CategoryRequest;
import com.readrealm.catalog.dto.category.CategoryResponse;
import com.readrealm.catalog.dto.category.UpdateCategoryRequest;
import com.readrealm.catalog.exception.NotFoundException;
import com.readrealm.catalog.service.CategoryService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest(properties = "spring.flyway.enabled=false")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Category Integration Test")
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CategoryIntegrationTest {


    @Container
    @ServiceConnection
    private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.3.0")
            .withDatabaseName("catalog-db-test")
            .withUsername("root")
            .withPassword("root");


    @Autowired
    private CategoryService categoryService;


    @AfterAll
    static void closeContainer(){
        mySQLContainer.close();
    }


    @Test
    void when_requesting_all_categories_should_return_all_category_records() {

        List<CategoryResponse> categories = categoryService.findAllCategories();

        assertThat(categories).hasSize(5);

    }

    @Test
    void when_requesting_an_existing_category_then_should_return_all_category_record() {

        CategoryResponse category = categoryService.findCategoryById(1L);

        assertThat(category).isNotNull();
        assertThat(category.id()).isEqualTo(1L);
        assertThat(category.name()).isEqualTo("Fantasy");


    }


    @Test
    void when_requesting_non_existing_category_then_should_throw_not_found_exception(){

        assertThatThrownBy(() -> categoryService.findCategoryById(1000L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void when_receiving_valid_create_category_request_then_should_create_category_record(){
        CategoryRequest request = CategoryRequest.builder()
                .name("Thriller")
                .build();

        categoryService.addCategory(request);

        CategoryResponse createdCategory = categoryService.findCategoryById(6L);

        assertThat(createdCategory.name()).isEqualTo("Thriller" );


    }


    @Test
    void when_receiving_valid_update_category_request_then_should_update_category_record(){

        CategoryRequest details = CategoryRequest.builder()
                .name("Thriller")
                .build();

        UpdateCategoryRequest request = UpdateCategoryRequest
                .builder()
                .id("1")
                .details(details)
                .build();

        categoryService.updateCategory(request);

        CategoryResponse updatedCategory = categoryService.findCategoryById(1L);

        assertThat(updatedCategory.name()).isEqualTo("Thriller" );

    }

    @Test
    void when_receiving_non_existing_category_update_request_then_should_throw_not_found_exception(){

        CategoryRequest details = CategoryRequest.builder()
                .name("Thriller")
                .build();


        UpdateCategoryRequest request = UpdateCategoryRequest
                .builder()
                .id("1000")
                .details(details)
                .build();

        assertThatThrownBy(() -> categoryService.updateCategory(request))
                .isInstanceOf(NotFoundException.class);

    }


    @Test
    void when_category_id_exists_then_category_should_be_deleted() {

        categoryService.deleteCategory(1L);

        assertThatThrownBy(() -> categoryService.findCategoryById(1L))
                .isInstanceOf(NotFoundException.class);
    }


}
