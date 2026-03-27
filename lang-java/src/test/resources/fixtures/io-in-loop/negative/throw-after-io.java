package com.example;

import java.util.List;

public class ThrowAfterIo {
    private LogService logService;

    // The save() is inside a loop but is always followed by throw.
    // The loop runs at most one iteration — not a real IO-in-loop.
    public void validateProducts(List<Product> products) {
        for (Product p : products) {
            if (p.getWeight() > MAX_WEIGHT) {
                logService.save(new LogEntry("Product " + p.getSku() + " too heavy"));
                throw new ValidationException("Product exceeds weight limit");
            }
        }
    }
}
