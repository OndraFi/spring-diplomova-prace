package upce.fei.attendance.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upce.fei.attendance.domain.AttendanceRecord;
import upce.fei.attendance.domain.BreakRecord;
import upce.fei.attendance.service.AttendanceRecordService;
import upce.fei.attendance.service.BreakRecordService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import upce.fei.attendance.domain.Employee;
import upce.fei.attendance.dto.AttendanceRecordDto;

import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceRecordService attendanceRecordService;
    private final BreakRecordService breakRecordService;
    private final upce.fei.attendance.service.EmployeeService employeeService;

    @GetMapping("/my-attendance")
    public ResponseEntity<List<AttendanceRecordDto>> getMyAttendance(@AuthenticationPrincipal Employee employee) {
        log.info("Fetching attendance for employee: {}", employee.getEmail());
        return ResponseEntity.ok(attendanceRecordService.getRecordsForEmployee(employee));
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<List<AttendanceRecordDto>> getEmployeeAttendance(@PathVariable Long id) {
        log.info("Manager fetching attendance for employee ID: {}", id);
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(attendanceRecordService.getRecordsForEmployee(employee));
    }

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceRecord> clockIn(@RequestParam String cardId) {
        log.info("Clocking in card: {}", cardId);
        return ResponseEntity.ok(attendanceRecordService.clockIn(cardId));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceRecord> clockOut(@RequestParam String cardId) {
        log.info("Clocking out card: {}", cardId);
        return ResponseEntity.ok(attendanceRecordService.clockOut(cardId));
    }

    @PostMapping("/break-start")
    public ResponseEntity<BreakRecord> startBreak(@RequestParam String cardId) {
        log.info("Starting break for card: {}", cardId);
        return ResponseEntity.ok(breakRecordService.startBreak(cardId));
    }

    @PostMapping("/break-end")
    public ResponseEntity<BreakRecord> endBreak(@RequestParam String cardId) {
        log.info("Ending break for card: {}", cardId);
        return ResponseEntity.ok(breakRecordService.endBreak(cardId));
    }
}
