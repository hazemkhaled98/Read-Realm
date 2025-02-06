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
