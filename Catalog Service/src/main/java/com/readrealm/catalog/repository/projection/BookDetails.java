package com.readrealm.catalog.repository.projection;

import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.List;

public interface BookDetails {
    Long getId();
    String getIsbn();
    String getTitle();
    String getDescription();
    BigDecimal getPrice();
    List<AuthorInfo> getAuthors();
    List<CategoryInfo> getCategories();

    interface AuthorInfo {
        Long getId();

        @Value("#{target.firstName + ' ' + target.lastName}")
        String getFullName();
    }

    interface CategoryInfo {
        Long getId();
        String getName();
    }
}

