package com.readrealm.catalog.Integration;

import com.readrealm.auth.authorizer.Authorizer;
import com.readrealm.catalog.dto.author.AuthorRequest;
import com.readrealm.catalog.dto.author.AuthorResponse;
import com.readrealm.catalog.dto.author.UpdateAuthorRequest;
import com.readrealm.catalog.service.AuthorService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.readrealm.catalog.util.MockAuthorizationUtil.mockAdminAuthorization;
import static com.readrealm.catalog.util.MockAuthorizationUtil.mockCustomerAuthorization;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Author Integration Test")
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AuthorIntegrationTest {

    @Container
    @ServiceConnection
    private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.3.0")
            .withDatabaseName("catalog-db-test")
            .withUsername("root")
            .withPassword("root");

    @Autowired
    private AuthorService authorService;
    @Autowired
    private Authorizer authorizer;

    @AfterAll
    static void closeContainer() {
        mySQLContainer.close();
    }

    @Test
    void when_requesting_all_authors_should_return_all_authors_records() {

        List<AuthorResponse> authors = authorService.findAllAuthors();

        assertThat(authors).hasSize(5);

    }

    @Test
    void when_requesting_an_existing_author_then_should_return_all_author_record() {

        AuthorResponse author = authorService.findAuthorById(1L);

        assertThat(author).isNotNull();
        assertThat(author.id()).isEqualTo(1L);
        assertThat(author.firstName()).isEqualTo("George R.R.");
        assertThat(author.lastName()).isEqualTo("Martin");

    }

    @Test
    void when_requesting_non_existing_author_then_should_throw_not_found_exception() {

        assertThatThrownBy(() -> authorService.findAuthorById(1000L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void Given_admin_authorization_when_receiving_valid_create_author_request_then_should_create_author_record() {

        mockAdminAuthorization();

        AuthorRequest request = AuthorRequest.builder()
                .firstName("Naguib")
                .lastName("Mahfouz")
                .build();

        AuthorResponse createdAuthor = authorService.addAuthor(request);

        assertThat(createdAuthor.firstName()).isEqualTo("Naguib");
        assertThat(createdAuthor.lastName()).isEqualTo("Mahfouz");
    }

    @Test
    void Given_admin_authorization_when_receiving_valid_update_author_request_then_should_update_author_record() {
        mockAdminAuthorization();

        AuthorRequest details = AuthorRequest.builder()
                .firstName("Naguib")
                .lastName("Mahfouz")
                .build();

        UpdateAuthorRequest request = UpdateAuthorRequest
                .builder()
                .id("1")
                .details(details)
                .build();

        AuthorResponse updatedAuthor = authorService.updateAuthor(request);

        assertThat(updatedAuthor.firstName()).isEqualTo("Naguib");
        assertThat(updatedAuthor.lastName()).isEqualTo("Mahfouz");
    }

    @Test
    void Given_admin_authorization_when_receiving_non_existing_author_update_request_then_should_throw_not_found_exception() {
        mockAdminAuthorization();

        AuthorRequest details = AuthorRequest.builder()
                .firstName("Naguib")
                .lastName("Mahfouz")
                .build();

        UpdateAuthorRequest request = UpdateAuthorRequest
                .builder()
                .id("1000")
                .details(details)
                .build();

        assertThatThrownBy(() -> authorService.updateAuthor(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void Given_admin_authorization_when_author_id_exists_then_author_should_be_deleted() {
        mockAdminAuthorization();

        authorService.deleteAuthor(1L);

        assertThatThrownBy(() -> authorService.findAuthorById(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void Given_admin_authorization_when_deleting_non_existing_author_should_not_throw_exception() {
        mockAdminAuthorization();

        assertThatNoException().isThrownBy(() -> authorService.deleteAuthor(1000L));
    }

    @Test
    void Given_admin_authorization_when_creating_author_with_same_name_should_create_new_record() {
        mockAdminAuthorization();

        // First author
        AuthorRequest request1 = AuthorRequest.builder()
                .firstName("Naguib")
                .lastName("Mahfouz")
                .build();

        AuthorResponse createdAuthor1 = authorService.addAuthor(request1);

        AuthorRequest request2 = AuthorRequest.builder()
                .firstName("Naguib")
                .lastName("Mahfouz")
                .build();

        AuthorResponse createdAuthor2 = authorService.addAuthor(request2);

        assertThat(createdAuthor2.id()).isNotEqualTo(createdAuthor1.id());
        assertThat(createdAuthor2.firstName()).isEqualTo("Naguib");
        assertThat(createdAuthor2.lastName()).isEqualTo("Mahfouz");
    }

    @Test
    void Given_customer_authorization_when_creating_author_should_throw_access_denied_exception() {
        mockCustomerAuthorization();

        AuthorRequest request = AuthorRequest.builder()
                .firstName("Naguib")
                .lastName("Mahfouz")
                .build();

        assertThatThrownBy(() -> authorService.addAuthor(request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void Given_customer_authorization_when_updating_author_should_throw_access_denied_exception() {
        mockCustomerAuthorization();

        AuthorRequest details = AuthorRequest.builder()
                .firstName("Naguib")
                .lastName("Mahfouz")
                .build();

        UpdateAuthorRequest request = UpdateAuthorRequest
                .builder()
                .id("1")
                .details(details)
                .build();

        assertThatThrownBy(() -> authorService.updateAuthor(request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void Given_customer_authorization_when_deleting_author_should_throw_access_denied_exception() {
        mockCustomerAuthorization();

        assertThatThrownBy(() -> authorService.deleteAuthor(1L))
                .isInstanceOf(AccessDeniedException.class);
    }


}
