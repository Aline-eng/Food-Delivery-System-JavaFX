package com.delivery.fooddeliverysystem.manager;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import java.util.List;

public interface Repository<T> {
    void add(T item) throws DeliverySystemException;
    boolean remove(String id) throws DeliverySystemException;
    T findById(String id) throws DeliverySystemException;
    List<T> getAll();
    List<T> search(String keyword);
}
