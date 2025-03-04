package com.readrealm.catalog.service;

import com.readrealm.catalog.dto.book.BookRequest;
import com.readrealm.catalog.dto.book.BookResponse;
import com.readrealm.catalog.dto.book.BookSearchCriteria;
import com.readrealm.catalog.entity.Author;
import com.readrealm.catalog.entity.Book;
import com.readrealm.catalog.entity.Category;
import com.readrealm.catalog.mapper.BookMapper;
import com.readrealm.catalog.repository.AuthorRepository;
import com.readrealm.catalog.repository.BookRepository;
import com.readrealm.catalog.repository.CategoryRepository;
import com.readrealm.catalog.repository.projection.BookDetails;
import com.readrealm.catalog.repository.specification.BookSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
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

    @Transactional
    @CachePut(cacheNames = "bookByISBN", key = "#bookRequest.isbn()", cacheManager = "cacheManager")
    @CacheEvict(cacheNames = "booksByCriteria", allEntries = true, cacheManager = "cacheManager")
    public BookResponse addBook(BookRequest bookRequest) {
        log.info("Creating book: {}", bookRequest);

        List<Author> authors = authorRepository.findAllById(bookRequest.authorsIds());
        checkIfAuthorsExist(authors, bookRequest.authorsIds());

        List<Category> categories = categoryRepository.findAllById(bookRequest.categoriesIds());
        checkIfCategoriesExist(categories, bookRequest.categoriesIds());

        Book book = Book.builder()
                .isbn(bookRequest.isbn())
                .title(bookRequest.title())
                .description(bookRequest.description())
                .price(bookRequest.price())
                .authors(authors)
                .categories(categories)
                .build();

        bookRepository.save(book);

        return bookMapper.toBookResponse(book);
    }

    @Transactional
    @CachePut(cacheNames = "bookByISBN", key = "#bookRequest.isbn()", cacheManager = "cacheManager")
    @CacheEvict(cacheNames = "booksByCriteria", allEntries = true, cacheManager = "cacheManager")
    public BookResponse updateBook(BookRequest bookRequest) {
        log.info("Updating book: {}", bookRequest);

        List<Author> authors = authorRepository.findAllById(bookRequest.authorsIds());
        checkIfAuthorsExist(authors, bookRequest.authorsIds());

        List<Category> categories = categoryRepository.findAllById(bookRequest.categoriesIds());
        checkIfCategoriesExist(categories, bookRequest.categoriesIds());

        Book updatedBook = bookRepository.findBookByIsbn(bookRequest.isbn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Book with isbn: " + bookRequest.isbn() + " not found"));

        updatedBook.setTitle(bookRequest.title());
        updatedBook.setDescription(bookRequest.description());
        updatedBook.setPrice(bookRequest.price());
        updatedBook.setAuthors(authors);
        updatedBook.setCategories(categories);

        bookRepository.save(updatedBook);

        return bookMapper.toBookResponse(updatedBook);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "bookByISBN", key = "#isbn", cacheManager = "cacheManager")
    public BookResponse getBookByIsbn(String isbn) {
        log.info("Searching for book by ISBN: {}", isbn);

        return bookRepository.findBookDetailsByIsbn(isbn)
                .map(bookMapper::toBookResponse)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No book found with ISBN: " + isbn));
    }

    public List<BookResponse> getBooks(Collection<String> ISBNs) {
        List<BookDetails> books = bookRepository.findBooksByISBNs(ISBNs);

        if(books.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No books found with specified ISBNs");
        }

        if(books.size() < ISBNs.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more books not found with specified ISBNs");
        }

        return bookMapper.toBookResponseList(books);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "bookByISBN", key = "#isbn", cacheManager = "cacheManager"),
            @CacheEvict(cacheNames = "booksByCriteria", allEntries = true, cacheManager = "cacheManager")
    })
    public void deleteBook(String isbn) {
        bookRepository.deleteBookByIsbn(isbn);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "booksByCriteria", key = "#criteria", cacheManager = "cacheManager")
    public List<BookResponse> searchBooks(BookSearchCriteria criteria) {
        log.info("Searching for books with criteria: {}", criteria);

        List<BookDetails> matchedBooks = bookRepository.findBy(
                BookSpecifications.withSearchCriteria(criteria),
                query -> query
                        .as(BookDetails.class)
                        .page(createPageable(criteria))
                        .toList());

        if (matchedBooks.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No books are found with specified criteria");
        }

        return matchedBooks
                .stream()
                .map(bookMapper::toBookResponse)
                .toList();
    }

    private static Pageable createPageable(BookSearchCriteria criteria) {

        int pageNumber = criteria.pageNumber() != null ? (criteria.pageNumber() - 1) : 0;
        int pageSize = criteria.pageSize() != null ? criteria.pageSize() : DEFAULT_PAGE_SIZE;

        return PageRequest.of(pageNumber, pageSize);
    }

    private static void checkIfAuthorsExist(List<Author> authors, List<Long> authorsIds) {
        if (authors.size() != authorsIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more authors not found");
        }
    }

    private static void checkIfCategoriesExist(List<Category> categories, List<Long> categoriesIds) {
        if (categories.size() != categoriesIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more categories not found");
        }
    }
}
