import java.util.List;
import java.util.ArrayList;

public class ParentChildIteration {
    List<String> collectEmployeeNames(List<Department> departments) {
        List<String> names = new ArrayList<>();
        for (Department department : departments) {
            for (Employee emp : department.getEmployees()) {
                names.add(emp.getName());
            }
        }
        return names;
    }
}

class Department {
    List<Employee> getEmployees() { return null; }
}

class Employee {
    String getName() { return null; }
}
