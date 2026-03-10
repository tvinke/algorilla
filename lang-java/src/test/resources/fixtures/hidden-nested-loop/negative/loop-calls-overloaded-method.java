import java.util.List;
import java.time.LocalDate;

public class DataFetcher {
    // Overloaded method WITH loop — loops over zones and calls the other overload
    public void fetchData(LocalDate date) {
        List<Zone> zones = getZones();
        for (Zone zone : zones) {
            fetchData(zone, date);  // calls the 2-param version, NOT this method
        }
    }

    // Overloaded method WITHOUT loop — does a single fetch
    public void fetchData(Zone zone, LocalDate date) {
        System.out.println("Fetching data for " + zone + " on " + date);
    }

    private List<Zone> getZones() {
        return List.of();
    }
}
