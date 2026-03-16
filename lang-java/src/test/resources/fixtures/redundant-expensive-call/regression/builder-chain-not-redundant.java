public class QueryBuilder {
    public String buildQuery(String table, String column, String value) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(table)
          .append(" WHERE ").append(column)
          .append(" = '").append(value).append("'");
        return sb.toString();
    }
}
