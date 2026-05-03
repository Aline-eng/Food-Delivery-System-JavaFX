package com.delivery.fooddeliverysystem.util;

import java.net.URL;

public class ViewLoader {

    private static final String BASE = "/com/delivery/fooddeliverysystem/fxml/";

    /**
     * Returns a URL for an FXML file by name (e.g. "login.fxml").
     * Uses the system class loader which correctly resolves absolute
     * resource paths inside a named Java module.
     */
    public static URL fxml(String filename) {
        URL url = ViewLoader.class.getResource(BASE + filename);
        if (url == null) {
            // Fallback: try the thread context class loader
            url = Thread.currentThread().getContextClassLoader()
                    .getResource("com/delivery/fooddeliverysystem/fxml/" + filename);
        }
        if (url == null) {
            throw new IllegalStateException(
                    "FXML resource not found: " + BASE + filename +
                    "\nEnsure the file exists under src/main/resources" + BASE);
        }
        return url;
    }
}
