
# Job Intelligence Platform 🚀

An AI-assisted job recommendation platform that analyzes resumes, extracts skills, identifies professional experience, and recommends relevant job opportunities based on candidate profiles.

## 📌 Project Overview

The Job Intelligence Platform simplifies the job search process by analyzing a user's resume and matching their skills and experience with available job opportunities.

It combines a Java Spring Boot backend with Python-based resume processing and MongoDB Atlas for data storage.

## ✨ Key Features

- **Resume Upload & Analysis:** Upload and analyze resumes.
- **Skill Extraction:** Automatically identify technical and professional skills.
- **Experience Detection:** Detect work experience from explicit experience details and employment date ranges.
- **Dynamic Job Fetching:** Retrieve job opportunities using external job APIs.
- **Job Matching:** Calculate a skill-based match percentage for job opportunities.
- **Personalized Recommendations:** Suggest relevant jobs based on resume information.
- **User Authentication:** Registration and login with CAPTCHA, email OTP verification, and BCrypt password hashing.
- **Application Tracking:** Track jobs users have applied for through their dashboard.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Core programming language |
| Spring Boot | Backend development and REST APIs |
| Python | Resume parsing and skill extraction |
| HTML, CSS, JavaScript | Frontend development |
| MongoDB Atlas | Cloud database |
| Docker | Application containerization |
| Render | Application deployment |
| Git & GitHub | Version control and source code management |
| JSearch API | External job data integration |

## 🏗️ System Architecture

The application follows a modular architecture:

1. **Frontend:** Provides the user interface for resume uploads, authentication, job recommendations, and application tracking.
2. **Spring Boot Backend:** Handles REST APIs, authentication, business logic, and job data integration.
3. **Python Processing Module:** Extracts resume information, skills, and professional experience.
4. **MongoDB Atlas:** Stores user information, resume-related data, and application records.
5. **External Job API:** Fetches available job opportunities.
6. **Docker & Render:** Support application containerization and deployment.

## ⚙️ Getting Started

### Prerequisites

Install the following before running the project:

- Java JDK
- Maven
- Python 3
- MongoDB Atlas account
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/iniraj01/smartLMS.git
```

### 2. Navigate to the Project

```bash
cd smartLMS
```

### 3. Configure Environment Variables

Configure the required credentials and application settings in your environment:

- Database password (`DB_PASSWORD`)
- MongoDB connection string
- Job API key
- Email service credentials
- CAPTCHA configuration
- OTP-related configuration

Never commit API keys, passwords, or other secrets to GitHub.

### 4. Run the Application

Use the project's Maven wrapper if available:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Alternatively, run the application using your IDE.

> Make sure Python dependencies and required environment variables are configured before running the application.

## 🚀 Deployment

The application uses Docker for containerization and Render for cloud deployment.

Deployment requires the appropriate environment variables and external service configurations.

## 📂 Project Structure

```text
job-intelligence-platform/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── ... Spring Boot application
│       └── resources/
│           └── static/
│               └── index.html
│
├── Python scripts/
│   ├── extract_skills.py
│   └── job_fetcher.py
│
├── Dockerfile
├── pom.xml
└── README.md
```

*Note: The structure above is a simplified overview. Actual folders may differ.*

## 🎯 Project Objectives

- Automate resume analysis and skill identification.
- Reduce manual effort in searching for relevant jobs.
- Improve job recommendations through skill-based matching.
- Provide a centralized platform for job discovery and application tracking.

## 🔮 Future Scope

- Integration with additional job portals.
- Improved AI-based resume analysis.
- Advanced career guidance and skill-gap analysis.
- Interview preparation recommendations.
- Enhanced job matching algorithms.

## 👨‍💻 Developer

**Niraj Kumar**

GitHub: [@iniraj01](https://github.com/iniraj01)

Project Repository: [Job Intelligence Platform](https://github.com/iniraj01/job-intelligence-platform)

---

⭐ If you find this project interesting, consider giving the repository a star!
