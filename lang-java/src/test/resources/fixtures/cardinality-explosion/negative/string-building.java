import java.util.List;

class StringBuilding {
    String buildMatrix(List<String> rows, List<String> cols) {
        StringBuilder sb = new StringBuilder();
        for (String row : rows) {
            for (String col : cols) {
                sb.append(row).append(":").append(col).append(",");
            }
        }
        return sb.toString();
    }
}
