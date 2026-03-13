class UserReport {
    fun printNames(userRepository: UserRepository) {
        val users = userRepository.findAll()
        for (user in users) {
            val name = user.getName()
            println(name)
        }
    }
}
