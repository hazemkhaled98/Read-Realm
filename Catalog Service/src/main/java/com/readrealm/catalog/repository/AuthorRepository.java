package com.readrealm.catalog.repository;

import com.readrealm.catalog.entity.Author;
import com.readrealm.catalog.repository.projection.AuthorDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {



    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books")
    List<AuthorDetails> findAllAuthorDetails();


    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books WHERE a.id = :id")
    Optional<AuthorDetails> findAuthorDetailsById(@Param("id") Long id);
}
