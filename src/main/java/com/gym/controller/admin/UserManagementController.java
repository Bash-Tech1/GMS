package com.gym.controller.admin;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private TableView<UserData> usersTable;
    @FXML private ComboBox<String> roleFilter;
    @FXML private TextField searchField;

    private ObservableList<UserData> allUsers = FXCollections.observableArrayList();
    private FilteredList<UserData> filteredUsers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilters();
        loadUsers();
    }

    private void setupTable() {
        usersTable.setItems(allUsers);
        usersTable.setRowFactory(tv -> {
            TableRow<UserData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    UserData user = row.getItem();
                    showEditDialog(user);
                }
            });
            return row;
        });
    }

    private void setupFilters() {
        roleFilter.getItems().addAll("All", "admin", "trainer", "member");
        roleFilter.setValue("All");
        
        filteredUsers = new FilteredList<>(allUsers, p -> true);
        usersTable.setItems(filteredUsers);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/admin-dashboard.fxml");
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        showAddDialog();
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        UserData selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a user to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete User");
        confirm.setContentText("Are you sure you want to delete user: " + selected.getFullName() + "?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM users WHERE id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selected.getId());
                stmt.executeUpdate();
                showSuccess("User deleted successfully!");
                loadUsers();
            } catch (SQLException e) {
                showError("Error deleting user: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        updateFilter();
    }

    @FXML
    private void handleSearch() {
        updateFilter();
    }

    private void updateFilter() {
        String role = roleFilter.getValue();
        String search = searchField.getText().toLowerCase();
        
        filteredUsers.setPredicate(user -> {
            boolean roleMatch = role == null || role.equals("All") || user.getRole().equalsIgnoreCase(role);
            boolean searchMatch = search.isEmpty() || 
                user.getFullName().toLowerCase().contains(search) ||
                user.getEmail().toLowerCase().contains(search);
            return roleMatch && searchMatch;
        });
    }

    private void loadUsers() {
        allUsers.clear();
        String sql = "SELECT id, email, username, full_name, phone, role, plan_id, is_active FROM users ORDER BY id";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                UserData user = new UserData(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("role"),
                    rs.getObject("plan_id") != null ? rs.getInt("plan_id") : 0,
                    rs.getBoolean("is_active")
                );
                allUsers.add(user);
            }
        } catch (SQLException e) {
            showError("Error loading users: " + e.getMessage());
        }
    }

    private void showAddDialog() {
        Dialog<UserData> dialog = createUserDialog(null);
        dialog.showAndWait().ifPresent(user -> {
            if (saveUser(user, true)) {
                loadUsers();
            }
        });
    }

    private void showEditDialog(UserData user) {
        Dialog<UserData> dialog = createUserDialog(user);
        dialog.showAndWait().ifPresent(updatedUser -> {
            if (saveUser(updatedUser, false)) {
                loadUsers();
            }
        });
    }

    private Dialog<UserData> createUserDialog(UserData existingUser) {
        Dialog<UserData> dialog = new Dialog<>();
        dialog.setTitle(existingUser == null ? "Add New User" : "Edit User");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");
        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("admin", "trainer", "member"));
        roleCombo.setPromptText("Role");
        TextField planIdField = new TextField();
        planIdField.setPromptText("Plan ID (optional)");
        CheckBox activeCheck = new CheckBox("Active");

        if (existingUser != null) {
            emailField.setText(existingUser.getEmail());
            usernameField.setText(existingUser.getUsername());
            fullNameField.setText(existingUser.getFullName());
            phoneField.setText(existingUser.getPhone() != null ? existingUser.getPhone() : "");
            roleCombo.setValue(existingUser.getRole());
            planIdField.setText(existingUser.getPlanId() > 0 ? String.valueOf(existingUser.getPlanId()) : "");
            activeCheck.setSelected(existingUser.isActive());
        }

        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Full Name:"), 0, 2);
        grid.add(fullNameField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Password:"), 0, 4);
        grid.add(passwordField, 1, 4);
        grid.add(new Label("Role:"), 0, 5);
        grid.add(roleCombo, 1, 5);
        grid.add(new Label("Plan ID:"), 0, 6);
        grid.add(planIdField, 1, 6);
        grid.add(activeCheck, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int planId = planIdField.getText().isEmpty() ? 0 : Integer.parseInt(planIdField.getText());
                    UserData user = new UserData(
                        existingUser != null ? existingUser.getId() : 0,
                        emailField.getText(),
                        usernameField.getText(),
                        fullNameField.getText(),
                        phoneField.getText(),
                        roleCombo.getValue(),
                        planId,
                        activeCheck.isSelected()
                    );
                    user.setPassword(passwordField.getText());
                    return user;
                } catch (NumberFormatException e) {
                    showError("Invalid Plan ID");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private boolean saveUser(UserData user, boolean isNew) {
        String sql;
        if (isNew) {
            sql = "INSERT INTO users (email, password, username, full_name, phone, role, plan_id, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                sql = "UPDATE users SET email=?, password=?, username=?, full_name=?, phone=?, role=?, plan_id=?, is_active=? WHERE id=?";
            } else {
                sql = "UPDATE users SET email=?, username=?, full_name=?, phone=?, role=?, plan_id=?, is_active=? WHERE id=?";
            }
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            stmt.setString(paramIndex++, user.getEmail());
            if (isNew || (user.getPassword() != null && !user.getPassword().isEmpty())) {
                stmt.setString(paramIndex++, user.getPassword());
            }
            stmt.setString(paramIndex++, user.getUsername());
            stmt.setString(paramIndex++, user.getFullName());
            stmt.setString(paramIndex++, user.getPhone());
            stmt.setString(paramIndex++, user.getRole());
            if (user.getPlanId() > 0) {
                stmt.setInt(paramIndex++, user.getPlanId());
            } else {
                stmt.setNull(paramIndex++, Types.INTEGER);
            }
            stmt.setBoolean(paramIndex++, user.isActive());
            
            if (!isNew) {
                stmt.setInt(paramIndex, user.getId());
            }

            stmt.executeUpdate();
            showSuccess(isNew ? "User added successfully!" : "User updated successfully!");
            return true;
        } catch (SQLException e) {
            showError("Error saving user: " + e.getMessage());
            return false;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for table data
    public static class UserData {
        private int id;
        private String email;
        private String username;
        private String fullName;
        private String phone;
        private String role;
        private int planId;
        private boolean active;
        private String password;

        public UserData(int id, String email, String username, String fullName, String phone, String role, int planId, boolean active) {
            this.id = id;
            this.email = email;
            this.username = username;
            this.fullName = fullName;
            this.phone = phone;
            this.role = role;
            this.planId = planId;
            this.active = active;
        }

        // Getters
        public int getId() { return id; }
        public String getEmail() { return email; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getPhone() { return phone; }
        public String getRole() { return role; }
        public int getPlanId() { return planId; }
        public boolean isActive() { return active; }
        public String getPassword() { return password; }

        // Setters
        public void setPassword(String password) { this.password = password; }
    }
}

