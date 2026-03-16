import java.util.*;

public class Validator {
    public List<String> filterValid(List<String> inputs) {
        List<String> result = new ArrayList<>();
        for (String input : inputs) {
            if (input.matches("^[a-zA-Z0-9]+$")) {
                result.add(input);
            }
        }
        return result;
    }

    public List<String> splitInLoop(List<String> lines) {
        List<String> words = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split("\\s+");
            Collections.addAll(words, parts);
        }
        return words;
    }
}
