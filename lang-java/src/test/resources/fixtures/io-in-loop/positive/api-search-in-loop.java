class ApiSearchInLoop {
    void searchAll(List<String> queries) {
        for (String query : queries) {
            apiClient.search(query);
        }
    }
}
