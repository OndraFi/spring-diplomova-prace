package upce.fei.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upce.fei.attendance.domain.AttendanceRecord;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
}
