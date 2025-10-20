# FinTrack: Personal & Group Expense Manager

FinTrack is a comprehensive, user-friendly desktop application designed to manage personal and group expenses efficiently. It simplifies expense tracking, budget management, and group settlements, making it ideal for individuals, families, and teams.

---

## 📌 Project Overview
FinTrack is a **Java-based desktop application** providing a robust platform for personal finance management and shared expense tracking.

**Key Features:**  
- **Personal Expense Tracking:** Log income, expenses, and savings with notes and categories.  
- **Budget Management:** Set monthly budgets and track spending.  
- **Group Expense Management:** Create groups, add members, and record shared expenses.  
- **Automated Split Calculations:** Calculate who owes what automatically.  
- **Interactive Dashboard:** Visualize data through charts and summaries.  
- **User & Group Management:** Add, edit, and manage users and groups.  

---

## 🏗️ Project Architecture

**Folder Structure:**  
- `src/main/java/com/fynance` – Core Java source code (UI, DAOs, models)  
- `src/main/resources` – Configuration files (e.g., `config.properties`)  
- `pom.xml` – Maven project configuration  


**Modules and Components:**  
- **UI (Swing):** Native desktop experience.  
  - `ExpenseAppGUI`: Main application window  
  - `GroupDetailsPanel`, `UserManagementPanel`: Specialized panels  
- **Data Access Objects (DAOs):** Abstract SQL queries from business logic  
- **Model Classes:** POJOs representing Users, Expenses, Groups, etc.  
- **Services:** `BalanceService`, `SettlementService` handle business logic  
- **Database:** MySQL to persist all data  

**Workflow:**  
1. User interacts with the Swing UI.  
2. UI calls DAO methods for data access or modifications.  
3. DAO executes SQL queries in MySQL.  
4. Data flows back to the UI for display.

---

## 👥 User Roles & Functionality
FinTrack is a **single-user desktop application**, but supports multiple users in the database for group expense tracking.

**User Functionality:**  
- **Personal Finance:** Add, edit, delete transactions; set budgets; view dashboards.  
- **Group Management:** Create groups, add members, add group expenses, view balances, settle debts.  

---

## 🗃️ Database Schema & Details

| Table Name       | Purpose |
|-----------------|---------|
| `users`         | Stores user information (name, email) |
| `expense_groups`| Stores information about expense groups |
| `group_members` | Links users to groups (many-to-many) |
| `expenses`      | Records all personal and group expenses |
| `expense_splits`| Details how group expenses are split among members |
| `settlements`   | Logs debt settlements between users |

---

## 🚀 Future Scope & Improvements
- **Multi-User Authentication:** Support multiple users on the same desktop.  
- **Cloud Sync:** Sync data across devices.  
- **Mobile App:** Companion app for on-the-go tracking.  
- **Advanced Analytics:** Trend analysis, forecasts, and reports.  
- **API Integrations:** REST API for third-party apps.  

---

## 🧩 Current Issues & Challenges
- **Single User Focus:** User ID hardcoded.  
- **No Data Encryption:** Financial data stored without encryption.  
- **Limited Error Handling:** Needs user-friendly feedback.  
- **UI/UX Enhancements:** Swing UI could be modernized.  

---

## ⏳ Past Challenges & Solutions
- **Complex SQL Queries:** Solved with `BalanceService` for structured calculations.  
- **UI Responsiveness:** Used `SwingWorker` to prevent UI freezing during DB operations.  

---

## 🧭 Setup Instructions
1. **Clone the Repository:**  
```bash
  git clone https://github.com/your-username/fintrack.git
```

## 🧭 Setup Instructions

### Database Setup
1. Install **MySQL** and create a new database.  
2. Execute `schema.sql` to create the necessary tables.  
3. Update `src/main/resources/config.properties` with your database credentials.  

### Build & Run
1. Open the project in your IDE (IntelliJ, Eclipse, etc.).  
2. Build the project using Maven:  
```bash
mvn clean install
```
Run ExpenseAppGUI.java

## 🧾 Usage Guide
- **Dashboard:** Overview of finances, budgets, and recent transactions.  
- **Add Personal Expense:** Click "Add Personal Expense" to log transactions.  
- **Manage Groups:** Navigate to "Groups" to create, join, and manage groups.  
- **Add Group Expense:** Specify shared expenses and split among members.  

## 🧰 Tech Stack
- **Language:** Java 17  
- **UI Framework:** Java Swing  
- **Database:** MySQL  
- **Build Tool:** Maven  
- **UI Theme:** FlatLaf  

## 🧪 Testing
- **Unit Testing:** JUnit 5 for service layer and core components  
- **Manual Testing:** UI and overall functionality tested manually  
