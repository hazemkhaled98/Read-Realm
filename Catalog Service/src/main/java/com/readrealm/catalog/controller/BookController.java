package com.readrealm.catalog.controller;


import com.readrealm.catalog.dto.book.BookRequest;
import com.readrealm.catalog.dto.book.BookResponse;
import com.readrealm.catalog.dto.book.BookSearchCriteria;
import com.readrealm.catalog.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/books")
@RequiredArgsConstructor
public class BookController {


    private final BookService bookService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BookResponse> getBooksByCriteria(@Valid @ModelAttribute BookSearchCriteria criteria) {
        return bookService.searchBooks(criteria);
    }


    @GetMapping("{isbn}")
    @ResponseStatus(HttpStatus.OK)
    public BookResponse getBookByIsbn(@PathVariable String isbn) {
        return bookService.getBookByIsbn(isbn);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String addBook(@Valid @RequestBody BookRequest bookRequest) {
        return bookService.addBook(bookRequest);
    }


    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public String updateBook(@Valid @RequestBody BookRequest bookRequest){
        return bookService.updateBook(bookRequest);
    }


    @DeleteMapping("{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable String isbn){
        bookService.deleteBook(isbn);
    }


}
