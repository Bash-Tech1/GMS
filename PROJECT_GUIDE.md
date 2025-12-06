# Complete Project Guide - Gym Management System
## For Java Beginners

This document explains every aspect of the Gym Management System project in detail, designed for beginners learning Java.

---

## Table of Contents

1. [Java Basics You Need to Know](#java-basics)
2. [Project Architecture Overview](#architecture)
3. [File-by-File Explanation](#file-explanations)
4. [How Everything Connects](#connections)
5. [Learning Path](#learning-path)

---

## <a name="java-basics"></a>1. Java Basics You Need to Know

### What is Java?
Java is an object-oriented programming language. Everything in Java is an "object" or a "class".

### Key Concepts Used in This Project:

#### **Classes and Objects**
```java
// A class is like a blueprint
public class User {
    // Properties (variables)
    private String name;
    private int age;
    
    // Methods (functions)
    public void setName(String name) {
        this.name = name;
    }
}
```

#### **Packages**
```java
package com.gym.controller;  // Groups related classes together
```
- Think of packages as folders organizing your code
- `com.gym.controller` means: com → gym → controller folder

#### **Imports**
```java
import javafx.scene.control.Button;  // Bring in code from other files
```
- Allows you to use classes from other packages

#### **Public, Private, Protected**
- **public**: Can be accessed from anywhere
- **private**: Only accessible within the same class
- **protected**: Accessible in the same package or subclasses

#### **Static**
```java
public static void main(String[] args) { }
```
- Belongs to the class itself, not an instance
- Can be called without creating an object

#### **@FXML Annotation**
```java
@FXML
private Button myButton;
```
- Tells JavaFX this variable is connected to an FXML file element

---

## <a name="architecture"></a>2. Project Architecture Overview

### MVC Pattern (Model-View-Controller)

```
┌─────────────┐
│   VIEW      │  ← FXML files (what user sees)
│  (FXML)     │
└──────┬──────┘
       │
       │ User interacts
       │
┌──────▼──────┐
│ CONTROLLER  │  ← Java files (handles logic)
│  (Java)     │
└──────┬──────┘
       │
       │ Reads/Writes
       │
┌──────▼──────┐
│   MODEL     │  ← Database (stores data)
│  (MySQL)    │
└─────────────┘
```

**Flow:**
1. User clicks button in FXML (View)
2. Controller method is called
3. Controller reads/writes to Database (Model)
4. Controller updates the View

---

## <a name="file-explanations"></a>3. File-by-File Explanation

### **Entry Point: GymApplication.java**

**Location:** `src/main/java/com/gym/GymApplication.java`

**What it does:**
- This is where your program starts
- Java looks for `public static void main(String[] args)` to begin execution

**Code Breakdown:**
```java
package com.gym;  // This file belongs to the com.gym package

import javafx.application.Application;  // Import JavaFX framework
import javafx.fxml.FXMLLoader;  // Load FXML files
import javafx.scene.Scene;  // Create a window scene
import javafx.stage.Stage;  // Create a window (stage)

public class GymApplication extends Application {
    // "extends Application" means this class inherits from JavaFX Application
    // This gives us window management capabilities
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // This method is called when the app starts
        // primaryStage = the main window
        
        // Load the login.fxml file
        Parent root = FXMLLoader.load(getClass().getResource("/view/auth/login.fxml"));
        
        // Create a scene (the content inside the window)
        Scene scene = new Scene(root, 900, 600);
        
        // Apply CSS styling
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        // Set the window title
        primaryStage.setTitle("Gym Management System");
        
        // Put the scene in the window
        primaryStage.setScene(scene);
        
        // Show the window
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        // This is the entry point - Java starts here
        launch(args);  // Launch the JavaFX application
    }
}
```

**Key Concepts:**
- `extends Application`: Inheritance - gets all Application features
- `@Override`: We're replacing the parent's method with our own
- `throws Exception`: This method might throw an error (we handle it)
- `Parent`: JavaFX class representing the root of a scene graph
- `Stage`: The window itself
- `Scene`: The content inside the window

---

### **Utility Classes**

#### **1. DatabaseConnection.java**

**Location:** `src/main/java/com/gym/util/DatabaseConnection.java`

**Purpose:** Manages connection to MySQL database

**Why Singleton Pattern?**
- Only ONE database connection needed for the entire app
- Prevents multiple connections (wasteful and slow)

**Code Explanation:**
```java
public class DatabaseConnection {
    private static DatabaseConnection instance;  // The ONE instance
    private Connection connection;  // The actual database connection
    
    // Private constructor - prevents creating new instances
    private DatabaseConnection() {
        try {
            // Load database properties from file
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("/database.properties"));
            
            // Get connection details
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");
            
            // Create connection
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Get the single instance (creates it if doesn't exist)
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    // Get the connection
    public Connection getConnection() {
        return connection;
    }
}
```

**Key Concepts:**
- **Singleton**: Design pattern ensuring only one instance exists
- **Properties**: Key-value pairs from `.properties` file
- **DriverManager**: Java's way to connect to databases
- **Connection**: Represents a link to the database

---

#### **2. SessionManager.java**

**Location:** `src/main/java/com/gym/util/SessionManager.java`

**Purpose:** Stores information about the currently logged-in user

**Why Singleton?**
- Only ONE user can be logged in at a time
- All controllers need to access the same user info

**Code Explanation:**
```java
public class SessionManager {
    private static SessionManager instance;  // The ONE instance
    
    // Store current user data
    private int currentUserId;
    private String currentUserEmail;
    private String currentUserRole;
    private String currentUserName;
    
    // Private constructor
    private SessionManager() { }
    
    // Get instance
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    // Set user data when they log in
    public void setCurrentUser(int id, String email, String role, String name) {
        this.currentUserId = id;
        this.currentUserEmail = email;
        this.currentUserRole = role;
        this.currentUserName = name;
    }
    
    // Clear data when they log out
    public void clearSession() {
        this.currentUserId = 0;
        this.currentUserEmail = null;
        this.currentUserRole = null;
        this.currentUserName = null;
    }
    
    // Getters - allow other classes to read the data
    public int getCurrentUserId() {
        return currentUserId;
    }
    // ... more getters
}
```

**Usage Example:**
```java
// In LoginController after successful login:
SessionManager.getInstance().setCurrentUser(userId, email, role, name);

// In any other controller:
int userId = SessionManager.getInstance().getCurrentUserId();
```

---

#### **3. NavigationUtil.java**

**Location:** `src/main/java/com/gym/util/NavigationUtil.java`

**Purpose:** Switches between different screens (scenes)

**Code Explanation:**
```java
public class NavigationUtil {
    public static void switchScene(ActionEvent event, String fxmlPath) {
        try {
            // Load the FXML file
            Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlPath));
            
            // Get the current window
            Node source = (Node) event.getSource();
            Scene currentScene = source.getScene();
            Stage stage = (Stage) currentScene.getWindow();
            
            // Create new scene with same size
            Scene scene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            
            // Apply CSS
            scene.getStylesheets().add(
                NavigationUtil.class.getResource("/css/styles.css").toExternalForm()
            );
            
            // Replace the scene in the window
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

**Key Concepts:**
- **ActionEvent**: Represents a user action (button click, etc.)
- **Node**: Any UI element in JavaFX
- **Casting**: `(Node) event.getSource()` - tells Java to treat it as a Node
- **IOException**: Error that can occur when reading files

---

### **Controller Classes**

All controllers follow the same pattern. Let's examine one in detail:

#### **LoginController.java**

**Location:** `src/main/java/com/gym/controller/auth/LoginController.java`

**Purpose:** Handles user login

**Code Breakdown:**

```java
package com.gym.controller.auth;  // Package declaration

// Imports - bring in needed classes
import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
import com.gym.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

public class LoginController {
    // @FXML connects these to elements in login.fxml
    @FXML
    private TextField emailField;  // The email input box
    
    @FXML
    private PasswordField passwordField;  // The password input box
    
    @FXML
    private Label errorLabel;  // Where error messages appear
    
    // This method is called when "Login" button is clicked
    @FXML
    private void handleLogin(ActionEvent event) {
        // Get what user typed
        String emailOrUsername = emailField.getText();
        String password = passwordField.getText();
        
        // Validate input
        if (emailOrUsername.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter email and password");
            errorLabel.setVisible(true);
            return;  // Stop here if empty
        }
        
        // Try to authenticate
        try {
            // Get database connection
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // SQL query to find user
            String sql = "SELECT id, email, password, role, full_name, is_active " +
                        "FROM users WHERE email = ? OR username = ?";
            
            // PreparedStatement prevents SQL injection attacks
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, emailOrUsername);  // Replace first ? with email
            stmt.setString(2, emailOrUsername);  // Replace second ? with email
            
            // Execute query and get results
            ResultSet rs = stmt.executeQuery();
            
            // Check if user exists
            if (rs.next()) {  // rs.next() moves to first row, returns true if exists
                String storedPassword = rs.getString("password");
                boolean isActive = rs.getBoolean("is_active");
                
                // Check password
                if (password.equals(storedPassword) && isActive) {
                    // SUCCESS! Store user session
                    int userId = rs.getInt("id");
                    String role = rs.getString("role");
                    String fullName = rs.getString("full_name");
                    
                    SessionManager.getInstance().setCurrentUser(
                        userId, emailOrUsername, role, fullName
                    );
                    
                    // Navigate to appropriate dashboard
                    if ("admin".equalsIgnoreCase(role)) {
                        NavigationUtil.switchScene(event, "/view/admin/admin-dashboard.fxml");
                    } else if ("trainer".equalsIgnoreCase(role)) {
                        NavigationUtil.switchScene(event, "/view/trainer/trainer-dashboard.fxml");
                    } else if ("member".equalsIgnoreCase(role)) {
                        NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
                    }
                } else {
                    // Wrong password or inactive
                    errorLabel.setText("Invalid email or password");
                    errorLabel.setVisible(true);
                }
            } else {
                // User not found
                errorLabel.setText("Invalid email or password");
                errorLabel.setVisible(true);
            }
        } catch (SQLException e) {
            // Database error
            errorLabel.setText("Database error: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }
}
```

**Key Concepts Explained:**

1. **@FXML Annotation:**
   - Links Java variables to FXML elements
   - `fx:id="emailField"` in FXML connects to `@FXML private TextField emailField;`

2. **ActionEvent:**
   - Contains information about the user action
   - Passed to methods called by button clicks

3. **SQL Query:**
   ```sql
   SELECT id, email, password, role, full_name, is_active 
   FROM users 
   WHERE email = ? OR username = ?
   ```
   - `?` is a placeholder (prevents SQL injection)
   - `stmt.setString(1, emailOrUsername)` replaces first `?`

4. **ResultSet:**
   - Contains query results
   - `rs.next()` moves to next row (returns false if no more rows)
   - `rs.getString("column_name")` gets value from that column

5. **String Comparison:**
   - `"admin".equalsIgnoreCase(role)` - case-insensitive comparison
   - Safer than `role.equals("admin")` (can throw NullPointerException)

---

### **FXML Files (Views)**

#### **login.fxml**

**Location:** `src/main/resources/view/auth/login.fxml`

**Purpose:** Defines the login screen layout

**Structure:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- This is XML - a markup language for defining structure -->

<BorderPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.gym.controller.auth.LoginController">
    <!-- fx:controller links this FXML to LoginController.java -->
    
    <!-- Top section -->
    <top>
        <VBox>
            <Label text="Gym Management System" styleClass="login-title"/>
        </VBox>
    </top>
    
    <!-- Center section -->
    <center>
        <VBox>
            <!-- Email input -->
            <TextField fx:id="emailField" 
                      promptText="Email or Username"
                      styleClass="login-field"/>
            <!-- fx:id connects to @FXML private TextField emailField; -->
            
            <!-- Password input -->
            <PasswordField fx:id="passwordField"
                          promptText="Password"
                          styleClass="login-field"/>
            
            <!-- Error label (hidden by default) -->
            <Label fx:id="errorLabel" 
                  styleClass="error-label"
                  visible="false"/>
            
            <!-- Login button -->
            <Button text="Login" 
                   onAction="#handleLogin"
                   styleClass="login-button"/>
            <!-- onAction="#handleLogin" calls handleLogin() method -->
        </VBox>
    </center>
</BorderPane>
```

**Key Concepts:**
- **fx:id**: Links to Java variable with same name
- **onAction**: Calls method when clicked
- **styleClass**: Applies CSS class
- **promptText**: Placeholder text

---

### **Database Schema (schema.sql)**

**Location:** `src/main/resources/sql/schema.sql`

**Purpose:** Creates database structure and sample data

**Key Tables Explained:**

#### **users Table:**
```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,  -- Unique ID, auto-increments
    email VARCHAR(255) UNIQUE NOT NULL,  -- Email, must be unique, required
    password VARCHAR(255) NOT NULL,      -- Password, required
    username VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('admin', 'trainer', 'member') DEFAULT 'member',
    plan_id INT,  -- Foreign key to plans table
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_user_plan FOREIGN KEY (plan_id) REFERENCES plans(id)
);
```

**SQL Concepts:**
- **PRIMARY KEY**: Unique identifier
- **AUTO_INCREMENT**: Automatically increases (1, 2, 3...)
- **UNIQUE**: No duplicates allowed
- **NOT NULL**: Required field
- **ENUM**: Only allows specific values
- **FOREIGN KEY**: References another table
- **DEFAULT**: Value if not specified
- **TIMESTAMP**: Date and time

---

## <a name="connections"></a>4. How Everything Connects

### **Complete Flow Example: User Login**

```
1. User opens app
   ↓
2. GymApplication.main() runs
   ↓
3. GymApplication.start() loads login.fxml
   ↓
4. LoginController is created and linked to login.fxml
   ↓
5. User enters credentials and clicks "Login"
   ↓
6. handleLogin() method is called
   ↓
7. DatabaseConnection.getInstance() gets database connection
   ↓
8. SQL query checks if user exists
   ↓
9. If valid: SessionManager stores user info
   ↓
10. NavigationUtil.switchScene() loads dashboard
   ↓
11. User sees their dashboard
```

### **Data Flow Diagram:**

```
┌─────────────────┐
│  login.fxml     │  User sees this
│  (View)         │
└────────┬────────┘
         │ fx:id links
         │ onAction calls
         ▼
┌─────────────────┐
│ LoginController │  Logic happens here
│  (Controller)   │
└────────┬────────┘
         │ Uses
         ├──► DatabaseConnection ──► MySQL Database
         ├──► SessionManager ──► Stores user info
         └──► NavigationUtil ──► Changes screen
```

---

## <a name="learning-path"></a>5. Learning Path

### **Step 1: Understand Java Basics**
1. Variables and Data Types
2. Methods and Classes
3. Object-Oriented Programming (OOP)
4. Exception Handling (try-catch)

### **Step 2: Learn JavaFX**
1. What is JavaFX?
2. FXML vs Java code
3. Scene, Stage, Node concepts
4. Event handling

### **Step 3: Learn Database Concepts**
1. What is SQL?
2. Tables, Rows, Columns
3. SELECT, INSERT, UPDATE, DELETE
4. JDBC (Java Database Connectivity)

### **Step 4: Study This Project**
1. Start with `GymApplication.java` - understand entry point
2. Study `LoginController.java` - simplest controller
3. Look at `login.fxml` - see how UI is defined
4. Trace data flow: User action → Controller → Database → Response

### **Step 5: Practice**
1. Add a new button to a screen
2. Create a simple controller method
3. Add a new field to a table
4. Create a new screen

---

## Common Patterns in This Project

### **Pattern 1: Load Data from Database**
```java
private void loadData() {
    String sql = "SELECT * FROM table_name";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            // Process each row
            String name = rs.getString("name");
            // Add to list/table
        }
    } catch (SQLException e) {
        showError("Error: " + e.getMessage());
    }
}
```

### **Pattern 2: Save Data to Database**
```java
private void saveData(String name, int value) {
    String sql = "INSERT INTO table_name (name, value) VALUES (?, ?)";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, name);  // Replace first ?
        stmt.setInt(2, value);    // Replace second ?
        stmt.executeUpdate();      // Execute the insert
    } catch (SQLException e) {
        showError("Error: " + e.getMessage());
    }
}
```

### **Pattern 3: Update Data**
```java
private void updateData(int id, String newName) {
    String sql = "UPDATE table_name SET name = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, newName);
        stmt.setInt(2, id);
        stmt.executeUpdate();
    } catch (SQLException e) {
        showError("Error: " + e.getMessage());
    }
}
```

### **Pattern 4: Delete Data**
```java
private void deleteData(int id) {
    String sql = "DELETE FROM table_name WHERE id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, id);
        stmt.executeUpdate();
    } catch (SQLException e) {
        showError("Error: " + e.getMessage());
    }
}
```

---

## Important Java Concepts Used

### **1. Try-With-Resources**
```java
try (Connection conn = ...) {
    // Use connection
} // Automatically closes connection here
```
- Automatically closes resources (Connection, Statement, ResultSet)
- Prevents memory leaks

### **2. PreparedStatement**
```java
PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
stmt.setInt(1, userId);
```
- Prevents SQL injection attacks
- More efficient than regular Statement

### **3. ObservableList**
```java
ObservableList<Data> list = FXCollections.observableArrayList();
table.setItems(list);
```
- Automatically updates UI when data changes
- Used with JavaFX TableView

### **4. Lambda Expressions**
```java
button.setOnAction(event -> {
    // Code here
});
```
- Shorter way to write anonymous functions
- `event ->` means "takes event parameter"

---

## Debugging Tips

### **1. Use System.out.println()**
```java
System.out.println("Debug: User ID = " + userId);
```
- Prints to console
- Helps track program flow

### **2. Check Error Messages**
- Read the full error message
- Look at line numbers
- Check stack trace

### **3. Common Errors:**

**NullPointerException:**
- Object is null when you try to use it
- Check if object exists before using

**SQLException:**
- Database error
- Check SQL syntax
- Verify database connection

**ClassNotFoundException:**
- Missing import
- Wrong package name

---

## Next Steps for Learning

1. **Read Code Line by Line**
   - Start with one controller
   - Understand every line
   - Look up concepts you don't know

2. **Modify Existing Code**
   - Change button text
   - Add a new field
   - Modify a query

3. **Add New Features**
   - Create a new screen
   - Add a new database table
   - Implement new functionality

4. **Use Java Documentation**
   - https://docs.oracle.com/javase/
   - Search for classes you use
   - Read method descriptions

5. **Practice SQL**
   - Write queries manually
   - Test in MySQL Workbench
   - Understand JOIN operations

---

## Resources

- **Java Tutorial:** https://docs.oracle.com/javase/tutorial/
- **JavaFX Documentation:** https://openjfx.io/
- **MySQL Tutorial:** https://dev.mysql.com/doc/
- **JDBC Guide:** https://docs.oracle.com/javase/tutorial/jdbc/

---

## Summary

This project demonstrates:
- **MVC Architecture**: Separation of concerns
- **Database Integration**: JDBC with MySQL
- **Desktop GUI**: JavaFX for user interface
- **Object-Oriented Design**: Classes, inheritance, encapsulation
- **Design Patterns**: Singleton pattern for database connection

Take it step by step, understand one piece at a time, and don't hesitate to experiment!

