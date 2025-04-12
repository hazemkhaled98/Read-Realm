package com.readrealm.catalog.Integration;

import com.readrealm.catalog.dto.category.CategoryRequest;
import com.readrealm.catalog.dto.category.CategoryResponse;
import com.readrealm.catalog.dto.category.UpdateCategoryRequest;
import com.readrealm.catalog.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.readrealm.auth.util.MockAuthorizationUtil.mockAdminAuthorization;
import static com.readrealm.auth.util.MockAuthorizationUtil.mockCustomerAuthorization;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Category Integration Test")
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CategoryIntegrationTest extends AbstractContainerBaseTest {

    @Autowired
    private CategoryService categoryService;

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
    void when_requesting_non_existing_category_then_should_throw_not_found_exception() {

        assertThatThrownBy(() -> categoryService.findCategoryById(1000L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void Given_admin_authorization_when_receiving_valid_create_category_request_then_should_create_category_record() {
        mockAdminAuthorization();
        CategoryRequest request = CategoryRequest.builder()
                .name("Thriller")
                .build();

        CategoryResponse createdCategory = categoryService.addCategory(request);

        assertThat(createdCategory.name()).isEqualTo("Thriller");
    }

    @Test
    void Given_admin_authorization_when_receiving_valid_update_category_request_then_should_update_category_record() {
        mockAdminAuthorization();

        CategoryRequest details = CategoryRequest.builder()
                .name("Thriller")
                .build();

        UpdateCategoryRequest request = UpdateCategoryRequest
                .builder()
                .id("1")
                .details(details)
                .build();

        CategoryResponse updatedCategory = categoryService.updateCategory(request);

        assertThat(updatedCategory.name()).isEqualTo("Thriller");

    }

    @Test
    void Given_admin_authorization_when_receiving_non_existing_category_update_request_then_should_throw_not_found_exception() {
        mockAdminAuthorization();

        CategoryRequest details = CategoryRequest.builder()
                .name("Thriller")
                .build();

        UpdateCategoryRequest request = UpdateCategoryRequest
                .builder()
                .id("1000")
                .details(details)
                .build();

        assertThatThrownBy(() -> categoryService.updateCategory(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);

    }

    @Test
    void Given_admin_authorization_when_category_id_exists_then_category_should_be_deleted() {
        mockAdminAuthorization();

        categoryService.deleteCategory(1L);

        assertThatThrownBy(() -> categoryService.findCategoryById(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void Given_admin_authorization_when_deleting_non_existing_category_should_not_throw_exception() {
        mockAdminAuthorization();

        assertThatNoException().isThrownBy(() -> categoryService.deleteCategory(1000L));
    }

    @Test
    void Given_admin_authorization_when_creating_category_with_same_name_should_throw_exception() {
        mockAdminAuthorization();

        CategoryRequest request1 = CategoryRequest.builder()
                .name("Thriller")
                .build();

        categoryService.addCategory(request1);

        CategoryRequest request2 = CategoryRequest.builder()
                .name("Thriller")
                .build();

        assertThatThrownBy(() -> categoryService.addCategory(request2))
        .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("Category with name Thriller already exists");
    }

    @Test
    void Given_customer_authorization_when_creating_category_should_throw_access_denied_exception() {
        mockCustomerAuthorization();

        CategoryRequest request = CategoryRequest.builder()
                .name("Thriller")
                .build();

        assertThatThrownBy(() -> categoryService.addCategory(request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void Given_customer_authorization_when_updating_category_should_throw_access_denied_exception() {
        mockCustomerAuthorization();

        CategoryRequest details = CategoryRequest.builder()
                .name("Thriller")
                .build();

        UpdateCategoryRequest request = UpdateCategoryRequest
                .builder()
                .id("1")
                .details(details)
                .build();

        assertThatThrownBy(() -> categoryService.updateCategory(request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void Given_customer_authorization_when_deleting_category_should_throw_access_denied_exception() {
        mockCustomerAuthorization();

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(AccessDeniedException.class);
    }
}
