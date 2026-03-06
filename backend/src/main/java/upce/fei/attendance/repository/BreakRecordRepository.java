package upce.fei.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upce.fei.attendance.domain.BreakRecord;

public interface BreakRecordRepository extends JpaRepository<BreakRecord, Long> {
}
