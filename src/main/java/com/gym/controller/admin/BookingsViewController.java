package com.gym.controller.admin;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
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

public class BookingsViewController implements Initializable {

    @FXML private TableView<BookingData> bookingsTable;
    private ObservableList<BookingData> bookings = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadBookings();
    }

    private void setupTable() {
        bookingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/admin-dashboard.fxml");
    }

    private void loadBookings() {
        bookings.clear();
        String sql = "SELECT b.id, u.full_name as member_name, cs.title as class_name, " +
                     "b.booking_date, b.status " +
                     "FROM bookings b " +
                     "JOIN users u ON b.user_id = u.id " +
                     "JOIN class_sessions cs ON b.class_session_id = cs.id " +
                     "ORDER BY b.booking_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                BookingData booking = new BookingData(
                    rs.getInt("id"),
                    rs.getString("member_name"),
                    rs.getString("class_name"),
                    rs.getTimestamp("booking_date"),
                    rs.getString("status")
                );
                bookings.add(booking);
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

    public static class BookingData {
        private int id;
        private String memberName;
        private String className;
        private Timestamp bookingDate;
        private String status;

        public BookingData(int id, String memberName, String className, Timestamp bookingDate, String status) {
            this.id = id;
            this.memberName = memberName;
            this.className = className;
            this.bookingDate = bookingDate;
            this.status = status;
        }

        public int getId() { return id; }
        public String getMemberName() { return memberName; }
        public String getClassName() { return className; }
        public Timestamp getBookingDate() { return bookingDate; }
        public String getStatus() { return status; }
        
        public String getBookingDateStr() {
            return bookingDate != null ? bookingDate.toString() : "";
        }
    }
}

