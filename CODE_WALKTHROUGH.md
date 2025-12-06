# Code Walkthrough - Detailed Line-by-Line Explanations

This document provides detailed explanations of key code sections in the Gym Management System.

---

## 1. GymApplication.java - Complete Breakdown

```java
package com.gym;
```
**Explanation:** Declares this file belongs to the `com.gym` package. Packages organize related classes.

```java
import javafx.application.Application;
```
**Explanation:** Imports the `Application` class from JavaFX. This is the base class for JavaFX applications.

```java
import javafx.fxml.FXMLLoader;
```
**Explanation:** `FXMLLoader` loads FXML files (UI definitions) into Java objects.

```java
import javafx.scene.Scene;
```
**Explanation:** `Scene` represents the content of a window (all UI elements).

```java
import javafx.scene.Parent;
```
**Explanation:** `Parent` is the root node of a scene graph (the top-level container).

```java
import javafx.stage.Stage;
```
**Explanation:** `Stage` is the window itself (the frame, title bar, etc.).

```java
public class GymApplication extends Application {
```
**Explanation:** 
- `public` - accessible from anywhere
- `class GymApplication` - defines a class named GymApplication
- `extends Application` - inherits from JavaFX Application class, gets window management features

```java
    @Override
    public void start(Stage primaryStage) throws Exception {
```
**Explanation:**
- `@Override` - indicates we're replacing the parent's method
- `public` - accessible
- `void` - returns nothing
- `start` - method name (called by JavaFX when app launches)
- `Stage primaryStage` - the main window (passed by JavaFX)
- `throws Exception` - this method might throw an error

```java
        Parent root = FXMLLoader.load(getClass().getResource("/view/auth/login.fxml"));
```
**Explanation:**
- `FXMLLoader.load()` - loads an FXML file
- `getClass()` - gets the current class (GymApplication)
- `.getResource()` - finds a resource file
- `"/view/auth/login.fxml"` - path to the FXML file (starts with / means from resources root)
- `Parent root` - stores the loaded UI as the root element

```java
        Scene scene = new Scene(root, 900, 600);
```
**Explanation:**
- `new Scene()` - creates a new scene
- `root` - the UI content
- `900, 600` - width and height in pixels

```java
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
```
**Explanation:**
- `getStylesheets()` - gets the list of CSS files
- `.add()` - adds a CSS file
- `getClass().getResource("/css/styles.css")` - finds the CSS file
- `.toExternalForm()` - converts URL to string format

```java
        primaryStage.setTitle("Gym Management System");
```
**Explanation:** Sets the window title bar text.

```java
        primaryStage.setScene(scene);
```
**Explanation:** Puts the scene (content) into the stage (window).

```java
        primaryStage.show();
```
**Explanation:** Makes the window visible.

```java
    public static void main(String[] args) {
```
**Explanation:**
- `public static` - can be called without creating an object
- `void` - returns nothing
- `main` - special method name, Java starts here
- `String[] args` - command-line arguments (not used here)

```java
        launch(args);
```
**Explanation:** Starts the JavaFX application, calls `start()` method.

---

## 2. DatabaseConnection.java - Detailed Explanation

```java
public class DatabaseConnection {
```
**Explanation:** Defines a class to manage database connections.

```java
    private static DatabaseConnection instance;
```
**Explanation:**
- `private` - only accessible within this class
- `static` - belongs to the class, not instances
- `DatabaseConnection instance` - stores the single instance (Singleton pattern)

```java
    private Connection connection;
```
**Explanation:** Stores the actual database connection object.

```java
    private DatabaseConnection() {
```
**Explanation:**
- `private` constructor - prevents creating instances with `new DatabaseConnection()`
- Only this class can create instances
- This is part of the Singleton pattern

```java
        try {
```
**Explanation:** Starts a try block - code that might throw an error.

```java
            Properties props = new Properties();
```
**Explanation:**
- `Properties` - Java class for key-value pairs
- `new Properties()` - creates empty Properties object

```java
            props.load(getClass().getResourceAsStream("/database.properties"));
```
**Explanation:**
- `load()` - loads properties from a stream
- `getClass().getResourceAsStream()` - opens the properties file as a stream
- `"/database.properties"` - path to the file

```java
            String url = props.getProperty("db.url");
```
**Explanation:**
- `getProperty("db.url")` - gets value for key "db.url"
- Reads from database.properties file

```java
            connection = DriverManager.getConnection(url, username, password);
```
**Explanation:**
- `DriverManager` - Java's database connection manager
- `getConnection()` - creates a connection to the database
- Returns a `Connection` object

```java
        } catch (Exception e) {
            e.printStackTrace();
        }
```
**Explanation:**
- `catch` - handles errors from try block
- `Exception e` - catches any exception
- `printStackTrace()` - prints error details to console

```java
    public static DatabaseConnection getInstance() {
```
**Explanation:**
- `public static` - can be called without creating object
- `getInstance()` - standard Singleton method name

```java
        if (instance == null) {
            instance = new DatabaseConnection();
        }
```
**Explanation:**
- Checks if instance exists
- If not, creates it (only once)
- This ensures only ONE instance exists

```java
        return instance;
```
**Explanation:** Returns the single instance.

```java
    public Connection getConnection() {
        return connection;
    }
```
**Explanation:** Allows other classes to get the database connection.

---

## 3. LoginController.java - Method Breakdown

### handleLogin() Method - Complete Walkthrough

```java
    @FXML
    private void handleLogin(ActionEvent event) {
```
**Explanation:**
- `@FXML` - links to FXML button's `onAction="#handleLogin"`
- `private` - only this class can call it
- `void` - doesn't return a value
- `ActionEvent event` - contains info about the button click

```java
        String emailOrUsername = emailField.getText();
```
**Explanation:**
- `emailField` - the TextField from FXML (linked via @FXML)
- `.getText()` - gets the text user typed
- Stores in `emailOrUsername` variable

```java
        if (emailOrUsername.isEmpty() || password.isEmpty()) {
```
**Explanation:**
- `isEmpty()` - checks if string is empty
- `||` - logical OR (if either is empty)
- Validates user input

```java
            errorLabel.setText("Please enter email and password");
```
**Explanation:**
- `errorLabel` - Label from FXML
- `.setText()` - sets the text to display

```java
            errorLabel.setVisible(true);
```
**Explanation:** Makes the error label visible (was hidden by default).

```java
            return;
```
**Explanation:** Exits the method early (stops execution).

```java
        try {
```
**Explanation:** Starts try block for database operations (might fail).

```java
            Connection conn = DatabaseConnection.getInstance().getConnection();
```
**Explanation:**
- Gets the single database connection instance
- `Connection` - Java interface for database connections

```java
            String sql = "SELECT id, email, password, role, full_name, is_active " +
                        "FROM users WHERE email = ? OR username = ?";
```
**Explanation:**
- SQL query string
- `?` - placeholder (prevents SQL injection)
- `OR` - matches email OR username

```java
            PreparedStatement stmt = conn.prepareStatement(sql);
```
**Explanation:**
- `PreparedStatement` - pre-compiled SQL statement
- Safer and faster than regular Statement
- `prepareStatement()` - prepares the SQL

```java
            stmt.setString(1, emailOrUsername);
```
**Explanation:**
- `setString()` - sets a string parameter
- `1` - first `?` in the SQL
- Replaces `?` with actual value

```java
            ResultSet rs = stmt.executeQuery();
```
**Explanation:**
- `executeQuery()` - runs SELECT query
- Returns `ResultSet` - contains query results

```java
            if (rs.next()) {
```
**Explanation:**
- `rs.next()` - moves to first row
- Returns `true` if row exists, `false` if empty
- Must call before reading data

```java
                String storedPassword = rs.getString("password");
```
**Explanation:**
- `getString("password")` - gets value from "password" column
- Returns as String

```java
                if (password.equals(storedPassword) && isActive) {
```
**Explanation:**
- `equals()` - compares strings (not ==)
- `&&` - logical AND (both must be true)
- Checks password matches AND user is active

```java
                    SessionManager.getInstance().setCurrentUser(
                        userId, emailOrUsername, role, fullName
                    );
```
**Explanation:**
- Gets SessionManager instance
- Stores user info for later use

```java
                    if ("admin".equalsIgnoreCase(role)) {
```
**Explanation:**
- `equalsIgnoreCase()` - case-insensitive comparison
- Safer than `role.equals("admin")` (won't crash if role is null)

```java
                        NavigationUtil.switchScene(event, "/view/admin/admin-dashboard.fxml");
```
**Explanation:**
- Calls static method to change screen
- Passes event and path to new FXML file

```java
        } catch (SQLException e) {
```
**Explanation:**
- Catches database-related errors
- `SQLException` - specific exception type for SQL errors

```java
            errorLabel.setText("Database error: " + e.getMessage());
```
**Explanation:**
- `e.getMessage()` - gets error message
- Displays to user

---

## 4. FXML Structure - login.fxml Explained

```xml
<?xml version="1.0" encoding="UTF-8"?>
```
**Explanation:** XML declaration - specifies version and character encoding.

```xml
<BorderPane xmlns="http://javafx.com/javafx"
```
**Explanation:**
- `BorderPane` - layout container (divides into top, bottom, left, right, center)
- `xmlns` - XML namespace (defines valid elements)

```xml
            fx:controller="com.gym.controller.auth.LoginController">
```
**Explanation:**
- Links this FXML to LoginController.java
- JavaFX will create controller instance

```xml
    <top>
        <VBox>
```
**Explanation:**
- `top` - top section of BorderPane
- `VBox` - vertical box (stacks children vertically)

```xml
            <Label text="Gym Management System" styleClass="login-title"/>
```
**Explanation:**
- `Label` - displays text
- `text` - the text to display
- `styleClass` - applies CSS class "login-title"

```xml
            <TextField fx:id="emailField"
```
**Explanation:**
- `TextField` - text input box
- `fx:id` - links to `@FXML private TextField emailField;` in controller

```xml
                      promptText="Email or Username"
```
**Explanation:** Placeholder text shown when field is empty.

```xml
            <Button text="Login" onAction="#handleLogin"/>
```
**Explanation:**
- `Button` - clickable button
- `text` - button label
- `onAction="#handleLogin"` - calls `handleLogin()` method when clicked

---

## 5. SQL Query Patterns

### SELECT Query
```sql
SELECT column1, column2 
FROM table_name 
WHERE condition
```
**Explanation:**
- `SELECT` - choose which columns
- `FROM` - which table
- `WHERE` - filter rows

### INSERT Query
```sql
INSERT INTO table_name (column1, column2) 
VALUES (value1, value2)
```
**Explanation:**
- `INSERT INTO` - add new row
- Column names in parentheses
- Values in parentheses (in same order)

### UPDATE Query
```sql
UPDATE table_name 
SET column1 = value1 
WHERE condition
```
**Explanation:**
- `UPDATE` - modify existing rows
- `SET` - what to change
- `WHERE` - which rows to update

### DELETE Query
```sql
DELETE FROM table_name 
WHERE condition
```
**Explanation:**
- `DELETE FROM` - remove rows
- `WHERE` - which rows to delete (be careful!)

---

## 6. Common JavaFX Components

### TextField
```java
@FXML
private TextField nameField;
```
**Purpose:** Single-line text input

### PasswordField
```java
@FXML
private PasswordField passwordField;
```
**Purpose:** Text input that hides characters

### Button
```java
@FXML
private Button submitButton;
```
**Purpose:** Clickable button

### Label
```java
@FXML
private Label messageLabel;
```
**Purpose:** Displays text (read-only)

### TableView
```java
@FXML
private TableView<UserData> usersTable;
```
**Purpose:** Displays data in table format

### ComboBox
```java
@FXML
private ComboBox<String> roleFilter;
```
**Purpose:** Dropdown selection list

---

## 7. Exception Handling

### Try-Catch Block
```java
try {
    // Code that might fail
    riskyOperation();
} catch (ExceptionType e) {
    // Handle the error
    handleError(e);
}
```

### Multiple Catch Blocks
```java
try {
    // Code
} catch (SQLException e) {
    // Handle SQL errors
} catch (IOException e) {
    // Handle file errors
} catch (Exception e) {
    // Handle any other errors
}
```

### Finally Block
```java
try {
    // Code
} catch (Exception e) {
    // Handle error
} finally {
    // Always executes (cleanup code)
    closeResources();
}
```

---

## 8. Java Collections Used

### ObservableList
```java
ObservableList<UserData> users = FXCollections.observableArrayList();
```
**Purpose:** List that automatically updates UI when changed
**Used with:** TableView, ListView

### ArrayList
```java
List<String> names = new ArrayList<>();
```
**Purpose:** Dynamic array (can grow/shrink)

### HashMap
```java
Map<String, Integer> scores = new HashMap<>();
```
**Purpose:** Key-value pairs (like dictionary)

---

## 9. String Operations

### Concatenation
```java
String fullName = firstName + " " + lastName;
```

### Comparison
```java
if (name.equals("John")) { }  // Case-sensitive
if (name.equalsIgnoreCase("john")) { }  // Case-insensitive
```

### Empty Check
```java
if (text.isEmpty()) { }  // True if length is 0
if (text == null || text.isEmpty()) { }  // Safe check
```

### Substring
```java
String part = text.substring(0, 5);  // First 5 characters
```

---

## 10. Date and Time

### Current Timestamp
```java
Timestamp now = new Timestamp(System.currentTimeMillis());
```

### Format Date
```java
SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
String formatted = formatter.format(date);
```

---

This walkthrough covers the fundamental concepts. Study each section, run the code, and experiment with modifications to deepen your understanding!

