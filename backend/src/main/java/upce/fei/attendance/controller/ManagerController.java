package upce.fei.attendance.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import upce.fei.attendance.domain.AttendanceList;
import upce.fei.attendance.service.AttendanceListService;

import java.time.Month;

@RestController
@RequestMapping("/manager")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerController {

    private final AttendanceListService attendanceListService;

    @PostMapping("/attendance-list/approve/{id}")
    public ResponseEntity<AttendanceList> approveAttendanceList(@PathVariable Long id) {
        log.info("Approving attendance list ID: {}", id);
        return ResponseEntity.ok(attendanceListService.approveAttendanceList(id));
    }

    // Additional endpoints for manager actions can be added here
}
