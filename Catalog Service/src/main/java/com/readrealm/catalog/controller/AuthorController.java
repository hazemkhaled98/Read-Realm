package com.readrealm.catalog.controller;

import com.readrealm.catalog.dto.author.AuthorRequest;
import com.readrealm.catalog.dto.author.AuthorResponse;
import com.readrealm.catalog.dto.author.UpdateAuthorRequest;
import com.readrealm.catalog.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/authors")
@RequiredArgsConstructor

public class AuthorController {

    private final AuthorService authorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponse addAuthor(@Valid @RequestBody AuthorRequest authorRequest) {
        return authorService.addAuthor(authorRequest);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public AuthorResponse updateAuthor(@Valid @RequestBody UpdateAuthorRequest updateAuthorRequest) {
        return authorService.updateAuthor(updateAuthorRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AuthorResponse> getAllAuthors() {

        return authorService.findAllAuthors();
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public AuthorResponse getAuthor(@PathVariable long id) {
        return authorService.findAuthorById(id);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable long id) {
        authorService.deleteAuthor(id);
    }
}
