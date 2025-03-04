package com.readrealm.catalog.repository;

import com.readrealm.catalog.entity.Book;
import com.readrealm.catalog.repository.projection.BookDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<BookDetails> findBookDetailsByIsbn(String isbn);

    Optional<Book> findBookByIsbn(String isbn);

    void deleteBookByIsbn(String isbn);

    @Query("SELECT b FROM Book b WHERE b.isbn IN :isbns")
    List<BookDetails> findBooksByISBNs(Collection<String> isbns);
}

