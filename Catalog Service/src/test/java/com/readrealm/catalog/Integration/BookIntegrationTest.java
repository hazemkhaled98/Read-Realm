package com.readrealm.catalog.Integration;

import com.readrealm.catalog.dto.book.BookRequest;
import com.readrealm.catalog.dto.book.BookResponse;
import com.readrealm.catalog.dto.book.BookSearchCriteria;
import com.readrealm.catalog.service.BookService;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Book Integration Test")
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BookIntegrationTest {

    @Container
    @ServiceConnection
    private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.3.0")
            .withDatabaseName("catalog-db-test")
            .withUsername("root")
            .withPassword("root");

    @Autowired
    private BookService bookService;

    @AfterAll
    static void closeContainer() {
        mySQLContainer.close();
    }

    @Test
    void when_found_books_with_matched_criteria_then_should_returns_matched_books() {

        BookSearchCriteria criteria = BookSearchCriteria
                .builder()
                .title("A Game of Thrones")
                .build();

        List<BookResponse> matchedBooks = bookService.searchBooks(criteria);

        assertThat(matchedBooks).hasSize(1);

        assertThat(matchedBooks.get(0).title()).isEqualTo("A Game of Thrones");

    }

    @Test
    void when_no_criteria_then_should_returns_all_books() {

        BookSearchCriteria criteria = BookSearchCriteria
                .builder()
                .build();

        List<BookResponse> matchedBooks = bookService.searchBooks(criteria);

        assertThat(matchedBooks).hasSize(5);
    }

    @Test
    void when_page_size_specified_then_books_count_should_equal_page_size() {

        BookSearchCriteria criteria = BookSearchCriteria
                .builder()
                .pageSize(2)
                .build();

        List<BookResponse> matchedBooks = bookService.searchBooks(criteria);

        assertThat(matchedBooks).hasSize(2);
    }

    @Test
    void when_no_books_matched_criteria_then_should_throw_not_found_exception() {

        BookSearchCriteria criteria = BookSearchCriteria
                .builder()
                .title("Not a real book")
                .build();

        assertThatThrownBy(() -> bookService.searchBooks(criteria))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void when_given_isbn_exists_then_should_returns_book() {

        BookResponse matchedBook = bookService.getBookByIsbn("9780553103540");

        assertThat(matchedBook).isNotNull();
        assertThat(matchedBook.isbn()).isEqualTo("9780553103540");
    }

    @Test
    void when_given_isbn_does_not_exists_then_should_throw_not_found_exception() {

        assertThatThrownBy(() -> bookService.getBookByIsbn("9780553103549"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @Transactional
    void when_given_a_valid_book_request_to_add_then_should_add_book() {

        BookRequest bookRequest = BookRequest.builder()
                .isbn("9780553103549")
                .title("Effective Java")
                .description("A comprehensive guide to best practices in Java programming.")
                .price(new BigDecimal("45.99"))
                .authorsIds(List.of(1L, 2L))
                .categoriesIds(List.of(1L, 2L))
                .build();

        BookResponse addedBook = bookService.addBook(bookRequest);

        assertThat(addedBook).isNotNull();
        assertThat(addedBook.isbn()).isEqualTo("9780553103549");
        assertThat(addedBook.title()).isEqualTo("Effective Java");
        assertThat(addedBook.description()).isEqualTo("A comprehensive guide to best practices in Java programming.");
        assertThat(addedBook.price()).isEqualTo(new BigDecimal("45.99"));
        assertThat(addedBook.authors()).hasSize(2);
        assertThat(addedBook.categories()).hasSize(2);
    }

    @Test
    void when_one_or_more_authors_does_not_exist_in_add_request_then_should_throw_invalid_input_exception() {

        BookRequest bookRequest = BookRequest.builder()
                .isbn("9780553103549")
                .title("Effective Java")
                .description("A comprehensive guide to best practices in Java programming.")
                .price(new BigDecimal("45.99"))
                .authorsIds(List.of(1000L, 2L))
                .categoriesIds(List.of(1L, 2L))
                .build();

        assertThatThrownBy(() -> bookService.addBook(bookRequest))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void when_one_or_more_categories_does_not_exist_in_add_request_then_should_throw_invalid_input_exception() {

        BookRequest bookRequest = BookRequest.builder()
                .isbn("9780553103549")
                .title("Effective Java")
                .description("A comprehensive guide to best practices in Java programming.")
                .price(new BigDecimal("45.99"))
                .authorsIds(List.of(1L, 2L))
                .categoriesIds(List.of(1000L, 2L))
                .build();

        assertThatThrownBy(() -> bookService.addBook(bookRequest))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    @Transactional
    void when_given_a_valid_book_request_to_update_then_should_update_book() {

        BookRequest bookRequest = BookRequest.builder()
                .isbn("9780553103540")
                .title("A Game of Thrones")
                .description("The first book in A Song of Ice and Fire series")
                .price(new BigDecimal("45.99"))
                .authorsIds(List.of(1L))
                .categoriesIds(List.of(1L, 2L, 5L))
                .build();

        BookResponse updatedBook = bookService.updateBook(bookRequest);

        assertThat(updatedBook).isNotNull();
        assertThat(updatedBook.isbn()).isEqualTo("9780553103540");
        assertThat(updatedBook.title()).isEqualTo("A Game of Thrones");
        assertThat(updatedBook.description()).isEqualTo("The first book in A Song of Ice and Fire series");
        assertThat(updatedBook.price()).isEqualTo(new BigDecimal("45.99"));
        assertThat(updatedBook.authors()).hasSize(1);
        assertThat(updatedBook.categories()).hasSize(3);
    }

    @Test
    void when_one_or_more_authors_does_not_exist_in_update_request_then_should_throw_invalid_input_exception() {

        BookRequest bookRequest = BookRequest.builder()
                .isbn("9780553103540")
                .title("A Game of Thrones")
                .description("The first book in A Song of Ice and Fire series")
                .price(new BigDecimal("45.99"))
                .authorsIds(List.of(1000L))
                .categoriesIds(List.of(1L, 2L, 5L))
                .build();

        assertThatThrownBy(() -> bookService.updateBook(bookRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void when_one_or_more_categories_does_not_exist_in_update_request_then_should_throw_invalid_input_exception() {

        BookRequest bookRequest = BookRequest.builder()
                .isbn("9780553103540")
                .title("A Game of Thrones")
                .description("The first book in A Song of Ice and Fire series")
                .price(new BigDecimal("45.99"))
                .authorsIds(List.of(1L))
                .categoriesIds(List.of(1000L, 2L, 5L))
                .build();

        assertThatThrownBy(() -> bookService.updateBook(bookRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void when_isbn_exists_then_book_should_be_deleted() {

        bookService.deleteBook("9780553103540");

        assertThatThrownBy(() -> bookService.getBookByIsbn("9780553103540"))
                .isInstanceOf(EntityNotFoundException.class);
    }

}
