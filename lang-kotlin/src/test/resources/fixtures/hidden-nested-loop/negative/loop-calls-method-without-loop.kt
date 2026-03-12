class NotificationService {
    fun notifyAll(users: List<User>) {
        for (user in users) {
            sendEmail(user)
        }
    }

    private fun sendEmail(user: User) {
        val subject = "Hello, ${user.name}"
        println(subject)
    }
}
