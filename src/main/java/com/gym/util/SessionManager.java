package com.gym.util;

public class SessionManager {
    private static SessionManager instance;
    private int currentUserId;
    private String currentUserEmail;
    private String currentUserRole;
    private String currentUserName;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(int userId, String email, String role, String name) {
        this.currentUserId = userId;
        this.currentUserEmail = email;
        this.currentUserRole = role;
        this.currentUserName = name;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public String getCurrentUserRole() {
        return currentUserRole;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public void clearSession() {
        this.currentUserId = 0;
        this.currentUserEmail = null;
        this.currentUserRole = null;
        this.currentUserName = null;
    }
}

