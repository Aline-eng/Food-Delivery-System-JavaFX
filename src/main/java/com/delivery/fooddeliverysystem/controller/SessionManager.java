package com.delivery.fooddeliverysystem.controller;

import com.delivery.fooddeliverysystem.model.UserAccount;
import com.delivery.fooddeliverysystem.model.UserRole;

public class SessionManager {
    private static SessionManager instance;
    private UserAccount currentUser;

    private SessionManager() {}

    public static SessionManager get() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void login(UserAccount user) { this.currentUser = user; }
    public void logout()                { this.currentUser = null; }

    public UserAccount getCurrentUser() { return currentUser; }
    public boolean isLoggedIn()         { return currentUser != null; }
    public boolean isAdmin()            { return isLoggedIn() && currentUser.getRole() == UserRole.ADMIN; }
    public boolean isCustomer()         { return isLoggedIn() && currentUser.getRole() == UserRole.CUSTOMER; }
    public boolean isDriver()           { return isLoggedIn() && currentUser.getRole() == UserRole.DRIVER; }
    public String getLinkedId()         { return isLoggedIn() ? currentUser.getLinkedId() : ""; }
}
