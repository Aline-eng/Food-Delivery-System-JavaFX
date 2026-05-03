package com.delivery.fooddeliverysystem.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogger {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Path LOG_FILE = Paths.get(System.getProperty("user.home"), ".fooddelivery", "app.log");

    public static void info(String message) { log("INFO", message); }
    public static void error(String message) { log("ERROR", message); }
    public static void warn(String message) { log("WARN", message); }

    private static void log(String level, String message) {
        String entry = "[" + LocalDateTime.now().format(FMT) + "] [" + level + "] " + message;
        System.out.println(entry);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE.toFile(), true))) {
            bw.write(entry);
            bw.newLine();
        } catch (IOException ignored) {}
    }
}
