public class TernaryBranches {
    // Calls in ternary branches are mutually exclusive — only one executes
    public String resolve(Config config, boolean flag) {
        String value = flag
            ? config.getValue("optionA")
            : config.getValue("optionB");
        return value;
    }
}
