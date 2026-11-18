-- Monthly Bills Tracker sample data
-- Adds 8 sample bills for the user named demo
-- Step 1: run schema.sql. Step 2: register a user named demo in the app.
-- Step 3: run this file once. Running it twice adds the bills twice.

USE monthly_bills_db;

-- Find the id of the demo user and keep it in a variable
-- If demo does not exist, the inserts below fail. That is fine
SET @demo_id = (SELECT id FROM users WHERE username = 'demo');

-- Month values are built from today's date, so the demo works in any month
-- paid_month = paid through this month. NULL = nothing paid yet
-- created_month = month the bill was added

INSERT INTO bills (user_id, title, bill_type, amount, due_day, paid_month, created_month, notes)
VALUES
-- Created last month, nothing paid: shows "Overdue: 1 month unpaid"
(@demo_id, 'Electricity', 'Utilities', 1850.00, 5,
 NULL,
 DATE_FORMAT(CURDATE() - INTERVAL 1 MONTH, '%Y-%m'),
 'Monthly electricity bill'),

-- Paid through last month: this month is still unpaid
(@demo_id, 'Piped Gas', 'Utilities', 620.00, 12,
 DATE_FORMAT(CURDATE() - INTERVAL 1 MONTH, '%Y-%m'),
 DATE_FORMAT(CURDATE() - INTERVAL 2 MONTH, '%Y-%m'),
 NULL),

-- Paid through this month: shows "Paid"
(@demo_id, 'Mutual Fund SIP', 'Investment', 5000.00, 7,
 DATE_FORMAT(CURDATE(), '%Y-%m'),
 DATE_FORMAT(CURDATE() - INTERVAL 3 MONTH, '%Y-%m'),
 'Monthly SIP'),

-- Paid through last month
(@demo_id, 'Recurring Deposit', 'Investment', 3000.00, 20,
 DATE_FORMAT(CURDATE() - INTERVAL 1 MONTH, '%Y-%m'),
 DATE_FORMAT(CURDATE() - INTERVAL 2 MONTH, '%Y-%m'),
 NULL),

-- Created 3 months ago, nothing paid: shows "Overdue: 3 months unpaid"
(@demo_id, 'Home Loan EMI', 'Loan', 24500.00, 3,
 NULL,
 DATE_FORMAT(CURDATE() - INTERVAL 3 MONTH, '%Y-%m'),
 'Pay the oldest month first'),

-- Paid through last month
(@demo_id, 'Car Loan EMI', 'Loan', 11200.00, 25,
 DATE_FORMAT(CURDATE() - INTERVAL 1 MONTH, '%Y-%m'),
 DATE_FORMAT(CURDATE() - INTERVAL 2 MONTH, '%Y-%m'),
 NULL),

-- Paid through this month
(@demo_id, 'Mobile Recharge', 'Subscription', 299.00, 15,
 DATE_FORMAT(CURDATE(), '%Y-%m'),
 DATE_FORMAT(CURDATE() - INTERVAL 1 MONTH, '%Y-%m'),
 NULL),

-- Added this month, nothing paid yet
(@demo_id, 'Wi-Fi', 'Subscription', 799.00, 28,
 NULL,
 DATE_FORMAT(CURDATE(), '%Y-%m'),
 'Broadband plan');