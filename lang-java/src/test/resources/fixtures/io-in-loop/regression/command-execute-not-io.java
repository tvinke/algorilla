public class CommandRunner {
    // Command.execute() is in-memory dispatch, not IO
    public void runAll(List<Command> commands) {
        for (Command cmd : commands) {
            cmd.execute();
        }
    }

    // ExecutorService.execute() is thread pool dispatch, not IO
    public void submitTasks(ExecutorService executor, List<Runnable> tasks) {
        for (Runnable task : tasks) {
            executor.execute(task);
        }
    }

    // Future.cancel() is an in-memory flag, not IO
    public void cancelAll(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            future.cancel(true);
        }
    }
}
