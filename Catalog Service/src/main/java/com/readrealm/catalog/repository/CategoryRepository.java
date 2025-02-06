package com.readrealm.catalog.repository;

import com.readrealm.catalog.entity.Category;
import com.readrealm.catalog.repository.projection.CategoryDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {


    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.books")
    List<CategoryDetails> findAllCategoriesDetails();


    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.books WHERE c.id = :id")
    Optional<CategoryDetails> findCategoryDetailsById(@Param("id") Long id);
}
