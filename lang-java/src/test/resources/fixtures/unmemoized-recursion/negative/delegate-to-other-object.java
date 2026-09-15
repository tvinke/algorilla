public class JobConfig {
    private JobExecutor jobExecutor;

    public void setWaitTimeInMillis(int millis) {
        jobExecutor.setWaitTimeInMillis(millis);
    }
}
