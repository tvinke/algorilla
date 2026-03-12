class Service {
    fun process(config: Config, repo: Repository): String {
        val a = repo.findByName(config.getName())
        val b = repo.findByName(config.getName())
        return "$a $b"
    }
}
