package upce.fei.attendance.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import upce.fei.attendance.domain.Employee;
import upce.fei.attendance.dto.EmployeeDto;
import upce.fei.attendance.service.EmployeeService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/me")
    public ResponseEntity<EmployeeDto> getCurrentEmployee(@AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(convertToDto(employee));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDto>> searchEmployees(@RequestParam String query) {
        List<Employee> employees = employeeService.searchEmployees(query);
        return ResponseEntity.ok(employees.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
    }

    private EmployeeDto convertToDto(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .cardId(employee.getCardId())
                .role(employee.getRole())
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .earnedVacationHours(employee.getEarnedVacationHours())
                .usedVacationHours(employee.getUsedVacationHours())
                .contractType(employee.getContract() != null ? employee.getContract().getType().name() : null)
                .build();
    }
}
