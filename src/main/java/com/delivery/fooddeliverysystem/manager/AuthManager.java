package com.delivery.fooddeliverysystem.manager;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.exception.DuplicateEntryException;
import com.delivery.fooddeliverysystem.exception.InvalidInputException;
import com.delivery.fooddeliverysystem.model.UserAccount;
import com.delivery.fooddeliverysystem.model.UserRole;
import com.delivery.fooddeliverysystem.util.AppLogger;
import com.delivery.fooddeliverysystem.util.FileHandler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AuthManager {
    private final LinkedList<UserAccount> accounts = new LinkedList<>();
    private static final String FILE = "accounts.csv";

    public AuthManager() {
        List<String[]> rows = FileHandler.readCSV(FILE);
        for (String[] r : rows) {
            if (r.length >= 4)
                accounts.add(new UserAccount(r[0], r[1], UserRole.valueOf(r[2]), r[3]));
        }
        // Seed a default admin if no accounts exist
        if (accounts.isEmpty()) {
            accounts.add(new UserAccount("admin", hash("admin123"), UserRole.ADMIN, ""));
            persist();
            AppLogger.info("Default admin account created (username: admin, password: admin123)");
        }
    }

    public UserAccount login(String username, String password) throws DeliverySystemException {
        if (username == null || username.isBlank()) throw new InvalidInputException("Username is required.");
        if (password == null || password.isBlank()) throw new InvalidInputException("Password is required.");
        String hashed = hash(password);
        return accounts.stream()
                .filter(a -> a.getUsername().equalsIgnoreCase(username) && a.getPasswordHash().equals(hashed))
                .findFirst()
                .orElseThrow(() -> new DeliverySystemException("Invalid username or password."));
    }

    public void register(String username, String password, UserRole role, String linkedId)
            throws DeliverySystemException {
        if (username == null || username.isBlank()) throw new InvalidInputException("Username is required.");
        if (password == null || password.length() < 6)  throw new InvalidInputException("Password must be at least 6 characters.");
        if (accounts.stream().anyMatch(a -> a.getUsername().equalsIgnoreCase(username)))
            throw new DuplicateEntryException(username);
        accounts.add(new UserAccount(username, hash(password), role, linkedId));
        persist();
        AppLogger.info("New account registered: " + username + " [" + role + "]");
    }

    public boolean usernameExists(String username) {
        return accounts.stream().anyMatch(a -> a.getUsername().equalsIgnoreCase(username));
    }

    private void persist() {
        FileHandler.writeCSV(FILE, accounts.stream().map(UserAccount::toString).collect(Collectors.toList()));
    }

    public static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
