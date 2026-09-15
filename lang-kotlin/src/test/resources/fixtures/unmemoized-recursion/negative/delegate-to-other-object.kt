class JobConfig(private val jobExecutor: JobExecutor) {
    fun setWaitTimeInMillis(millis: Int) {
        jobExecutor.setWaitTimeInMillis(millis)
    }
}
