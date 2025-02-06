-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS `catalog-db-test`;


-- Switch to the created database
USE `catalog-db-test`;


-- V1__Create_Tables.sql

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


-- V2__Insert_Data.sql

-- Insert Authors
INSERT INTO authors (first_name, last_name)
VALUES ('George R.R.', 'Martin'),
       ('J.K.', 'Rowling'),
       ('J.R.R.', 'Tolkien'),
       ('Stephen', 'King'),
       ('Agatha', 'Christie');

-- Insert Categories
INSERT INTO categories (name)
VALUES ('Fantasy'),
       ('Mystery'),
       ('Horror'),
       ('Science Fiction'),
       ('Adventure');

-- Insert Books
INSERT INTO books (isbn, title, description, price)
VALUES ('9780553103540', 'A Game of Thrones', 'The first book in A Song of Ice and Fire series', 29.99),
       ('9780439064873', 'Harry Potter and the Chamber of Secrets', 'The second book in the Harry Potter series',
        24.99),
       ('9780395489321', 'The Lord of the Rings', 'Epic high-fantasy novel', 35.99),
       ('9781982110567', 'The Shining', 'A horror novel about a haunted hotel', 19.99),
       ('9780062073488', 'Murder on the Orient Express', 'A Hercule Poirot mystery', 14.99);

-- Insert Author-Book relationships (many-to-many)
INSERT INTO books_authors (author_id, book_id)
VALUES ((SELECT id FROM authors WHERE last_name = 'Martin'), (SELECT id FROM books WHERE isbn = '9780553103540')),
       ((SELECT id FROM authors WHERE last_name = 'Rowling'), (SELECT id FROM books WHERE isbn = '9780439064873')),
       ((SELECT id FROM authors WHERE last_name = 'Tolkien'), (SELECT id FROM books WHERE isbn = '9780395489321')),
       ((SELECT id FROM authors WHERE last_name = 'King'), (SELECT id FROM books WHERE isbn = '9781982110567')),
       ((SELECT id FROM authors WHERE last_name = 'Christie'), (SELECT id FROM books WHERE isbn = '9780062073488'));

-- Insert Book-Category relationships (many-to-many)
INSERT INTO books_categories (book_id, category_id)
VALUES ((SELECT id FROM books WHERE isbn = '9780553103540'), (SELECT id FROM categories WHERE name = 'Fantasy')),
       ((SELECT id FROM books WHERE isbn = '9780553103540'), (SELECT id FROM categories WHERE name = 'Adventure')),
       ((SELECT id FROM books WHERE isbn = '9780439064873'), (SELECT id FROM categories WHERE name = 'Fantasy')),
       ((SELECT id FROM books WHERE isbn = '9780395489321'), (SELECT id FROM categories WHERE name = 'Fantasy')),
       ((SELECT id FROM books WHERE isbn = '9780395489321'), (SELECT id FROM categories WHERE name = 'Adventure')),
       ((SELECT id FROM books WHERE isbn = '9781982110567'), (SELECT id FROM categories WHERE name = 'Horror')),
       ((SELECT id FROM books WHERE isbn = '9780062073488'), (SELECT id FROM categories WHERE name = 'Mystery'));
