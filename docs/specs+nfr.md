# TaskManager - ISS Project Documentation

**Tech Stack:** Spring Boot (Java) · React (HeroUI) · PostgreSQL  

---

## Functional Requirements

### FR1 - Import Data for the Database *(Priority 1)*
- Support importing data from **CSV** into the database
- **Validate data** before inserting - reject or flag malformed records
- Useful for **seeding test data** during development and onboarding

### FR2 - Authenticate as a User *(Priority 1)*
- Users can **register and log in** using email and password
- Protected endpoints **reject requests** while logged out

### FR3 - Create / Remove Tasks *(Priority 1)*
- Users can **create tasks** with a title and optional description
- Each task is **linked to the user** who created it
- Users can **permanently delete** their own tasks
- **Confirmation step** required before deletion to prevent accidents
- Task list **updates without a page refresh**

### FR4 - Modify Task Properties *(Priority 2)*
- Users can **edit** task name, description, deadline, and status (To Do / In Progress / Done / Postponed)
- Changes are **saved immediately** to the database

### FR5 - Task Due Notifications *(Priority 2)*
- Users are **notified in-app** when a task deadline is approaching
- System checks for tasks **due within the next 24 hours**
- Notifications are **dismissable** and don't reappear once acknowledged

### FR6 - Search Tasks *(Priority 3)*
- Users can **search tasks** by keyword
- Matches against **title and description**
- Search is **scoped to the current user** - no cross-user data

---

## Non-Functional Requirements

### NFR1 - CI/CD Pipeline *(Priority 1)*
- Pipeline triggers on **every push to pull request**
- Steps: **build → lint → test**
- Linting for both **Java** (Checkstyle) and **React** (ESLint)
- **Blocks merges** if any step fails

### NFR2 - Data Consistency
- Multi-step DB operations wrapped in **transactions**
- **Failures rollback** automatically - no partial data left behind
- PostgreSQL **constraints** (foreign keys, not-null) enforced at DB level
- **DB fetches** are faster than 1s

### NFR3 - Code Quality
- Clear **separation of concerns** (Controller / Service / Repository on backend, components / services on frontend)
- **No duplicated code**, meaningful naming throughout
- Maybe **basic unit tests** for core backend logic

### NFR4 - Security
- **Authentication** - all sensitive endpoints require login, validated server-side
- **Password encryption** - passwords hashed with **BCrypt**, never stored as plain text
- **Input validation** - all client input sanitized on the backend (prevents SQL injection, malformed data)

### NFR5 - UI Simplicity
- Interface is **clean and intuitive**, easy to use without prior training
- Key actions reachable in **as few clicks as possible**
- **User-friendly feedback** - success/error messages, confirmation dialogs, loading states

### NFR6 - Performance
- **Response time** - standard API calls complete in **under 1 second**
- **DB query performance** - frequently filtered fields (user ID, deadline) are **indexed**
- **Concurrent users** - multiple sessions work simultaneously with **no data leaks or race conditions**

---

## Development Iterations

### Iteration 1 - Foundation
> *FR1, FR2, FR3 · NFR1, NFR3, NFR4*

- Set up **Spring Boot project** with PostgreSQL connection
- Implement **user registration & login** + BCrypt
- Build **create & delete task** endpoints
- Implement **data import** (CSV) with validation
- Initialize **React frontend** with login page and basic task list
- Configure **CI/CD pipeline** with linting and build checks

**Deliverable:** A user can register, log in, import data, and manage a basic task list - pipeline is live.

---

### Iteration 2 - Core Features
> *FR4, FR5 · NFR2, NFR5, NFR6*

- Add **task editing** - name, description, deadline, status
- Add **frontend validation** (e.g. no past deadlines)
- Implement **due notifications** - backend checks tasks due in 24h, frontend shows alert/badge
- Wrap critical operations in **database transactions**
- Add **UI polish** - loading states, error messages, delete confirmation dialogs
- Add **DB indexes** on user ID and deadline fields

**Deliverable:** Full task management with editing and notifications, polished UI, and consistent data.

---

### Iteration 3 - Polish & Search
> *FR6 · NFR2, NFR5, NFR6 - continued*

- Implement **keyword search** on title and description, scoped to current user
- Handle remaining **edge cases** - invalid import files, empty states
- **Profile and optimize** any slow queries
- **Final code cleanup** - remove dead code, check test coverage, review linting

**Deliverable:** Complete, submission-ready application with all features implemented and requirements met.
