class ScalarGetter {
    private UserRepository userRepository;

    void processUsers() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            String name = user.getName();
            log(name);
        }
    }

    void log(String msg) {}
}
