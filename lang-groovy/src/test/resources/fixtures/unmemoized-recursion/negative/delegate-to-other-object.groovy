class JobConfig {
    JobExecutor jobExecutor

    void setWaitTimeInMillis(int millis) {
        jobExecutor.setWaitTimeInMillis(millis)
    }
}
