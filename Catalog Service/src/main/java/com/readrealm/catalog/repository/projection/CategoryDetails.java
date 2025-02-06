package com.readrealm.catalog.repository.projection;

import java.math.BigDecimal;
import java.util.List;

public interface CategoryDetails {

    long getId();

    String getName();

    List<BookInfo> getBooks();


    interface BookInfo {

        String getIsbn();

        String getTitle();

        BigDecimal getPrice();
    }
}
