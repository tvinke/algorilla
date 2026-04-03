import java.util.List;

public class GetterChainStringContains {
    static class Reference {
        String getName() { return ""; }
    }

    boolean hasHash(List<Reference> references) {
        for (Reference reference : references) {
            if (reference.getName().contains("#")) {
                return true;
            }
        }
        return false;
    }
}
