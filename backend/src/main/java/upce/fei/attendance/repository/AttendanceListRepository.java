package upce.fei.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upce.fei.attendance.domain.AttendanceList;

public interface AttendanceListRepository extends JpaRepository<AttendanceList, Long> {
}
