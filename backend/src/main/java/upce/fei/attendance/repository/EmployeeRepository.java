package upce.fei.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upce.fei.attendance.domain.Department;
import upce.fei.attendance.domain.Employee;
import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByCardId(String cardId);
    List<Employee> findAllByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);

    List<Employee> findAllByDepartment(Department department);
}
