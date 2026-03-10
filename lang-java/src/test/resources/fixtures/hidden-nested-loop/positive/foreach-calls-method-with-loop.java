import java.util.List;

public class ReportGenerator {
    public void generateReport(List<Department> departments) {
        departments.forEach(dept -> summarize(dept));
    }

    private void summarize(Department dept) {
        for (Employee emp : dept.getEmployees()) {
            calculateBonus(emp);
        }
    }

    private void calculateBonus(Employee emp) {
        // no loop
    }
}
