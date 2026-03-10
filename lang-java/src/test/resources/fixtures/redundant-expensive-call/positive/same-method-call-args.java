public class Service {
    public void process(Config config, Repository repo) {
        String a = repo.findByName(config.getName());
        String b = repo.findByName(config.getName());
    }
}
