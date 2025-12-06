package com.gym.controller.member;

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

public class MyBookingsController implements Initializable {

    @FXML private TableView<BookingData> bookingsTable;
    private ObservableList<BookingData> bookings = FXCollections.observableArrayList();
    private int memberId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        memberId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadBookings();
    }

    private void setupTable() {
        bookingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        BookingData selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a booking to cancel.");
            return;
        }

        if (!"confirmed".equals(selected.getStatus())) {
            showError("Only confirmed bookings can be cancelled.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Cancel");
        confirm.setHeaderText("Cancel Booking");
        confirm.setContentText("Are you sure you want to cancel this booking?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "UPDATE bookings SET status = 'cancelled', cancelled_at = NOW() WHERE id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selected.getId());
                stmt.executeUpdate();
                
                // Update enrollment count
                String updateSql = "UPDATE class_sessions SET current_enrollment = GREATEST(0, current_enrollment - 1) WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, selected.getClassId());
                    updateStmt.executeUpdate();
                }
                
                showSuccess("Booking cancelled successfully!");
                loadBookings();
            } catch (SQLException e) {
                showError("Error cancelling booking: " + e.getMessage());
            }
        }
    }

    private void loadBookings() {
        bookings.clear();
        String sql = "SELECT b.id, cs.id as class_id, cs.title as class_name, u.full_name as trainer_name, " +
                     "cs.start_time, b.booking_date, b.status " +
                     "FROM bookings b " +
                     "JOIN class_sessions cs ON b.class_session_id = cs.id " +
                     "JOIN users u ON cs.trainer_id = u.id " +
                     "WHERE b.user_id = ? " +
                     "ORDER BY b.booking_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BookingData booking = new BookingData(
                        rs.getInt("id"),
                        rs.getInt("class_id"),
                        rs.getString("class_name"),
                        rs.getString("trainer_name"),
                        rs.getTimestamp("start_time"),
                        rs.getTimestamp("booking_date"),
                        rs.getString("status")
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
        private int id;
        private int classId;
        private String className;
        private String trainerName;
        private Timestamp classTime;
        private Timestamp bookingDate;
        private String status;

        public BookingData(int id, int classId, String className, String trainerName, Timestamp classTime, Timestamp bookingDate, String status) {
            this.id = id;
            this.classId = classId;
            this.className = className;
            this.trainerName = trainerName;
            this.classTime = classTime;
            this.bookingDate = bookingDate;
            this.status = status;
        }

        public int getId() { return id; }
        public int getClassId() { return classId; }
        public String getClassName() { return className; }
        public String getTrainerName() { return trainerName; }
        public Timestamp getClassTime() { return classTime; }
        public Timestamp getBookingDate() { return bookingDate; }
        public String getStatus() { return status; }
        public String getClassTimeStr() { return classTime != null ? classTime.toString() : ""; }
        public String getBookingDateStr() { return bookingDate != null ? bookingDate.toString() : ""; }
    }
}

