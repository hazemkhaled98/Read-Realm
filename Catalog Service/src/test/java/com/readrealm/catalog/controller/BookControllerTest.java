package com.readrealm.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.readrealm.catalog.dto.book.BookRequest;
import com.readrealm.catalog.dto.book.BookResponse;
import com.readrealm.catalog.dto.book.BookSearchCriteria;
import com.readrealm.catalog.service.BookService;
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

@WebMvcTest(value = BookController.class)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Book Controller Unit Test")
class BookControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private BookService bookService;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        public void setup() {
                objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        }

        @Test
        void Requesting_all_books_returns_200() throws Exception {
                BookResponse bookDetails = Mockito.mock(BookResponse.class);
                when(bookService.searchBooks(any(BookSearchCriteria.class)))
                                .thenReturn(Collections.singletonList(bookDetails));

                mockMvc.perform(get("/v1/books")
                                .param("title", "Sample Title")
                                .param("author", "Sample Author")
                                .param("genre", "Fiction"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void Given_a_valid_ISBN_of_book_should_return_200() throws Exception {
                BookResponse bookResponse = Mockito.mock(BookResponse.class);

                when(bookService.getBookByIsbn("9780553103540"))
                                .thenReturn(bookResponse);

                mockMvc.perform(get("/v1/books/9780553103540"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void Given_a_valid_book_body_to_create_should_return_201() throws Exception {
                BookResponse bookResponse = Mockito.mock(BookResponse.class);
                when(bookService.addBook(any(BookRequest.class))).thenReturn(bookResponse);

                String bookRequestJson = """
                                {
                                     "description": "The second book in A Song of Ice and Fire series",
                                     "title": "A Game of Thrones III",
                                     "isbn": "9780553103540",
                                     "price": 29.99,
                                     "categoriesIds": [1, 2, 3],
                                     "authorsIds": [1]
                                 }
                                """;

                mockMvc.perform(post("/v1/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bookRequestJson))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void Given_a_valid_book_body_to_update_should_return_200() throws Exception {
                BookResponse bookResponse = Mockito.mock(BookResponse.class);
                when(bookService.updateBook(any(BookRequest.class))).thenReturn(bookResponse);

                String bookRequestJson = """
                                {
                                     "description": "The second book in A Song of Ice and Fire series",
                                     "title": "A Game of Thrones III",
                                     "isbn": "9780553103540",
                                     "price": 29.99,
                                     "categoriesIds": [1, 2, 3],
                                     "authorsIds": [1]
                                 }
                                """;

                mockMvc.perform(put("/v1/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bookRequestJson))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void Given_an_isbn_to_delete_should_return_204() throws Exception {
                mockMvc.perform(delete("/v1/books/1234567890"))
                                .andExpect(status().isNoContent());
        }
}
