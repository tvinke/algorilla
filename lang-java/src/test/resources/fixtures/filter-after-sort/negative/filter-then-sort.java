import java.util.*;
import java.util.stream.*;

public class CorrectOrder {
    public List<TeamScore> getFilteredScores(List<TeamScore> scores) {
        return scores.stream()
                .filter(s -> s.getPoints() > 0)
                .sorted(Comparator.comparing(TeamScore::getPoints).reversed())
                .collect(Collectors.toList());
    }
}
