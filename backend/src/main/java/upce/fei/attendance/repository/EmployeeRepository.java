package upce.fei.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upce.fei.attendance.domain.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
