package upce.fei.attendance.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.Month;

@Entity
@Table(name = "attendance_list")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Month month;
    private Instant createdAt;
    private Boolean approved;
    private String pdfData;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
}