import java.util.List;
import java.util.ArrayList;

public class DeclarationExamples {
    public void simpleMethod() {
        int count = 0;
        String name = "test";
    }

    public int methodWithParams(String name, int count) {
        return count;
    }

    public void localVariables() {
        List<String> items = new ArrayList<>();
        int x = 1, y = 2;
    }
}
