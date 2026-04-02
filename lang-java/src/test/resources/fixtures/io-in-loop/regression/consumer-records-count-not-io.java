class ConsumerRecordsCountNotIo {
    static class ConsumerRecords<K, V> {
        boolean isEmpty() { return false; }
        int count() { return 1; }
    }

    static class Consumer {
        ConsumerRecords<String, String> poll() { return new ConsumerRecords<>(); }
    }

    void consume(Consumer consumer, boolean running) {
        while (running) {
            ConsumerRecords<String, String> records = consumer.poll();
            if (records != null && !records.isEmpty()) {
                System.out.println(records.count());
            }
            running = false;
        }
    }
}
