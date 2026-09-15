import java.util.List;
import java.util.ArrayList;

public class ParentChildCopiedCollection {
    List<String> collectEmployeeNames(DeptService service) {
        List<String> names = new ArrayList<>();
        List<Department> departments = service.getDepartments();
        for (Department department : departments) {
            for (Employee employee : department.getEmployees()) {
                names.add(employee.getName());
            }
        }
        return names;
    }
}

class DeptService {
    List<Department> getDepartments() { return null; }
}

class Department {
    List<Employee> getEmployees() { return null; }
}

class Employee {
    String getName() { return null; }
}
