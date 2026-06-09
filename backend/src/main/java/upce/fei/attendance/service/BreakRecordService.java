package upce.fei.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upce.fei.attendance.domain.AttendanceRecord;
import upce.fei.attendance.domain.BreakRecord;
import upce.fei.attendance.domain.Employee;
import upce.fei.attendance.repository.AttendanceRecordRepository;
import upce.fei.attendance.repository.BreakRecordRepository;
import upce.fei.attendance.repository.EmployeeRepository;

import upce.fei.attendance.dto.BreakRecordDto;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BreakRecordService {

    private final BreakRecordRepository breakRecordRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public BreakRecord updateBreak(Long id, BreakRecordDto dto) {
        BreakRecord breakRecord = breakRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Break record not found with ID: " + id));

        breakRecord.setBreakStart(dto.getBreakStart());
        breakRecord.setBreakEnd(dto.getBreakEnd());

        return breakRecordRepository.save(breakRecord);
    }

    @Transactional
    public void deleteBreak(Long id) {
        if (!breakRecordRepository.existsById(id)) {
            throw new IllegalArgumentException("Break record not found with ID: " + id);
        }
        breakRecordRepository.deleteById(id);
    }

    @Transactional
    public BreakRecord createManualBreak(BreakRecordDto dto) {
        AttendanceRecord attendanceRecord = attendanceRecordRepository.findById(dto.getAttendanceRecordId())
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found with ID: " + dto.getAttendanceRecordId()));

        BreakRecord breakRecord = BreakRecord.builder()
                .attendanceRecord(attendanceRecord)
                .breakStart(dto.getBreakStart())
                .breakEnd(dto.getBreakEnd())
                .build();

        return breakRecordRepository.save(breakRecord);
    }

    @Transactional
    public BreakRecord startBreak(String cardId) {
        Employee employee = employeeRepository.findByCardId(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Employee with card ID " + cardId + " not found"));

        AttendanceRecord attendanceRecord = attendanceRecordRepository
                .findFirstByEmployeeAndAttendanceEndIsNullOrderByAttendanceStartDesc(employee)
                .orElseThrow(() -> new IllegalStateException("No active attendance record found. You must clock in first."));

        Optional<BreakRecord> openBreak = breakRecordRepository
                .findFirstByAttendanceRecordAndBreakEndIsNullOrderByBreakStartDesc(attendanceRecord);

        if (openBreak.isPresent()) {
            throw new IllegalStateException("There is already an active break.");
        }

        BreakRecord breakRecord = BreakRecord.builder()
                .attendanceRecord(attendanceRecord)
                .breakStart(Instant.now())
                .build();

        return breakRecordRepository.save(breakRecord);
    }

    @Transactional
    public BreakRecord endBreak(String cardId) {
        Employee employee = employeeRepository.findByCardId(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Employee with card ID " + cardId + " not found"));

        AttendanceRecord attendanceRecord = attendanceRecordRepository
                .findFirstByEmployeeAndAttendanceEndIsNullOrderByAttendanceStartDesc(employee)
                .orElseThrow(() -> new IllegalStateException("No active attendance record found."));

        BreakRecord breakRecord = breakRecordRepository
                .findFirstByAttendanceRecordAndBreakEndIsNullOrderByBreakStartDesc(attendanceRecord)
                .orElseThrow(() -> new IllegalStateException("No active break found."));

        breakRecord.setBreakEnd(Instant.now());
        return breakRecordRepository.save(breakRecord);
    }
}
