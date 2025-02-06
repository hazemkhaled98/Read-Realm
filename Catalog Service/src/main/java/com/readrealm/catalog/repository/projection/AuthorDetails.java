package com.readrealm.catalog.repository.projection;

import java.math.BigDecimal;
import java.util.List;

public interface AuthorDetails {


    long getId();

    String getFirstName();

    String getLastName();

    List<BookInfo> getBooks();


    interface BookInfo {

        String getIsbn();

        String getTitle();

        BigDecimal getPrice();
    }


}
