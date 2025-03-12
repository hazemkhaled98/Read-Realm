-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS `catalog-db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Switch to the created database
USE `catalog-db`;

SET GLOBAL TRANSACTION ISOLATION LEVEL READ COMMITTED;