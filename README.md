# Gym Management System

A JavaFX-based desktop application for managing a gym, including user management, class scheduling, workout plans, equipment tracking, and payment processing.

## Project Structure

```
GMS/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── gym/
│       │           ├── GymApplication.java          # Main entry point
│       │           │
│       │           ├── controller/                  # CONTROLLER LAYER
│       │           │   ├── auth/
│       │           │   │   └── LoginController.java
│       │           │   │
│       │           │   ├── admin/
│       │           │   │   ├── AdminDashboardController.java
│       │           │   │   ├── UserManagementController.java
│       │           │   │   ├── PlanManagementController.java
│       │           │   │   ├── EquipmentManagementController.java
│       │           │   │   ├── ReportsController.java
│       │           │   │   ├── PaymentHistoryController.java
│       │           │   │   ├── AttendanceReportsController.java
│       │           │   │   ├── ClassesViewController.java
│       │           │   │   ├── BookingsViewController.java
│       │           │   │   └── EquipmentStatusController.java
│       │           │   │
│       │           │   ├── trainer/
│       │           │   │   ├── TrainerDashboardController.java
│       │           │   │   ├── WorkoutPlansController.java
│       │           │   │   ├── AssignWorkoutPlanController.java
│       │           │   │   ├── MyClassesController.java
│       │           │   │   ├── ClassEquipmentController.java
│       │           │   │   ├── DietChartsController.java
│       │           │   │   ├── AssignedMembersController.java
│       │           │   │   ├── MarkAttendanceController.java
│       │           │   │   ├── MemberProgressController.java
│       │           │   │   ├── MyEquipmentController.java
│       │           │   │   └── RequestEquipmentController.java
│       │           │   │
│       │           │   └── member/
│       │           │       ├── MemberDashboardController.java
│       │           │       ├── ViewClassesController.java
│       │           │       ├── MyBookingsController.java
│       │           │       ├── MyWorkoutPlansController.java
│       │           │       ├── WorkoutProgressController.java
│       │           │       ├── MyDietChartController.java
│       │           │       ├── NutritionTipsController.java
│       │           │       ├── PaymentHistoryController.java
│       │           │       ├── MakePaymentController.java
│       │           │       ├── MyProfileController.java
│       │           │       ├── AttendanceHistoryController.java
│       │           │       └── MyStatisticsController.java
│       │           │
│       │           └── util/                         # UTILITY LAYER
│       │               ├── DatabaseConnection.java   # Singleton DB connection
│       │               ├── SessionManager.java       # User session management
│       │               └── NavigationUtil.java       # Scene navigation
│       │
│       └── resources/
│           ├── css/
│           │   └── styles.css                       # Application styles
│           ├── database.properties                  # DB configuration
│           ├── sql/
│           │   └── schema.sql                       # Database schema & sample data
│           └── view/                                 # FXML VIEWS
│               ├── auth/
│               │   └── login.fxml
│               ├── admin/
│               │   ├── admin-dashboard.fxml
│               │   ├── user-management.fxml
│               │   ├── plan-management.fxml
│               │   ├── equipment-management.fxml
│               │   ├── reports.fxml
│               │   ├── payment-history.fxml
│               │   ├── attendance-reports.fxml
│               │   ├── classes-view.fxml
│               │   ├── bookings-view.fxml
│               │   └── equipment-status.fxml
│               ├── trainer/
│               │   ├── trainer-dashboard.fxml
│               │   ├── workout-plans.fxml
│               │   ├── assign-workout-plan.fxml
│               │   ├── my-classes.fxml
│               │   ├── class-equipment.fxml
│               │   ├── diet-charts.fxml
│               │   ├── assigned-members.fxml
│               │   ├── mark-attendance.fxml
│               │   ├── member-progress.fxml
│               │   ├── my-equipment.fxml
│               │   └── request-equipment.fxml
│               └── member/
│                   ├── member-dashboard.fxml
│                   ├── view-classes.fxml
│                   ├── my-bookings.fxml
│                   ├── my-workout-plans.fxml
│                   ├── workout-progress.fxml
│                   ├── my-diet-chart.fxml
│                   ├── nutrition-tips.fxml
│                   ├── payment-history.fxml
│                   ├── make-payment.fxml
│                   ├── my-profile.fxml
│                   ├── attendance-history.fxml
│                   └── my-statistics.fxml
│
├── .gitignore
├── README.md                                        # Project overview and setup
├── PROJECT_GUIDE.md                                 # Complete beginner guide
├── CODE_WALKTHROUGH.md                              # Detailed code explanations
└── pom.xml                                          # Maven build configuration
```

## Features

### Admin Features
- User Management (Create, Read, Update, Delete users)
- Plan Management (Manage membership plans)
- Equipment Management (Track gym equipment)
- Reports & Analytics (Revenue, user statistics, class statistics)
- Payment History View
- Attendance Reports
- Classes & Bookings Overview
- Equipment Status Monitoring

### Trainer Features
- Workout Plan Creation & Management
- Assign Workout Plans to Members
- Class Management (View and manage assigned classes)
- Class Equipment Assignment
- Diet Chart Management
- View Assigned Members
- Mark Attendance
- Track Member Progress
- Equipment Management (View assigned equipment, request equipment)

### Member Features
- View & Book Classes
- Manage Bookings (View and cancel)
- View Assigned Workout Plans
- Track Workout Progress
- View Diet Chart
- Nutrition Tips
- Payment History
- Make Payments
- Profile Management
- Attendance History
- Personal Statistics

## Technology Stack

- **Java 17**
- **JavaFX 21.0.4** - UI Framework
- **MySQL 8.4.0** - Database
- **Maven** - Build Tool
- **JDBC** - Database Connectivity

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- JavaFX SDK (included via Maven)

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd GMS
   ```

2. **Configure Database**
   - Update `src/main/resources/database.properties` with your MySQL credentials:
     ```
     db.url=jdbc:mysql://localhost:3306/gym_management_system
     db.username=your_username
     db.password=your_password
     ```

3. **Create Database**
   - Run `src/main/resources/sql/schema.sql` in MySQL Workbench or command line
   - This will create the database, tables, and insert sample data

4. **Build the Project**
   ```bash
   mvn clean compile
   ```

5. **Run the Application**
   ```bash
   mvn clean javafx:run
   ```
   
   **Note:** The application window opens maximized by default with the title "Login".

## Default Login Credentials

All accounts use the password: `password123`

- **Admin**: `admin@gym.com`
- **Trainer**: `trainer1@gym.com`, `trainer2@gym.com`, `trainer3@gym.com`
- **Member**: `member1@email.com`, `member2@email.com`

## Documentation

For detailed explanations of the codebase, see:
- **PROJECT_GUIDE.md** - Complete beginner-friendly guide explaining Java concepts, project architecture, and how everything works
- **CODE_WALKTHROUGH.md** - Line-by-line code explanations for key files

## Building Executable JAR

To create a runnable JAR file:

```bash
mvn clean package
```

The JAR will be created in `target/gym-management-system-1.0.0-SNAPSHOT.jar`

Run with:
```bash
java -jar target/gym-management-system-1.0.0-SNAPSHOT.jar
```

## Project Architecture

The application follows a simplified MVC pattern:

- **Model**: Database tables (MySQL)
- **View**: FXML files in `src/main/resources/view/`
- **Controller**: Java controllers in `src/main/java/com/gym/controller/`

Utilities are centralized in the `util` package for database connections, session management, and navigation.

## Database Schema

The database includes the following main tables:
- `users` - User accounts (admin, trainer, member)
- `plans` - Membership plans
- `equipment` - Gym equipment
- `workout_plans` - Workout plan templates
- `member_workout_plans` - Assigned workout plans
- `class_sessions` - Class sessions
- `bookings` - Class bookings
- `diet_charts` - Diet plans
- `payments` - Payment records
- `attendance` - Attendance records

See `src/main/resources/sql/schema.sql` for the complete schema.

## License

This project is for educational purposes.
