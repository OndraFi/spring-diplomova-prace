package upce.fei.attendance.domain;

import jakarta.persistence.*;
import lombok.*;
import upce.fei.attendance.enums.AttendanceType;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "attendance_record")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Instant attendanceStart;
    private Instant attendanceEnd;
    private AttendanceType attendanceType;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @OneToMany(mappedBy = "attendanceRecord", cascade = CascadeType.ALL)
    private List<BreakRecord> breaks;

}
