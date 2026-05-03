package com.delivery.fooddeliverysystem.manager;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.exception.InvalidInputException;
import com.delivery.fooddeliverysystem.exception.ItemNotFoundException;
import com.delivery.fooddeliverysystem.model.*;
import com.delivery.fooddeliverysystem.util.FileHandler;
import com.delivery.fooddeliverysystem.util.AppLogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderManager implements Repository<Order> {
    private final LinkedList<Order> orders = new LinkedList<>();
    private static final String FILE = "orders.csv";

    private final CustomerManager customerManager;
    private final RestaurantManager restaurantManager;
    private final DriverManager driverManager;

    public OrderManager(CustomerManager cm, RestaurantManager rm, DriverManager dm) {
        this.customerManager = cm;
        this.restaurantManager = rm;
        this.driverManager = dm;
        loadOrders();
    }

    private void loadOrders() {
        List<String[]> rows = FileHandler.readCSV(FILE);
        for (String[] r : rows) {
            if (r.length < 5) continue;
            try {
                Customer c = customerManager.findById(r[1]);
                Restaurant rest = restaurantManager.findById(r[2]);
                Order o = new Order(r[0], c, rest);
                o.setStatus(OrderStatus.valueOf(r[3]));
                if (r.length >= 6 && !r[5].equals("NONE")) {
                    DeliveryDriver d = driverManager.findById(r[5]);
                    o.setDriver(d);
                }
                orders.add(o);
            } catch (DeliverySystemException ignored) {}
        }
    }

    @Override
    public void add(Order o) throws DeliverySystemException {
        if (o.getItems().isEmpty()) throw new InvalidInputException("Order must have at least one item.");
        orders.add(o);
        persist();
        AppLogger.info("Order placed: " + o.getOrderId());
    }

    @Override
    public boolean remove(String id) throws DeliverySystemException {
        Order found = findById(id);
        orders.remove(found);
        persist();
        AppLogger.info("Order removed: " + id);
        return true;
    }

    @Override
    public Order findById(String id) throws DeliverySystemException {
        return orders.stream().filter(o -> o.getOrderId().equals(id))
                .findFirst().orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Override
    public List<Order> getAll() { return new ArrayList<>(orders); }

    @Override
    public List<Order> search(String keyword) {
        String kw = keyword.toLowerCase();
        return orders.stream()
                .filter(o -> o.getOrderId().toLowerCase().contains(kw)
                        || o.getCustomer().getName().toLowerCase().contains(kw)
                        || o.getRestaurant().getName().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    public void assignDriver(String orderId) throws DeliverySystemException {
        Order o = findById(orderId);
        DeliveryDriver driver = driverManager.getAvailableDriver();
        driver.setAvailable(false);
        o.setDriver(driver);
        o.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        driverManager.persist();
        persist();
        AppLogger.info("Driver " + driver.getId() + " assigned to order " + orderId);
    }

    public void updateStatus(String orderId, OrderStatus status) throws DeliverySystemException {
        Order o = findById(orderId);
        o.setStatus(status);
        if (status == OrderStatus.DELIVERED && o.getDriver() != null) {
            o.getDriver().setAvailable(true);
            driverManager.persist();
        }
        persist();
        AppLogger.info("Order " + orderId + " status updated to " + status);
    }

    public List<Order> getSortedByTime() {
        return orders.stream().sorted(Comparator.comparing(Order::getOrderTime).reversed()).collect(Collectors.toList());
    }

    public List<Order> getByStatus(OrderStatus status) {
        return orders.stream().filter(o -> o.getStatus() == status).collect(Collectors.toList());
    }

    private void persist() {
        List<String> lines = orders.stream().map(Order::toString).collect(Collectors.toList());
        FileHandler.writeCSV(FILE, lines);
    }
}
