package upce.fei.attendance.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import upce.fei.attendance.domain.AttendanceRecord;
import upce.fei.attendance.dto.AttendanceRecordDto;
import upce.fei.attendance.service.AttendanceRecordService;

@RestController
@RequestMapping(value = "/attendance-record")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or expired token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Not allowed - insufficient role",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Record not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class AttendanceRecordController {

    private final AttendanceRecordService attendanceRecordService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AttendanceRecord> createRecord(@RequestBody AttendanceRecordDto dto) {
        log.info("Manager creating manual attendance record for employee ID: {}", dto.getEmployeeId());
        return ResponseEntity.ok(attendanceRecordService.createManualRecord(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AttendanceRecord> updateRecord(@PathVariable Long id, @RequestBody AttendanceRecordDto dto) {
        log.info("Manager updating attendance record ID: {}", id);
        return ResponseEntity.ok(attendanceRecordService.updateRecord(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        log.info("Manager deleting attendance record ID: {}", id);
        attendanceRecordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }
}
