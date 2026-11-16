-- Monthly Bills Tracker database script
-- Run this once in MySQL Workbench or the MySQL command line

-- Create and select the database
CREATE DATABASE IF NOT EXISTS monthly_bills_db;
USE monthly_bills_db;

-- One table holds all bills
CREATE TABLE bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    bill_type VARCHAR(30) NOT NULL,   -- Utilities, Investment, Loan, Subscription
    amount DECIMAL(10, 2) NOT NULL,
    due_day INT NOT NULL,             -- day of month, 1 to 31
    paid_month VARCHAR(7),            -- example: 2026-11, empty if never paid
    notes VARCHAR(255)
);

-- Sample data for testing
INSERT INTO bills (title, bill_type, amount, due_day, paid_month, notes) VALUES
('Electricity Bill', 'Utilities', 1850.00, 5, NULL, 'Monthly bill'),
('MNGL Piped Gas', 'Utilities', 450.00, 12, NULL, 'Gas bill'),
('LPG Cylinder', 'Utilities', 950.00, 20, NULL, 'Cylinder refill'),
('Water Bill', 'Utilities', 300.00, 8, NULL, 'Fixed monthly amount'),
('Mutual Fund SIP', 'Investment', 5000.00, 10, NULL, 'Monthly SIP'),
('Home Loan EMI', 'Loan', 22000.00, 3, NULL, 'Bank EMI'),
('Netflix', 'Subscription', 649.00, 25, NULL, 'OTT plan'),
('Recurring Deposit', 'Investment', 2000.00, 31, NULL, 'Tests the 31st rule');

-- Check the data loaded
SELECT * FROM bills;
