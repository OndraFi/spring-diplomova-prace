package upce.fei.attendance.domain;

import jakarta.persistence.*;
import lombok.*;
import upce.fei.attendance.enums.ContractType;

import java.util.Date;

@Entity
@Table(name = "contract")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private ContractType type;

    private Date startDate;

    private Date endDate;

    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
}