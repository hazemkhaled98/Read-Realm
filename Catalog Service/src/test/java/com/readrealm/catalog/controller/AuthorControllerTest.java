package com.readrealm.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.readrealm.auth.config.SecurityConfig;
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
import org.springframework.context.annotation.Import;
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

@WebMvcTest(value = AuthorController.class)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Author Controller Unit Test")
@Import(SecurityConfig.class)
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

        mockMvc.perform(get("/v1/authors")
                .with(jwt().jwt(mockCustomerJWT()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void Given_a_valid_Id_of_author_should_return_200() throws Exception {
        AuthorResponse authorResponse = Mockito.mock(AuthorResponse.class);

        when(authorService.findAuthorById(1L)).thenReturn(authorResponse);

        mockMvc.perform(get("/v1/authors/1")
                .with(jwt().jwt(mockCustomerJWT()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
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
                .content(authorCreateRequest)
                .with(jwt().jwt(mockAdminJWT())))
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
                .content(authorUpdateRequest)
                .with(jwt().jwt(mockAdminJWT())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void Given_invalid_author_data_should_return_400() throws Exception {
        String invalidAuthorRequest = """
                {
                    "firstName": "h",
                    "lastName": "K"
                }
                """;

        mockMvc.perform(post("/v1/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidAuthorRequest)
                .with(jwt().jwt(mockAdminJWT())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void When_customer_tries_to_create_author_should_return_403() throws Exception {
        String authorCreateRequest = """
                {
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        when(authorService.addAuthor(any(AuthorRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer cannot add author"));

        mockMvc.perform(post("/v1/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authorCreateRequest)
                .with(jwt().jwt(mockCustomerJWT())))
                .andExpect(status().isForbidden());
    }


    @Test
    void When_customer_tries_to_update_author_should_return_403() throws Exception {
        String authorUpdateRequest = """
                {
                    "id": 1,
                    "details":{
                        "firstName": "John",
                        "lastName": "Doe"
                    }
                }
                """;


        when(authorService.updateAuthor(any(UpdateAuthorRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer cannot update author"));

        mockMvc.perform(put("/v1/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authorUpdateRequest)
                .with(jwt().jwt(mockCustomerJWT())))
                .andExpect(status().isForbidden());
    }

    @Test
    void When_customer_tries_to_delete_author_should_return_403() throws Exception {

        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer cannot delete author"))
                .when(authorService).deleteAuthor(1);


        mockMvc.perform(delete("/v1/authors/1")
                .with(jwt().jwt(mockCustomerJWT()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void Given_nonexistent_author_id_should_return_404() throws Exception {
        when(authorService.findAuthorById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Author with id 999 not found"));

        mockMvc.perform(get("/v1/authors/999")
                .with(jwt().jwt(mockCustomerJWT()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void Given_invalid_update_request_should_return_400() throws Exception {
        String invalidUpdateRequest = """
                {
                    "id": "",
                    "details":{
                        "firstName": "John",
                        "lastName": "Doe"
                    }
                }
                """;

        mockMvc.perform(put("/v1/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidUpdateRequest)
                .with(jwt().jwt(mockAdminJWT())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void When_no_jwt_provided_should_return_401() throws Exception {
        mockMvc.perform(get("/v1/authors"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void Given_an_author_id_to_delete_should_return_204() throws Exception {
        mockMvc.perform(delete("/v1/authors/1234567890")
                .with(jwt().jwt(mockAdminJWT()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
