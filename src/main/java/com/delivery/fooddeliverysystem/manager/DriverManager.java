package com.delivery.fooddeliverysystem.manager;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.exception.DuplicateEntryException;
import com.delivery.fooddeliverysystem.exception.InvalidInputException;
import com.delivery.fooddeliverysystem.exception.ItemNotFoundException;
import com.delivery.fooddeliverysystem.exception.NoDriverAvailableException;
import com.delivery.fooddeliverysystem.model.DeliveryDriver;
import com.delivery.fooddeliverysystem.util.FileHandler;
import com.delivery.fooddeliverysystem.util.AppLogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DriverManager implements Repository<DeliveryDriver> {
    private final LinkedList<DeliveryDriver> drivers = new LinkedList<>();
    private static final String FILE = "drivers.csv";

    public DriverManager() {
        List<String[]> rows = FileHandler.readCSV(FILE);
        for (String[] r : rows) {
            if (r.length >= 5) {
                DeliveryDriver d = new DeliveryDriver(r[0], r[1], r[2], r[3]);
                d.setAvailable(Boolean.parseBoolean(r[4]));
                drivers.add(d);
            }
        }
    }

    @Override
    public void add(DeliveryDriver d) throws DeliverySystemException {
        if (d.getId() == null || d.getId().isBlank()) throw new InvalidInputException("Driver ID is required.");
        if (d.getName() == null || d.getName().isBlank()) throw new InvalidInputException("Driver name is required.");
        if (drivers.stream().anyMatch(x -> x.getId().equals(d.getId()))) throw new DuplicateEntryException(d.getId());
        drivers.add(d);
        persist();
        AppLogger.info("Driver added: " + d.getId());
    }

    @Override
    public boolean remove(String id) throws DeliverySystemException {
        DeliveryDriver found = findById(id);
        drivers.remove(found);
        persist();
        AppLogger.info("Driver removed: " + id);
        return true;
    }

    @Override
    public DeliveryDriver findById(String id) throws DeliverySystemException {
        return drivers.stream().filter(d -> d.getId().equals(id))
                .findFirst().orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Override
    public List<DeliveryDriver> getAll() { return new ArrayList<>(drivers); }

    @Override
    public List<DeliveryDriver> search(String keyword) {
        String kw = keyword.toLowerCase();
        return drivers.stream()
                .filter(d -> d.getName().toLowerCase().contains(kw)
                        || d.getId().toLowerCase().contains(kw)
                        || d.getVehicleType().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    public DeliveryDriver getAvailableDriver() throws NoDriverAvailableException {
        return drivers.stream().filter(DeliveryDriver::isAvailable)
                .findFirst().orElseThrow(NoDriverAvailableException::new);
    }

    public List<DeliveryDriver> getSortedByName() {
        return drivers.stream().sorted(Comparator.comparing(DeliveryDriver::getName)).collect(Collectors.toList());
    }

    public void persist() {
        List<String> lines = drivers.stream().map(DeliveryDriver::toString).collect(Collectors.toList());
        FileHandler.writeCSV(FILE, lines);
    }
}
