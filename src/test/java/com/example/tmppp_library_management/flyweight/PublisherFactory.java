package com.example.tmppp_library_management.flyweight;

import java.util.HashMap;
import java.util.Map;

public class PublisherFactory {
    private static PublisherFactory instance;
    private Map<String, Publisher> publishers;
    private int nextId;

    private PublisherFactory() {
        this.publishers = new HashMap<>();
        this.nextId = 1;
    }

    public static PublisherFactory getInstance() {
        if (instance == null) {
            instance = new PublisherFactory();
        }
        return instance;
    }

    public Publisher getPublisher(String name) {
        if (publishers.containsKey(name)) {
            return publishers.get(name);
        }

        Publisher publisher = new Publisher(nextId++, name);
        publishers.put(name, publisher);
        return publisher;
    }
}