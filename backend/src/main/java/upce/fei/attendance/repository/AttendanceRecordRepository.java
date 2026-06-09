package upce.fei.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upce.fei.attendance.domain.AttendanceRecord;
import upce.fei.attendance.domain.Employee;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findFirstByEmployeeAndAttendanceEndIsNullOrderByAttendanceStartDesc(Employee employee);
    List<AttendanceRecord> findAllByEmployeeAndAttendanceStartBetween(Employee employee, Instant start, Instant end);
    List<AttendanceRecord> findAllByEmployeeOrderByAttendanceStartDesc(Employee employee);
}
