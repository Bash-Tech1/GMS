package com.gym.controller.trainer;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
import com.gym.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class MarkAttendanceController implements Initializable {

    @FXML private ComboBox<String> classCombo;
    @FXML private TableView<BookingData> bookingsTable;
    
    private ObservableList<BookingData> bookings = FXCollections.observableArrayList();
    private int trainerId;
    private int selectedClassId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainerId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadClasses();
    }

    private void setupTable() {
        bookingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/trainer-dashboard.fxml");
    }

    @FXML
    private void handleLoadBookings(ActionEvent event) {
        String selection = classCombo.getValue();
        if (selection == null) {
            showError("Please select a class first.");
            return;
        }
        selectedClassId = Integer.parseInt(selection.split(" - ")[0]);
        loadBookings();
    }

    @FXML
    private void handleMarkPresent(ActionEvent event) {
        markAttendance("present");
    }

    @FXML
    private void handleMarkAbsent(ActionEvent event) {
        markAttendance("absent");
    }

    @FXML
    private void handleMarkLate(ActionEvent event) {
        markAttendance("late");
    }

    private void markAttendance(String status) {
        BookingData selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a booking to mark attendance.");
            return;
        }

        if (selectedClassId == -1) {
            showError("Please select a class first.");
            return;
        }

        String sql = "INSERT INTO attendance (user_id, class_session_id, booking_id, check_in_time, status) " +
                     "VALUES (?, ?, ?, NOW(), ?) " +
                     "ON DUPLICATE KEY UPDATE status = ?, check_in_time = NOW()";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selected.getUserId());
            stmt.setInt(2, selectedClassId);
            stmt.setInt(3, selected.getBookingId());
            stmt.setString(4, status);
            stmt.setString(5, status);
            stmt.executeUpdate();
            showSuccess("Attendance marked as " + status + "!");
            loadBookings();
        } catch (SQLException e) {
            showError("Error marking attendance: " + e.getMessage());
        }
    }

    private void loadClasses() {
        classCombo.getItems().clear();
        String sql = "SELECT id, title, start_time FROM class_sessions WHERE trainer_id = ? AND status IN ('scheduled', 'ongoing') ORDER BY start_time DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    classCombo.getItems().add(rs.getInt("id") + " - " + rs.getString("title") + " (" + rs.getTimestamp("start_time") + ")");
                }
            }
        } catch (SQLException e) {
            showError("Error loading classes: " + e.getMessage());
        }
    }

    private void loadBookings() {
        bookings.clear();
        String sql = "SELECT b.id as booking_id, u.id as user_id, u.full_name as member_name, b.status as booking_status, " +
                     "COALESCE(a.status, 'not_marked') as attendance_status " +
                     "FROM bookings b " +
                     "JOIN users u ON b.user_id = u.id " +
                     "LEFT JOIN attendance a ON b.user_id = a.user_id AND b.class_session_id = a.class_session_id " +
                     "WHERE b.class_session_id = ? AND b.status = 'confirmed'";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selectedClassId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BookingData booking = new BookingData(
                        rs.getInt("booking_id"),
                        rs.getInt("user_id"),
                        rs.getString("member_name"),
                        rs.getString("booking_status"),
                        rs.getString("attendance_status")
                    );
                    bookings.add(booking);
                }
            }
            bookingsTable.setItems(bookings);
        } catch (SQLException e) {
            showError("Error loading bookings: " + e.getMessage());
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

    public static class BookingData {
        private int bookingId;
        private int userId;
        private String memberName;
        private String bookingStatus;
        private String attendanceStatus;

        public BookingData(int bookingId, int userId, String memberName, String bookingStatus, String attendanceStatus) {
            this.bookingId = bookingId;
            this.userId = userId;
            this.memberName = memberName;
            this.bookingStatus = bookingStatus;
            this.attendanceStatus = attendanceStatus;
        }

        public int getBookingId() { return bookingId; }
        public int getUserId() { return userId; }
        public String getMemberName() { return memberName; }
        public String getBookingStatus() { return bookingStatus; }
        public String getAttendanceStatus() { return attendanceStatus.equals("not_marked") ? "Not Marked" : attendanceStatus; }
    }
}

