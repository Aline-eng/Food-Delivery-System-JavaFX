package com.delivery.fooddeliverysystem.manager;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.exception.DuplicateEntryException;
import com.delivery.fooddeliverysystem.exception.InvalidInputException;
import com.delivery.fooddeliverysystem.model.UserAccount;
import com.delivery.fooddeliverysystem.model.UserRole;
import com.delivery.fooddeliverysystem.util.AppLogger;
import com.delivery.fooddeliverysystem.util.FileHandler;
import org.mindrot.jbcrypt.BCrypt;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AuthManager {
    private final LinkedList<UserAccount> accounts = new LinkedList<>();
    private static final String FILE = "accounts.csv";

    // BCrypt work factor — 12 is a good balance of security vs. speed
    private static final int BCRYPT_ROUNDS = 12;

    public AuthManager() {
        List<String[]> rows = FileHandler.readCSV(FILE);
        for (String[] r : rows) {
            if (r.length >= 4)
                accounts.add(new UserAccount(r[0], r[1], UserRole.valueOf(r[2]), r[3]));
        }
        if (accounts.isEmpty()) {
            // Seed default admin — password is hashed immediately, never logged in plaintext
            String hashed = BCrypt.hashpw("admin123", BCrypt.gensalt(BCRYPT_ROUNDS));
            accounts.add(new UserAccount("admin", hashed, UserRole.ADMIN, ""));
            persist();
            AppLogger.info("Default admin account seeded. Change the password after first login.");
        }
    }

    public UserAccount login(String username, String password) throws DeliverySystemException {
        if (username == null || username.isBlank()) throw new InvalidInputException("Username is required.");
        if (password == null || password.isBlank()) throw new InvalidInputException("Password is required.");

        UserAccount account = accounts.stream()
                .filter(a -> a.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElseThrow(() -> new DeliverySystemException("Invalid username or password."));

        // BCrypt.checkpw handles the salt extraction internally
        if (!BCrypt.checkpw(password, account.getPasswordHash()))
            throw new DeliverySystemException("Invalid username or password.");

        AppLogger.info("User logged in: " + username + " [" + account.getRole() + "]");
        return account;
    }

    public void register(String username, String password, UserRole role, String linkedId)
            throws DeliverySystemException {
        if (username == null || username.isBlank())
            throw new InvalidInputException("Username is required.");
        if (password == null || password.length() < 6)
            throw new InvalidInputException("Password must be at least 6 characters.");
        if (accounts.stream().anyMatch(a -> a.getUsername().equalsIgnoreCase(username)))
            throw new DuplicateEntryException(username);

        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
        accounts.add(new UserAccount(username, hashed, role, linkedId));
        persist();
        // Log only username and role — never the password
        AppLogger.info("New account registered: " + username + " [" + role + "]");
    }

    public boolean usernameExists(String username) {
        return accounts.stream().anyMatch(a -> a.getUsername().equalsIgnoreCase(username));
    }

    private void persist() {
        FileHandler.writeCSV(FILE, accounts.stream().map(UserAccount::toString).collect(Collectors.toList()));
    }
}
