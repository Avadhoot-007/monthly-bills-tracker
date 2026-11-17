-- Monthly Bills Tracker database script
-- Run this once in MySQL Workbench or the MySQL command line
-- It makes a fresh database. It does not delete anything

-- Create and select the database
CREATE DATABASE IF NOT EXISTS monthly_bills_db;
USE monthly_bills_db;

-- One row per registered user
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL   -- BCrypt hash, never the real password
);

-- Every bill belongs to one user
CREATE TABLE bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    bill_type VARCHAR(30) NOT NULL,   -- Utilities, Investment, Loan, Subscription
    amount DECIMAL(10, 2) NOT NULL,
    due_day INT NOT NULL,             -- day of month, 1 to 31
    paid_month VARCHAR(7),            -- example: 2026-11, empty if never paid
    notes VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id)
);