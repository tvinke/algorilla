public class AlgoChecker {
    public void check(Algorithm algo) {
        if (!algo.name().startsWith("RS") && !algo.name().startsWith("PS")) {
            if (algo.name().startsWith("ES")) {
                throw new RuntimeException("unsupported");
            }
        }
    }
}
