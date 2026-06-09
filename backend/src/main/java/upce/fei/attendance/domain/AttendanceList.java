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

    @Column(nullable = false)
    private Month month;
    
    @Column(nullable = false)
    private Integer year;
    
    private Instant createdAt;
    private Boolean approved;
    
    @Column(columnDefinition = "TEXT")
    private String pdfData;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}