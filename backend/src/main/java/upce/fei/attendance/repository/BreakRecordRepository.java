package upce.fei.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upce.fei.attendance.domain.AttendanceRecord;
import upce.fei.attendance.domain.BreakRecord;

import java.util.Optional;

public interface BreakRecordRepository extends JpaRepository<BreakRecord, Long> {
    Optional<BreakRecord> findFirstByAttendanceRecordAndBreakEndIsNullOrderByBreakStartDesc(AttendanceRecord attendanceRecord);
}
