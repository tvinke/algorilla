package com.example;

import java.util.List;

public class RetryWithReturn {
    private RestTemplate restTemplate;

    // Retry pattern: loop over services, return on first success, catch and retry on failure.
    // The IO call is inside a try block followed by return — the loop runs at most
    // one successful iteration. This is NOT io-in-loop.
    public Object execute(List<Service> services, String path) {
        for (Service service : services) {
            try {
                Object result = restTemplate.exchange(service.getUrl() + path);
                return result;
            } catch (Exception e) {
                if (!canRetry(e)) {
                    throw e;
                }
            }
        }
        throw new RuntimeException("All services failed");
    }
}
