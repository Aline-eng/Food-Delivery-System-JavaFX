package com.delivery.fooddeliverysystem.manager;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.exception.DuplicateEntryException;
import com.delivery.fooddeliverysystem.exception.InvalidInputException;
import com.delivery.fooddeliverysystem.exception.ItemNotFoundException;
import com.delivery.fooddeliverysystem.model.Customer;
import com.delivery.fooddeliverysystem.util.FileHandler;
import com.delivery.fooddeliverysystem.util.AppLogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerManager implements Repository<Customer> {
    private final LinkedList<Customer> customers = new LinkedList<>();
    private static final String FILE = "customers.csv";

    public CustomerManager() {
        List<String[]> rows = FileHandler.readCSV(FILE);
        for (String[] r : rows) {
            if (r.length >= 4)
                customers.add(new Customer(r[0], r[1], r[2], r[3]));
        }
    }

    @Override
    public void add(Customer c) throws DeliverySystemException {
        if (c.getId() == null || c.getId().isBlank()) throw new InvalidInputException("Customer ID is required.");
        if (c.getName() == null || c.getName().isBlank()) throw new InvalidInputException("Customer name is required.");
        if (customers.stream().anyMatch(x -> x.getId().equals(c.getId()))) throw new DuplicateEntryException(c.getId());
        customers.add(c);
        persist();
        AppLogger.info("Customer added: " + c.getId());
    }

    @Override
    public boolean remove(String id) throws DeliverySystemException {
        Customer found = findById(id);
        customers.remove(found);
        persist();
        AppLogger.info("Customer removed: " + id);
        return true;
    }

    @Override
    public Customer findById(String id) throws DeliverySystemException {
        return customers.stream().filter(c -> c.getId().equals(id))
                .findFirst().orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Override
    public List<Customer> getAll() { return new ArrayList<>(customers); }

    @Override
    public List<Customer> search(String keyword) {
        String kw = keyword.toLowerCase();
        return customers.stream()
                .filter(c -> c.getName().toLowerCase().contains(kw)
                        || c.getId().toLowerCase().contains(kw)
                        || c.getAddress().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    public List<Customer> getSortedByName() {
        return customers.stream().sorted(Comparator.comparing(Customer::getName)).collect(Collectors.toList());
    }

    private void persist() {
        List<String> lines = customers.stream().map(Customer::toString).collect(Collectors.toList());
        FileHandler.writeCSV(FILE, lines);
    }
}
