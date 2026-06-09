package upce.fei.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upce.fei.attendance.domain.AttendanceList;
import upce.fei.attendance.domain.Employee;

import java.time.Month;
import java.util.Optional;

public interface AttendanceListRepository extends JpaRepository<AttendanceList, Long> {
    Optional<AttendanceList> findByDepartmentAndMonthAndYear(upce.fei.attendance.domain.Department department, java.time.Month month, Integer year);
    java.util.List<AttendanceList> findByDepartmentOrderByYearDescMonthDesc(upce.fei.attendance.domain.Department department);
}
