# Monthly Bills Tracker

A web app that tracks all fixed monthly household bills on one dashboard.
Each user has their own account and their own bills.
Bills are sorted by urgency. Missed months come first, then the nearest due date. Paid bills go last.
Color-coded badges show how urgent each bill is.

- **Course:** Full Stack Development Online (Excelr)
- **Author:** Avadhoot
- **GitHub:** Avadhoot-007

## What It Does

You create an account and log in.
You add each recurring bill once, with its due day of the month (1 to 31).
The dashboard compares today's date with each due day and shows a status badge.
Click **Mark Paid** to stop the alert for the current month.
The next month, the bill becomes unpaid again on its own.
If you skip a month, the bill stays **Overdue** until you pay the missed month.

### Bill types

- **Utilities:** Electricity, piped gas, LPG cylinder, water
- **Investment:** Mutual fund SIPs, recurring deposits
- **Loan:** Home, car, or personal loan EMIs
- **Subscription:** Mobile recharge, DTH, Wi-Fi, OTT, SaaS tools

### Current features

- Register and log in with a username and password
- Passwords are stored as BCrypt hashes, never as plain text
- The session id changes at login (blocks session fixation)
- Each user sees only their own bills
- Add, edit, and delete bills
- Dashboard cards: total monthly spend, paid amount, pending amount
- Cards always count all bills. The type filter does not change them
- Color-coded blinking badges for urgency
- Mark Paid and Mark Unpaid button
- Missed months carry over. Unpaid months show as Overdue
- **Pay Oldest Month** clears missed months one at a time
- Filter the table by bill type
- Server-side checks on every bill (title, amount, due day, type, notes)
- Expired login sends you back to the login page
- Due day 31 works in shorter months (it uses the last day of the month)

## How Paid Tracking Works

- `paid_month` means "paid through". Every month up to it is paid.
- `created_month` is the month the bill was added. Earlier months are never owed.
- Missed months are the months between `paid_month` and last month.
- **Mark Paid** pays the oldest unpaid month first.
- **Mark Unpaid** undoes the current month only.
- The server sets both fields. The browser cannot change them.

## Badge Rules

| Badge                          | When it shows                                        | Look                   |
| ------------------------------ | ---------------------------------------------------- | ---------------------- |
| Overdue: N months unpaid       | One or more earlier months are unpaid                | Red, fast blink        |
| Overdue by N days              | Due day has passed this month and the bill is unpaid | Red, fast blink        |
| Due today / Due in 1 to 3 days | 0 to 3 days left and unpaid                          | Orange, medium blink   |
| Due in 4 to 7 days             | 4 to 7 days left and unpaid                          | Dark slate, soft pulse |
| Due in 8 to 10 days            | 8 to 10 days left and unpaid                         | Blue, no animation     |
| Due in 11+ days                | More than 10 days left                               | Grey, no animation     |
| Paid                           | Paid through the current month                       | Green, no animation    |

## Tech Stack

| Part       | Technology                                                              |
| ---------- | ----------------------------------------------------------------------- |
| Backend    | Java 17 or higher, Spring Boot 4.1 (Spring Web MVC, Spring Data JPA)    |
| Security   | Spring Security Crypto (BCrypt hashing only), server-side HTTP sessions |
| Database   | MySQL 8.0                                                               |
| Frontend   | HTML, CSS, JavaScript                                                   |
| Build tool | Maven (included as `mvnw`)                                              |

The frontend files sit inside Spring Boot's `static` folder.
One server runs everything, so no extra setup is needed for cross-origin requests.

## Project Structure

```
billreminder/
├── .mvn/wrapper/
│   └── maven-wrapper.properties      (Maven wrapper settings)
├── database/
│   ├── schema.sql                    (creates the database and both tables)
│   └── sample_data.sql               (optional: 8 sample bills for one user)
├── src/main/java/com/example/billreminder/
│   ├── BillreminderApplication.java  (starts the app)
│   ├── User.java                     (one user, matches the users table)
│   ├── UserRepository.java           (user database access)
│   ├── AuthRequest.java              (username and password from the forms)
│   ├── AuthController.java           (register, login, logout, who am I)
│   ├── Bill.java                     (one bill, matches the bills table)
│   ├── BillRepository.java           (bill database access)
│   └── BillController.java           (bills REST API)
├── src/main/resources/
│   ├── application.properties.example  (database settings template)
│   └── static/
│       ├── index.html                (login page)
│       ├── register.html             (create account page)
│       ├── dashboard.html            (main dashboard)
│       ├── bill-form.html            (add and edit bill)
│       ├── style.css
│       ├── login.js
│       ├── register.js
│       ├── dashboard.js
│       └── form.js
├── src/test/java/com/example/billreminder/
│   └── BillreminderApplicationTests.java
├── mvnw                              (Maven wrapper, Mac and Linux)
├── mvnw.cmd                          (Maven wrapper, Windows)
├── pom.xml
└── README.md
```

## How to Run

### What you need

- Java 17 or higher
- MySQL 8.0
- An editor such as VS Code

### Steps

1. **Get the code.**

   ```
   git clone https://github.com/Avadhoot-007/[repository name].git
   cd [repository name]
   ```

2. **Create the database.**
   Open MySQL Workbench or the MySQL command line.
   Run everything inside `database/schema.sql`.
   Run it once. It creates tables, so a second run fails on a database that already has them.
   Already have an older database? Do not re-run it.
   Use the upgrade lines at the bottom of `schema.sql` instead.

3. **Set your database password.**
   Copy `src/main/resources/application.properties.example`
   to `src/main/resources/application.properties`.
   Open the new file and replace `YOUR_PASSWORD` with your MySQL password.
   On Windows PowerShell:

   ```
   copy src\main\resources\application.properties.example src\main\resources\application.properties
   ```

4. **Start the app.**
   Windows:

   ```
   .\mvnw.cmd spring-boot:run
   ```

   Mac or Linux:

   ```
   ./mvnw spring-boot:run
   ```

   The first run downloads libraries and takes a few minutes.
   Wait for the line `Started BillreminderApplication`.

5. **Open the app.**
   Go to `http://localhost:8080` in your browser.
   Click **Create an account**, register, then log in.
   There is no default account.

6. **Optional: load sample bills.**
   Register a user named `demo` first.
   Then run `database/sample_data.sql` in MySQL.
   It gives `demo` 8 sample bills.
   Rows with no `created_month` count as created this month.

### Run the tests

```
./mvnw test
```

The only test loads the Spring context.
It needs MySQL running and `application.properties` set up.

## REST API

All `/api/bills` endpoints need a logged-in session. Without one they return `401`.

### Auth

| Method | Address              | What it does                                     |
| ------ | -------------------- | ------------------------------------------------ |
| POST   | `/api/auth/register` | Create an account. Replies `OK` or an error text |
| POST   | `/api/auth/login`    | Log in. Replies `OK` or an error text            |
| POST   | `/api/auth/logout`   | End the session                                  |
| GET    | `/api/auth/me`       | Username of the logged-in user, or empty text    |

### Bills

| Method | Address                       | What it does                                                   |
| ------ | ----------------------------- | -------------------------------------------------------------- |
| GET    | `/api/bills`                  | List your bills                                                |
| GET    | `/api/bills/{id}`             | Get one of your bills                                          |
| POST   | `/api/bills`                  | Add a bill                                                     |
| PUT    | `/api/bills/{id}`             | Edit a bill (paid and created months stay unchanged)           |
| DELETE | `/api/bills/{id}`             | Delete a bill                                                  |
| POST   | `/api/bills/{id}/toggle-paid` | Undo this month if paid. Otherwise pay the oldest unpaid month |

A bill that does not exist, or belongs to another user, returns `404`.
Bad values return `400`.
The server always takes the user id from the session, never from the request.

## Database

Two tables: `users` and `bills`.

### users

| Column        | Type         | Meaning                              |
| ------------- | ------------ | ------------------------------------ |
| id            | INT          | Auto-generated primary key           |
| username      | VARCHAR(50)  | Unique login name                    |
| password_hash | VARCHAR(100) | BCrypt hash, never the real password |

### bills

| Column        | Type          | Meaning                                                                 |
| ------------- | ------------- | ----------------------------------------------------------------------- |
| id            | INT           | Auto-generated primary key                                              |
| user_id       | INT           | Owner of the bill. Foreign key to `users.id`                            |
| title         | VARCHAR(100)  | Bill name                                                               |
| bill_type     | VARCHAR(30)   | Utilities, Investment, Loan, or Subscription                            |
| amount        | DECIMAL(10,2) | Monthly amount in rupees                                                |
| due_day       | INT           | Day of the month, 1 to 31                                               |
| paid_month    | VARCHAR(7)    | Paid through this month, like `2026-11`. Empty if nothing is paid yet   |
| created_month | VARCHAR(7)    | Month the bill was added, like `2026-09`. Earlier months are never owed |
| notes         | VARCHAR(255)  | Optional notes                                                          |

A bill counts as paid when `paid_month` is the current month or later.
When a new month starts, `paid_month` falls behind, so the bill becomes unpaid on its own.

## Known Limitations

- Sessions live in server memory. Restarting the server logs everyone out.
- The app uses plain HTTP. Use HTTPS before any real deployment.
- No login attempt limit, password reset, or account deletion.
- No CSRF protection.
- Login and register replies use HTTP 200 with an error text.
- Summary cards count the current month only. Amounts for missed months are not added.
- Missed months are worked out from `paid_month`. There is no per-month payment record.
- Java stores money as `double`. The database uses `DECIMAL(10,2)`. Fine at this scale, not for real accounting.
- The dashboard uses the browser's date. Mark Paid uses the server's month. They differ if the two clocks differ.
- Deleting a bill is permanent. There is no payment history.
- The only test needs a live MySQL database.
- The app runs locally only. It is not deployed.

## Planned Features

**Future scope**

- Email and WhatsApp reminders
- Payment history archive
- Variable amount tracking, such as electricity units
- Receipt upload
- Spend charts by bill type

## Screenshots

Screenshots will be added in a `screenshots` folder.
