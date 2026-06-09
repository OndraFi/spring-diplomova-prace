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
import upce.fei.attendance.domain.BreakRecord;
import upce.fei.attendance.dto.BreakRecordDto;
import upce.fei.attendance.service.BreakRecordService;

@RestController
@RequestMapping(value = "/break-record")
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
public class BreakRecordController {

    private final BreakRecordService breakRecordService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BreakRecord> createBreak(@RequestBody BreakRecordDto dto) {
        log.info("Manager creating manual break for attendance record ID: {}", dto.getAttendanceRecordId());
        return ResponseEntity.ok(breakRecordService.createManualBreak(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BreakRecord> updateBreak(@PathVariable Long id, @RequestBody BreakRecordDto dto) {
        log.info("Manager updating break record ID: {}", id);
        return ResponseEntity.ok(breakRecordService.updateBreak(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteBreak(@PathVariable Long id) {
        log.info("Manager deleting break record ID: {}", id);
        breakRecordService.deleteBreak(id);
        return ResponseEntity.noContent().build();
    }
}
