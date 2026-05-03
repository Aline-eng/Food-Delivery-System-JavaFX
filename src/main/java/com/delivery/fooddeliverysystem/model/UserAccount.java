package com.delivery.fooddeliverysystem.model;

public class UserAccount {
    private String username;
    private String passwordHash;
    private UserRole role;
    private String linkedId; // Customer ID or Driver ID, empty for ADMIN

    public UserAccount(String username, String passwordHash, UserRole role, String linkedId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.linkedId = linkedId;
    }

    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole()       { return role; }
    public String getLinkedId()     { return linkedId; }

    /** CSV: username,passwordHash,role,linkedId */
    @Override
    public String toString() {
        return username + "," + passwordHash + "," + role.name() + "," + linkedId;
    }
}
