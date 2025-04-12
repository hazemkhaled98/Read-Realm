DROP DATABASE IF EXISTS `inventory-db-test`;

-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS `inventory-db-test`;


-- Switch to the created database
USE `inventory-db-test`;

CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(13) NOT NULL UNIQUE,
    quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert initial inventory data for existing books
INSERT INTO inventory (isbn, quantity)
VALUES
    ('9780553103540', 50),  -- A Game of Thrones
    ('9780439064873', 75),  -- Harry Potter and the Chamber of Secrets
    ('9780395489321', 60),  -- The Lord of the Rings
    ('9781982110567', 45),  -- The Shining
    ('9780062073488', 55);  -- Murder on the Orient Express