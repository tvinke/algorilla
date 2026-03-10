public class Processor {
    public void process(Config config) {
        String a = config.getValue().transform("option1");
        String b = config.getValue().transform("option2");
    }
}
