import java.util.*;
import java.util.stream.*;

public class StatisticsService {
    public List<TeamScore> getFilteredScores(List<TeamScore> scores) {
        return scores.stream()
                .sorted(Comparator.comparing(TeamScore::getPoints).reversed())
                .filter(s -> s.getPoints() > 0)
                .collect(Collectors.toList());
    }
}
