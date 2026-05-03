package com.delivery.fooddeliverysystem.manager;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.exception.DuplicateEntryException;
import com.delivery.fooddeliverysystem.exception.InvalidInputException;
import com.delivery.fooddeliverysystem.exception.ItemNotFoundException;
import com.delivery.fooddeliverysystem.model.MenuItem;
import com.delivery.fooddeliverysystem.model.Restaurant;
import com.delivery.fooddeliverysystem.util.FileHandler;
import com.delivery.fooddeliverysystem.util.AppLogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class RestaurantManager implements Repository<Restaurant> {
    private final LinkedList<Restaurant> restaurants = new LinkedList<>();
    private static final String FILE = "restaurants.csv";
    private static final String MENU_FILE = "menu_items.csv";

    public RestaurantManager() {
        List<String[]> rows = FileHandler.readCSV(FILE);
        for (String[] r : rows) {
            if (r.length >= 3)
                restaurants.add(new Restaurant(r[0], r[1], r[2]));
        }
        List<String[]> menuRows = FileHandler.readCSV(MENU_FILE);
        for (String[] r : menuRows) {
            if (r.length >= 5) {
                Restaurant rest = restaurants.stream()
                        .filter(res -> res.getRestaurantId().equals(r[0])).findFirst().orElse(null);
                if (rest != null)
                    rest.addMenuItem(new MenuItem(r[1], r[2], Double.parseDouble(r[3]), r[4]));
            }
        }
    }

    @Override
    public void add(Restaurant r) throws DeliverySystemException {
        if (r.getRestaurantId() == null || r.getRestaurantId().isBlank()) throw new InvalidInputException("Restaurant ID is required.");
        if (r.getName() == null || r.getName().isBlank()) throw new InvalidInputException("Restaurant name is required.");
        if (restaurants.stream().anyMatch(x -> x.getRestaurantId().equals(r.getRestaurantId()))) throw new DuplicateEntryException(r.getRestaurantId());
        restaurants.add(r);
        persistRestaurants();
        AppLogger.info("Restaurant added: " + r.getRestaurantId());
    }

    @Override
    public boolean remove(String id) throws DeliverySystemException {
        Restaurant found = findById(id);
        restaurants.remove(found);
        persistRestaurants();
        AppLogger.info("Restaurant removed: " + id);
        return true;
    }

    @Override
    public Restaurant findById(String id) throws DeliverySystemException {
        return restaurants.stream().filter(r -> r.getRestaurantId().equals(id))
                .findFirst().orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Override
    public List<Restaurant> getAll() { return new ArrayList<>(restaurants); }

    @Override
    public List<Restaurant> search(String keyword) {
        String kw = keyword.toLowerCase();
        return restaurants.stream()
                .filter(r -> r.getName().toLowerCase().contains(kw)
                        || r.getCuisine().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    public List<Restaurant> getSortedByName() {
        return restaurants.stream().sorted(Comparator.comparing(Restaurant::getName)).collect(Collectors.toList());
    }

    public void addMenuItemToRestaurant(String restaurantId, MenuItem item) throws DeliverySystemException {
        Restaurant r = findById(restaurantId);
        r.addMenuItem(item);
        persistMenuItems();
        AppLogger.info("Menu item added to restaurant " + restaurantId + ": " + item.getItemId());
    }

    public boolean removeMenuItemFromRestaurant(String restaurantId, String itemId) throws DeliverySystemException {
        Restaurant r = findById(restaurantId);
        boolean removed = r.removeMenuItem(itemId);
        if (!removed) throw new ItemNotFoundException(itemId);
        persistMenuItems();
        return true;
    }

    private void persistRestaurants() {
        List<String> lines = restaurants.stream().map(Restaurant::toString).collect(Collectors.toList());
        FileHandler.writeCSV(FILE, lines);
    }

    private void persistMenuItems() {
        List<String> lines = new ArrayList<>();
        for (Restaurant r : restaurants)
            for (MenuItem m : r.getMenu())
                lines.add(r.getRestaurantId() + "," + m.toString());
        FileHandler.writeCSV(MENU_FILE, lines);
    }
}
