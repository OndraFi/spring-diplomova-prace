package upce.fei.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import upce.fei.attendance.enums.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Long id;
    private String name;
    private String email;
    private String cardId;
    private Role role;
    private String departmentName;
    private Double earnedVacationHours;
    private Double usedVacationHours;
    private String contractType;
}
