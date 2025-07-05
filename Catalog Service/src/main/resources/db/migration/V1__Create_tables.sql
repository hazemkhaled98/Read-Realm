-- V1__Create_Tables.sql

USE `catalog-db`;

SET GLOBAL TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- Create Authors table
CREATE TABLE authors
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Categories table
CREATE TABLE categories
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Books table
CREATE TABLE books
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn        VARCHAR(13)    NOT NULL UNIQUE,
    title       VARCHAR(255)   NOT NULL,
    description TEXT,
    price       DECIMAL(10, 2) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_price_positive CHECK (price >= 0)
);

-- Create books_authors junction table for many-to-many relationship
CREATE TABLE books_authors
(
    author_id BIGINT NOT NULL,
    book_id   BIGINT NOT NULL,
    PRIMARY KEY (author_id, book_id),
    CONSTRAINT fk_author_book_author FOREIGN KEY (author_id) REFERENCES authors (id) ON DELETE CASCADE,
    CONSTRAINT fk_author_book_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

-- Create Books_Categories junction table for many-to-many relationship
CREATE TABLE books_categories
(
    category_id BIGINT NOT NULL,
    book_id     BIGINT NOT NULL,
    PRIMARY KEY (book_id, category_id),
    CONSTRAINT fk_book_category_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT fk_book_category_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_books_isbn ON books (isbn);
CREATE INDEX idx_books_title ON books (title);
CREATE INDEX idx_books_authors_author_id ON books_authors (author_id);
CREATE INDEX idx_books_authors_book_id ON books_authors (book_id);
