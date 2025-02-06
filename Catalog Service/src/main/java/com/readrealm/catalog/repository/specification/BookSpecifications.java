package com.readrealm.catalog.repository.specification;

import com.readrealm.catalog.dto.book.BookSearchCriteria;
import com.readrealm.catalog.entity.Author;
import com.readrealm.catalog.entity.Book;
import com.readrealm.catalog.entity.Category;
import com.readrealm.catalog.repository.projection.BookDetails;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecifications {
    private BookSpecifications() {}

    public static Specification<Book> withSearchCriteria(BookSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();


            query.distinct(true);


            // Eager fetch to avoid the n + 1 problem
            if (query.getResultType().equals(BookDetails.class)) {
                root.fetch("authors", JoinType.LEFT);
                root.fetch("categories", JoinType.LEFT);
            }

            if (criteria.title() != null && !criteria.title().trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + criteria.title().toLowerCase() + "%"
                ));
            }

            if (criteria.authorName() != null && !criteria.authorName().trim().isEmpty()) {
                Join<Book, Author> authorJoin = getAuthorJoin(root);
                String authorNamePattern = "%" + criteria.authorName().toLowerCase() + "%";

                predicates.add(cb.or(
                        cb.like(cb.lower(authorJoin.get("firstName")), authorNamePattern),
                        cb.like(cb.lower(authorJoin.get("lastName")), authorNamePattern),
                        cb.like(
                                cb.lower(cb.concat(cb.concat(authorJoin.get("firstName"), " "), authorJoin.get("lastName"))),
                                authorNamePattern
                        )
                ));
            }


            if (criteria.category() != null && !criteria.category().trim().isEmpty()) {
                Join<Book, Category> categoryJoin = getCategoryJoin(root);
                predicates.add(cb.equal(
                        cb.lower(categoryJoin.get("name")),
                        criteria.category().toLowerCase()
                ));
            }

            if (criteria.sortBy() != null) {
                Path<?> sortPath;

                if(criteria.sortBy().equalsIgnoreCase("title"))
                    sortPath = root.get("title");
                else
                    sortPath = root.get("price");

                query.orderBy(
                        criteria.sortOrder() != null && criteria.sortOrder().equalsIgnoreCase("DESC")
                                ? cb.desc(sortPath)
                                : cb.asc(sortPath)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Join<Book, Author> getAuthorJoin(Root<Book> root) {
        return (Join<Book, Author>) root.getJoins().stream()
                .filter(join -> join.getAttribute().getName().equals("authors"))
                .findFirst()
                .orElseGet(() -> root.join("authors", JoinType.LEFT));
    }

    private static Join<Book, Category> getCategoryJoin(Root<Book> root) {
        return (Join<Book, Category>) root.getJoins().stream()
                .filter(join -> join.getAttribute().getName().equals("categories"))
                .findFirst()
                .orElseGet(() -> root.join("categories", JoinType.LEFT));
    }
}