import java.util.List;

public class LoopExamples {
    public void basicForLoop() {
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
        }
    }

    public void enhancedForLoop(List<String> items) {
        for (String item : items) {
            System.out.println(item);
        }
    }

    public void whileLoop() {
        int i = 0;
        while (i < 10) {
            i++;
        }
    }

    public void doWhileLoop() {
        int i = 0;
        do {
            i++;
        } while (i < 10);
    }

    public void forEachStream(List<String> items) {
        items.stream().forEach(item -> {
            System.out.println(item);
        });
    }

    public void forEachHigherOrder(List<String> items) {
        items.forEach(item -> {
            System.out.println(item);
        });
    }
}
