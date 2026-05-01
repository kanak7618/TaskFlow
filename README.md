# 🚀 TaskFlow — Team Task Manager

<div align="center">

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Railway](https://img.shields.io/badge/Deploy-Railway-purple?style=flat-square&logo=railway)](https://railway.app)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

**A full-stack team task management application with role-based access control, JWT authentication, and a stunning dark-mode UI.**

[Live Demo](https://taskflow-production-1d99.up.railway.app) · [Report Bug](https://github.com/kanak7618/TaskFlow/issues) · [Request Feature](https://github.com/kanak7618/TaskFlow/issues)

</div>

---

## ✨ Features

- 🔐 **JWT Authentication** — Secure signup/login with BCrypt password hashing
- 👑 **Role-Based Access Control** — Admin and Member roles with endpoint-level protection
- 📁 **Project Management** — Create, edit, delete projects with team member management
- ✅ **Task Tracking** — Full CRUD with TODO → In Progress → In Review → Done workflow
- 📊 **Smart Dashboard** — Real-time stats: total projects, tasks, completion rate, overdue count
- 🗃️ **Kanban Board** — Visual task management with priority color-coding
- ⚡ **Priority System** — Low / Medium / High / Critical task priorities
- 📅 **Due Date & Overdue Alerts** — Automatic overdue detection and visual alerts
- 👥 **Team Management** — Add/remove members per project
- 🎨 **Modern Dark UI** — Glassmorphism design with animated gradients

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2.5 |
| **Security** | Spring Security + JWT (JJWT 0.11.5) |
| **ORM** | Spring Data JPA / Hibernate |
| **Database** | PostgreSQL |
| **Build** | Maven |
| **Frontend** | HTML5 + Vanilla CSS + JavaScript |
| **Deployment** | Railway |
| **IDE** | IntelliJ IDEA |

---

## 🗄️ Database Schema

```
users
  └─ id, full_name, email, password (BCrypt), role (ADMIN/MEMBER), created_at

projects
  └─ id, name, description, owner_id (→ users), created_at, updated_at
  └─ project_members: project_id, user_id  [Many-to-Many]

tasks
  └─ id, title, description
  └─ status (TODO/IN_PROGRESS/IN_REVIEW/DONE)
  └─ priority (LOW/MEDIUM/HIGH/CRITICAL), due_date
  └─ project_id (→ projects), assignee_id (→ users), created_by_id (→ users)
  └─ created_at, updated_at
```

---

## 🔌 REST API Reference

### Auth
| Method | Endpoint | Access |
|--------|----------|--------|
| `POST` | `/api/auth/signup` | Public |
| `POST` | `/api/auth/login` | Public |

### Projects
| Method | Endpoint | Access |
|--------|----------|--------|
| `GET` | `/api/projects` | Authenticated |
| `POST` | `/api/projects` | Authenticated |
| `GET` | `/api/projects/{id}` | Member/Admin |
| `PUT` | `/api/projects/{id}` | Owner/Admin |
| `DELETE` | `/api/projects/{id}` | Owner/Admin |
| `POST` | `/api/projects/{id}/members/{userId}` | Owner/Admin |
| `DELETE` | `/api/projects/{id}/members/{userId}` | Owner/Admin |

### Tasks
| Method | Endpoint | Access |
|--------|----------|--------|
| `GET` | `/api/tasks/project/{projectId}` | Member/Admin |
| `GET` | `/api/tasks/my-tasks` | Authenticated |
| `POST` | `/api/tasks` | Member/Admin |
| `PUT` | `/api/tasks/{id}` | Member/Admin |
| `PATCH` | `/api/tasks/{id}/status` | Assignee/Admin |
| `DELETE` | `/api/tasks/{id}` | Owner/Admin |

### Dashboard & Admin
| Method | Endpoint | Access |
|--------|----------|--------|
| `GET` | `/api/dashboard` | Authenticated |
| `GET` | `/api/users` | **Admin only** |
| `GET` | `/api/users/all` | Authenticated |

---

## 🛠️ Local Setup

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### 1. Clone the repository
```bash
git clone https://github.com/kanak7618/TaskFlow.git
cd TaskFlow
```

### 2. Create PostgreSQL database
```sql
CREATE DATABASE taskmanager;
CREATE USER taskuser WITH PASSWORD 'taskpass123';
GRANT ALL PRIVILEGES ON DATABASE taskmanager TO taskuser;
GRANT ALL ON SCHEMA public TO taskuser;
```

### 3. Configure application.properties
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager
spring.datasource.username=taskuser
spring.datasource.password=taskpass123
app.jwt.secret=your-secret-key-min-32-chars-long
app.jwt.expiration=86400000
```

### 4. Run the application

**Option A — IntelliJ IDEA:**
1. Open project → Install **Lombok plugin** → Enable **Annotation Processing**
2. Run `TeamTaskManagerApplication.java` (▶ button)

**Option B — Maven CLI:**
```bash
mvn spring-boot:run
```

**Option C — Run JAR:**
```bash
mvn clean package -DskipTests
java -jar target/team-task-manager-1.0.0.jar
```

### 5. Open the app
```
http://localhost:8080
```

---

## 🚂 Deploy to Railway

### 1. Push to GitHub
```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/TaskFlow.git
git push -u origin main
```

### 2. Create Railway project
1. Go to [railway.app](https://railway.app) → **New Project** → **Deploy from GitHub**
2. Select your repository → Railway auto-detects Java/Maven
3. Click **"+ New"** → **Database** → **PostgreSQL**

### 3. Set environment variables
In your app service → **Variables** tab, add:

| Variable | Value |
|----------|-------|
| `JWT_SECRET` | `your-very-long-secret-key-min-32-chars` |
| `JWT_EXPIRATION` | `86400000` |

> `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` are **auto-injected** by Railway from the PostgreSQL service.

### 4. Generate domain
**Settings → Networking → Generate Domain** → your app is live! 🎉

---

## 🔐 Role-Based Access Control

| Feature | Admin | Member |
|---------|:-----:|:------:|
| View all users | ✅ | ❌ |
| View all projects | ✅ | Own only |
| Create project | ✅ | ✅ |
| Edit / Delete project | ✅ | Owner only |
| Add / Remove members | ✅ | Owner only |
| Create tasks | ✅ | In projects |
| Update task status | ✅ | Assigned only |
| Delete tasks | ✅ | Owner only |
| View dashboard | ✅ | ✅ |

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/taskmanager/
│   │   ├── config/          # SecurityConfig (JWT + RBAC)
│   │   ├── controller/      # AuthController, ProjectController,
│   │   │                    # TaskController, DashboardController
│   │   ├── dto/             # Request/Response DTOs
│   │   ├── entity/          # User, Project, Task (JPA Entities)
│   │   ├── enums/           # Role, TaskStatus, Priority
│   │   ├── exception/       # GlobalExceptionHandler
│   │   ├── repository/      # JPA Repositories with custom queries
│   │   ├── security/        # JwtUtil, JwtAuthFilter, UserDetailsService
│   │   └── service/         # AuthService, ProjectService,
│   │                        # TaskService, DashboardService
│   └── resources/
│       ├── static/
│       │   ├── css/         # styles.css, landing.css, app.css
│       │   ├── js/          # api.js (shared utilities)
│       │   ├── pages/       # dashboard, projects, tasks, users, login, signup
│       │   └── index.html   # Landing page
│       └── application.properties
├── railway.toml             # Railway deployment config
└── pom.xml
```

---

## 🌐 Pages

| Page | URL | Description |
|------|-----|-------------|
| Landing | `/` | Animated hero with features |
| Sign Up | `/pages/signup.html` | Register with role selection |
| Login | `/pages/login.html` | JWT-based authentication |
| Dashboard | `/pages/dashboard.html` | Stats + recent tasks/projects |
| Projects | `/pages/projects.html` | Project cards + detail panel |
| Tasks | `/pages/tasks.html` | List / Kanban / My Tasks views |
| Users | `/pages/users.html` | Admin-only user directory |

---

## 📄 License

This project is licensed under the MIT License.

---

<div align="center">

Built with ❤️ using **Spring Boot** · **PostgreSQL** · **Railway**

⭐ **Star this repo if you found it helpful!**

</div>