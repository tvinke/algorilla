class UserReportService {
    void printNames(Object userRepository) {
        List users = userRepository.getAll();
        for (Object user : users) {
            String name = user.getName();
            println(name);
        }
    }
}
