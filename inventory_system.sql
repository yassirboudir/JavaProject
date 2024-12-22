-- Create the database
CREATE DATABASE Inventory_system;


-- Use the database
USE inventory_system;

-- Create the 'products' table
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create the 'employees' table
CREATE TABLE employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- Store hashed password for security
    role VARCHAR(50) NOT NULL,       -- Add role to distinguish admins, employees, etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data into 'products'
INSERT INTO products (name, category, quantity, price) 
VALUES 
('Laptop', 'Electronics', 10, 999.99),
('Chair', 'Furniture', 50, 49.99),
('Desk', 'Furniture', 20, 129.99);

-- Insert sample data into 'employees'
-- Use hashed passwords for better security in production environments
-- Insert sample data into the 'users' table
INSERT INTO users (username, password, role) 
VALUES 
('admin', 'admin123', 'admin'),
('employee1', 'password1', 'employee');



