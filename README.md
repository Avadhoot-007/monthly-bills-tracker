# Monthly Bills Tracker

A web app that tracks all fixed monthly household bills on one dashboard.
Bills are sorted by the nearest due date. Color-coded badges show how urgent each bill is.

- **Course:** Full Stack Development Online (Excelr)
- **Author:** Avadhoot
- **GitHub:** Avadhoot-007

## What It Does

You add each recurring bill once, with its due day of the month (1 to 31).
The dashboard compares today's date with each due day and shows a status badge.
Click **Mark Paid** to stop the alert for the current month.
The next month, the bill becomes unpaid again on its own.

### Bill types

- **Utilities:** Electricity, piped gas, LPG cylinder, water
- **Investment:** Mutual fund SIPs, recurring deposits
- **Loan:** Home, car, or personal loan EMIs
- **Subscription:** Mobile recharge, DTH, Wi-Fi, OTT, SaaS tools

### Current features

- Add, edit, and delete bills
- Dashboard cards: total monthly spend, paid amount, pending amount
- Color-coded blinking badges for urgency
- Mark Paid and Mark Unpaid button
- Filter the table by bill type
- Demo login page
- Due day 31 works in shorter months (it uses the last day of the month)

## Badge Rules

| Badge               | When it shows                             | Look                 |
| ------------------- | ----------------------------------------- | -------------------- |
| Overdue             | Due day has passed and the bill is unpaid | Red, fast blink      |
| Due in 0 to 3 days  | 0 to 3 days left and unpaid               | Orange, medium blink |
| Due in 4 to 7 days  | 4 to 7 days left and unpaid               | Yellow, soft pulse   |
| Due in 8 to 10 days | 8 to 10 days left and unpaid              | Blue, no animation   |
| Upcoming            | More than 10 days left                    | Grey, no animation   |
| Paid                | Marked paid for the current month         | Green, no animation  |

## Tech Stack

| Part       | Technology                                                   |
| ---------- | ------------------------------------------------------------ |
| Backend    | Java 17 or higher, Spring Boot (Spring Web, Spring Data JPA) |
| Database   | MySQL 8.0                                                    |
| Frontend   | HTML, CSS, JavaScript                                        |
| Build tool | Maven (included as `mvnw`)                                   |

The frontend files sit inside Spring Boot's `static` folder.
One server runs everything, so no extra setup is needed for cross-origin requests.

## Project Structure

```
billreminder/
├── database/
│   └── schema.sql                  (creates the database, table, and sample data)
├── src/main/java/com/example/billreminder/
│   ├── BillreminderApplication.java  (starts the app)
│   ├── Bill.java                     (one bill, matches the bills table)
│   ├── BillRepository.java           (database access)
│   └── BillController.java           (REST API)
├── src/main/resources/
│   ├── application.properties.example  (database settings template)
│   └── static/
│       ├── index.html                (login page)
│       ├── dashboard.html            (main dashboard)
│       ├── bill-form.html            (add and edit bill)
│       ├── style.css
│       ├── login.js
│       ├── dashboard.js
│       └── form.js
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
   git clone https://github.com/[your GitHub username]/[repository name].git
   cd [repository name]
   ```

2. **Create the database.**
   Open MySQL Workbench or the MySQL command line.
   Run everything inside `database/schema.sql`.

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
   Demo login: username `admin`, password `1234`.

## REST API

| Method | Address                       | What it does                          |
| ------ | ----------------------------- | ------------------------------------- |
| GET    | `/api/bills`                  | List all bills                        |
| GET    | `/api/bills/{id}`             | Get one bill                          |
| POST   | `/api/bills`                  | Add a bill                            |
| PUT    | `/api/bills/{id}`             | Edit a bill                           |
| DELETE | `/api/bills/{id}`             | Delete a bill                         |
| POST   | `/api/bills/{id}/toggle-paid` | Switch a bill between Paid and Unpaid |

## Database

One table named `bills`.

| Column     | Type          | Meaning                                                      |
| ---------- | ------------- | ------------------------------------------------------------ |
| id         | INT           | Auto-generated primary key                                   |
| title      | VARCHAR(100)  | Bill name                                                    |
| bill_type  | VARCHAR(30)   | Utilities, Investment, Loan, or Subscription                 |
| amount     | DECIMAL(10,2) | Monthly amount in rupees                                     |
| due_day    | INT           | Day of the month, 1 to 31                                    |
| paid_month | VARCHAR(7)    | Month the bill was paid, like `2026-11`. Empty if never paid |
| notes      | VARCHAR(255)  | Optional notes                                               |

A bill counts as paid when `paid_month` equals the current month.
This is why bills reset on their own each month.

## Known Limitations

- The login is a demo only. It runs in the browser and is not real security.
- All bills are shared. They do not belong to individual users yet.
- The server does not validate input. Only the form page does.
- Editing a bill that does not exist fails silently.
- The app runs locally only. It is not deployed.

## Planned Features

**In progress**

- User registration
- Bills that belong to each user

**Future scope**

- Scheduled monthly reset job
- Email and WhatsApp reminders
- Payment history archive
- Variable amount tracking, such as electricity units
- Receipt upload
- Spend charts by bill type

## Screenshots

Screenshots will be added in a `screenshots` folder.
