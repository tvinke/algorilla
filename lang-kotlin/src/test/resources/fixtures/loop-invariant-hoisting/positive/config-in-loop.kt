class RequestProcessor {
    fun processAll(requests: List<Request>, config: Config) {
        for (request in requests) {
            val timeout = config.getTimeout()
            request.execute(timeout)
        }
    }
}
