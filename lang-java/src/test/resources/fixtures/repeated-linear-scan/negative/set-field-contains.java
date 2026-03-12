import java.util.Set;

public class SetFieldContains {
    private Set<String> ipList;

    public boolean isAllowed(String clientIp) {
        return ipList.contains("*") || ipList.contains(clientIp);
    }
}
