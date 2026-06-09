package upce.fei.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakRecordDto {
    private Long id;
    private Instant breakStart;
    private Instant breakEnd;
    private Long attendanceRecordId;
}
