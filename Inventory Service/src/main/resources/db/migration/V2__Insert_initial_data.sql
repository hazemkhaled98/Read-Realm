-- V2__Insert_initial_data.sql

-- Insert initial inventory data for existing books
INSERT INTO inventory (isbn, quantity)
VALUES 
    ('9780553103540', 50),  -- A Game of Thrones
    ('9780439064873', 75),  -- Harry Potter and the Chamber of Secrets
    ('9780395489321', 60),  -- The Lord of the Rings
    ('9781982110567', 45),  -- The Shining
    ('9780062073488', 55);  -- Murder on the Orient Express