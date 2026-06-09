package upce.fei.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import upce.fei.attendance.enums.AttendanceType;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordDto {
    private Long id;
    private Instant attendanceStart;
    private Instant attendanceEnd;
    private AttendanceType attendanceType;
    private Long employeeId;
    private List<BreakRecordDto> breaks;
}
