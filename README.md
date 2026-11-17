# 🎓 SmartLMS – Learning Management System

**SmartLMS** is a web-based Learning Management System (LMS) developed using Java and Spring Boot. It provides a centralized platform where administrators, teachers, and students can manage and access learning activities efficiently.

The platform is designed to simplify online education through course management, student enrollment, lesson delivery, assignments, examinations, and performance tracking.

## 🚀 Key Features

### 👨‍💼 Admin Panel
- Manage students and teachers.
- Create and manage courses.
- Manage student course enrollments.
- Monitor platform activities.
- View reports and statistics.

### 👨‍🏫 Teacher Panel
- Manage assigned courses.
- Create and manage lessons.
- Upload assignments.
- View student submissions.
- Evaluate assignments and provide feedback.

### 👨‍🎓 Student Panel
- Browse and enroll in courses.
- Access enrolled course lessons.
- Track learning progress.
- View and submit assignments.
- Participate in examinations.
- View exam history and certificates.

### 📜 Additional Features
- Role-based access control.
- Secure authentication and authorization.
- Password encryption.
- Assignment submission management.
- Certificate generation and public verification.
- Responsive user interface.

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java | Core programming language |
| Spring Boot | Backend framework |
| Spring MVC | Request handling |
| Spring Data JPA | Database operations |
| Hibernate | ORM framework |
| Spring Security | Authentication and authorization |
| MySQL | Relational database |
| Thymeleaf | Server-side template engine |
| HTML5 | Web page structure |
| CSS3 | Styling |
| Bootstrap 5 | Responsive UI |
| JavaScript | Frontend interactions |
| Maven | Dependency and build management |

## 🏗️ Project Architecture

The project follows a layered architecture to maintain clean code and separation of responsibilities.

```text
com.smartlms
│
├── config
├── controller
├── service
├── repository
├── entity
├── dto
├── security
├── exception
│
└── SmartLmsApplication.java
```

**Layer Responsibilities:**

- **Controller:** Handles incoming HTTP requests.
- **Service:** Implements business logic.
- **Repository:** Performs database operations.
- **Entity:** Represents database tables.
- **DTO:** Transfers data between application layers.
- **Security:** Handles authentication and authorization.
- **Config:** Manages application configurations.
- **Exception:** Handles application errors.

## ⚙️ Installation and Setup

### Prerequisites

Make sure you have installed:

- JDK 23 or a compatible JDK version
- MySQL 8.0+
- Maven (or use the included Maven Wrapper)
- Eclipse IDE or Visual Studio Code

### Step 1: Clone the Repository

```bash
git clone https://github.com/iniraj01/smartlms.git
```

### Step 2: Navigate to Project Directory

```bash
cd smartlms
```

### Step 3: Create MySQL Database

```sql
CREATE DATABASE smartlms_db;
```

### Step 4: Configure Database

Update your `application.properties` file:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smartlms_db
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:your_password}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Configure the remaining application settings and environment variables as required.

### Step 5: Build the Project

On Windows:

```bash
.\mvnw.cmd clean install
```

On Linux/macOS:

```bash
./mvnw clean install
```

### Step 6: Run the Application

```bash
.\mvnw.cmd spring-boot:run
```

Open your browser and visit:

```text
http://localhost:8080
```

## 🔐 User Roles

| Role | Access |
|---|---|
| ADMIN | Complete platform management |
| TEACHER | Course, lesson and assignment management |
| STUDENT | Learning, enrollment and examination |

## 🔮 Future Enhancements

- Live online classes.
- Real-time notifications.
- Advanced analytics dashboard.
- Online payment integration.
- Cloud-based file storage.
- Mobile application.
- AI-powered learning recommendations.

## 🎯 Project Objective

The primary objective of SmartLMS is to develop a structured digital learning environment that simplifies academic management and improves interaction between administrators, teachers, and students.

## 👨‍💻 Developer

**Niraj Kumar**

- GitHub: [@iniraj01](https://github.com/iniraj01)
- Portfolio: [nirajpaswan.in](https://www.nirajpaswan.in/)

## 📄 License

This project is developed for educational and learning purposes. Licensing and reuse permissions can be defined as the project evolves.

---

⭐ **If you find this project interesting, consider giving it a star!**
