package upce.fei.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upce.fei.attendance.domain.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
