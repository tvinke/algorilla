class NotificationService {
    void notifyAll(List users) {
        users.each(user -> {
            sendEmail(user);
        });
    }

    private void sendEmail(Object user) {
        String subject = "Hello, " + user.name;
        println(subject);
    }
}
