package com.delivery.fooddeliverysystem.util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private static final Path DATA_DIR = Paths.get(System.getProperty("user.home"), ".fooddelivery");

    static {
        try { Files.createDirectories(DATA_DIR); }
        catch (IOException e) { AppLogger.error("Failed to create data directory: " + e.getMessage()); }
    }

    public static List<String[]> readCSV(String filename) {
        List<String[]> result = new ArrayList<>();
        Path file = DATA_DIR.resolve(filename);
        if (!Files.exists(file)) return result;
        try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) result.add(line.split(",", -1));
            }
        } catch (IOException e) {
            AppLogger.error("Error reading " + filename + ": " + e.getMessage());
        }
        return result;
    }

    public static void writeCSV(String filename, List<String> lines) {
        Path file = DATA_DIR.resolve(filename);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file.toFile()))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            AppLogger.error("Error writing " + filename + ": " + e.getMessage());
        }
    }

    public static Path getDataDir() { return DATA_DIR; }
}
