package upce.fei.attendance.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/attendance-list")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or expired token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Not allowed - insufficient role",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Attendance list not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class AttendanceListController {

    private final upce.fei.attendance.service.AttendanceListService attendanceListService;
    private final upce.fei.attendance.service.EmployeeService employeeService;

    @GetMapping("/department")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('MANAGER')")
    public org.springframework.http.ResponseEntity<java.util.List<upce.fei.attendance.domain.AttendanceList>> getDepartmentAttendanceLists(
            @org.springframework.security.core.annotation.AuthenticationPrincipal upce.fei.attendance.domain.Employee manager) {
        
        if (manager.getDepartment() == null) {
            return org.springframework.http.ResponseEntity.badRequest().build();
        }
        
        log.info("Manager {} fetching attendance lists for department: {}", manager.getEmail(), manager.getDepartment().getName());
        return org.springframework.http.ResponseEntity.ok(attendanceListService.getAttendanceListsForDepartment(manager.getDepartment()));
    }

    @PostMapping("/generate")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('MANAGER')")
    public org.springframework.http.ResponseEntity<upce.fei.attendance.domain.AttendanceList> generateDepartmentList(
            @org.springframework.security.core.annotation.AuthenticationPrincipal upce.fei.attendance.domain.Employee manager,
            @RequestParam java.time.Month month,
            @RequestParam Integer year) {
        
        if (manager.getDepartment() == null) {
            return org.springframework.http.ResponseEntity.badRequest().build();
        }

        log.info("Manager {} generating attendance list for department: {} for {}/{}", 
                manager.getEmail(), manager.getDepartment().getName(), month, year);
                
        return org.springframework.http.ResponseEntity.ok(
                attendanceListService.generateAttendanceListForDepartment(manager.getDepartment(), month, year));
    }

    @PutMapping("/{id}/approve")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('MANAGER')")
    public org.springframework.http.ResponseEntity<upce.fei.attendance.domain.AttendanceList> approveList(@PathVariable Long id) {
        log.info("Manager approving attendance list ID: {}", id);
        return org.springframework.http.ResponseEntity.ok(attendanceListService.approveAttendanceList(id));
    }
}
