package com.readrealm.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.readrealm.catalog.dto.author.AuthorRequest;
import com.readrealm.catalog.dto.author.AuthorResponse;
import com.readrealm.catalog.dto.author.UpdateAuthorRequest;
import com.readrealm.catalog.service.AuthorService;
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

@WebMvcTest(value = AuthorController.class)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Author Controller Unit Test")
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorService authorService;

    @BeforeEach
    void setup() {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Test
    void Requesting_all_authors_returns_200() throws Exception {

        AuthorResponse authorResponse = Mockito.mock(AuthorResponse.class);

        when(authorService.findAllAuthors()).thenReturn(Collections.singletonList(authorResponse));

        mockMvc.perform(get("/v1/authors")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_a_valid_Id_of_author_should_return_200() throws Exception {
        AuthorResponse authorResponse = Mockito.mock(AuthorResponse.class);

        when(authorService.findAuthorById(1L)).thenReturn(authorResponse);

        mockMvc.perform(get("/v1/authors/1")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_a_valid_author_body_to_create_should_return_201() throws Exception {
        AuthorResponse authorResponse = Mockito.mock(AuthorResponse.class);
        when(authorService.addAuthor(any(AuthorRequest.class))).thenReturn(authorResponse);

        String authorCreateRequest = """
                {
                    "firstName": "hazem",
                    "lastName": "Khaled"
                }
                """;

        mockMvc.perform(post("/v1/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authorCreateRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_a_valid_author_body_to_update_should_return_200() throws Exception {
        AuthorResponse authorResponse = Mockito.mock(AuthorResponse.class);
        when(authorService.updateAuthor(any(UpdateAuthorRequest.class))).thenReturn(authorResponse);

        String authorUpdateRequest = """
                {
                    "id": 1,
                    "details":{
                        "firstName": "Hazem",
                        "lastName": "Khaled"
                    }
                }
                """;

        mockMvc.perform(put("/v1/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authorUpdateRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_an_author_id_to_delete_should_return_204() throws Exception {
        mockMvc.perform(delete("/v1/authors/1234567890")).andExpect(status().isNoContent());
    }
}
