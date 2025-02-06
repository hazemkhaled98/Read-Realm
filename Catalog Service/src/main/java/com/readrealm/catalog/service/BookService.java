package com.readrealm.catalog.service;


import com.readrealm.catalog.dto.book.BookRequest;
import com.readrealm.catalog.dto.book.BookResponse;
import com.readrealm.catalog.dto.book.BookSearchCriteria;
import com.readrealm.catalog.entity.Author;
import com.readrealm.catalog.entity.Book;
import com.readrealm.catalog.entity.Category;
import com.readrealm.catalog.exception.InvalidInputException;
import com.readrealm.catalog.exception.NotFoundException;
import com.readrealm.catalog.mapper.BookMapper;
import com.readrealm.catalog.repository.AuthorRepository;
import com.readrealm.catalog.repository.BookRepository;
import com.readrealm.catalog.repository.CategoryRepository;
import com.readrealm.catalog.repository.projection.BookDetails;
import com.readrealm.catalog.repository.specification.BookSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(isolation = Isolation.REPEATABLE_READ)
public class BookService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;


    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "booksByCriteria", cacheManager = "cacheManager")
    public List<BookResponse> searchBooks(BookSearchCriteria criteria) {
        log.info("Searching for books with criteria: {}", criteria);

        List<BookDetails> matchedBooks = bookRepository.findBy(
                BookSpecifications.withSearchCriteria(criteria),
                query -> query
                        .as(BookDetails.class)
                        .page(createPageable(criteria))
                        .toList()
        );


        if (matchedBooks.isEmpty()) {
            throw new NotFoundException("No books are found with specified criteria");
        }

        return matchedBooks
                .stream()
                .map(bookMapper::toBookResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "bookByISBN", cacheManager = "cacheManager")
    public BookResponse getBookByIsbn(String isbn) {
        log.info("Searching for book by ISBN: {}", isbn);

        return bookRepository.findBookDetailsByIsbn(isbn)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new NotFoundException("No book found with ISBN: " + isbn));
    }

    @Transactional
    public String addBook(BookRequest bookRequest) {
        log.info("Creating book: {}", bookRequest);


        List<Author> authors = authorRepository.findAllById(bookRequest.authorsIds());
        if (authors.size() != bookRequest.authorsIds().size()) {
            throw new InvalidInputException("One or more authors not found");
        }

        List<Category> categories = categoryRepository.findAllById(bookRequest.categoriesIds());
        if (categories.size() != bookRequest.categoriesIds().size()) {
            throw new InvalidInputException("One or more categories not found");
        }

        Book book = Book.builder()
                .isbn(bookRequest.isbn())
                .title(bookRequest.title())
                .description(bookRequest.description())
                .price(bookRequest.price())
                .authors(authors)
                .categories(categories)
                .build();


        bookRepository.save(book);

        return "Book with isbn: " + book.getIsbn() + " created successfully";
    }

    @Transactional
    public String updateBook(BookRequest bookRequest) {
        log.info("Updating book: {}", bookRequest);


        List<Author> authors = authorRepository.findAllById(bookRequest.authorsIds());
        if (authors.size() != bookRequest.authorsIds().size()) {
            throw new InvalidInputException("One or more authors not found");
        }

        List<Category> categories = categoryRepository.findAllById(bookRequest.categoriesIds());
        if (categories.size() != bookRequest.categoriesIds().size()) {
            throw new InvalidInputException("One or more categories not found");
        }

        Book updatedBook = bookRepository.findBookByIsbn(bookRequest.isbn())
                .orElseThrow(() -> new InvalidInputException("Book with isbn: " + bookRequest.isbn() + " not found"));

        updatedBook.setTitle(bookRequest.title());
        updatedBook.setDescription(bookRequest.description());
        updatedBook.setPrice(bookRequest.price());
        updatedBook.setAuthors(authors);
        updatedBook.setCategories(categories);

        bookRepository.save(updatedBook);

        return "Book with isbn: " + bookRequest.isbn() + " updated successfully";
    }

    @Transactional
    public void deleteBook(String isbn) {
        bookRepository.deleteBookByIsbn(isbn);
    }

    private static Pageable createPageable(BookSearchCriteria criteria) {

        int pageNumber = criteria.pageNumber() != null ? (criteria.pageNumber() - 1) : 0;
        int pageSize = criteria.pageSize() != null ? criteria.pageSize() : DEFAULT_PAGE_SIZE;

        return PageRequest.of(pageNumber, pageSize);
    }
}
