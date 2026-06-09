package upce.fei.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upce.fei.attendance.domain.AttendanceRecord;
import upce.fei.attendance.domain.Employee;
import upce.fei.attendance.enums.AttendanceType;
import upce.fei.attendance.repository.AttendanceRecordRepository;
import upce.fei.attendance.repository.EmployeeRepository;

import upce.fei.attendance.dto.AttendanceRecordDto;
import upce.fei.attendance.dto.BreakRecordDto;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceRecordService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;

    public List<AttendanceRecordDto> getRecordsForEmployee(Employee employee) {
        return attendanceRecordRepository.findAllByEmployeeOrderByAttendanceStartDesc(employee)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private AttendanceRecordDto convertToDto(AttendanceRecord record) {
        return AttendanceRecordDto.builder()
                .id(record.getId())
                .attendanceStart(record.getAttendanceStart())
                .attendanceEnd(record.getAttendanceEnd())
                .attendanceType(record.getAttendanceType())
                .employeeId(record.getEmployee().getId())
                .breaks(record.getBreaks() != null ? record.getBreaks().stream()
                        .map(b -> BreakRecordDto.builder()
                                .id(b.getId())
                                .breakStart(b.getBreakStart())
                                .breakEnd(b.getBreakEnd())
                                .attendanceRecordId(record.getId())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .build();
    }

    @Transactional
    public AttendanceRecord clockIn(String cardId) {
        Employee employee = employeeRepository.findByCardId(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Employee with card ID " + cardId + " not found"));

        Optional<AttendanceRecord> openRecord = attendanceRecordRepository
                .findFirstByEmployeeAndAttendanceEndIsNullOrderByAttendanceStartDesc(employee);

        if (openRecord.isPresent()) {
            throw new IllegalStateException("Employee already has an open attendance record");
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .attendanceStart(Instant.now())
                .attendanceType(AttendanceType.IN_WORK)
                .build();

        return attendanceRecordRepository.save(record);
    }

    @Transactional
    public AttendanceRecord clockOut(String cardId) {
        Employee employee = employeeRepository.findByCardId(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Employee with card ID " + cardId + " not found"));

        AttendanceRecord record = attendanceRecordRepository
                .findFirstByEmployeeAndAttendanceEndIsNullOrderByAttendanceStartDesc(employee)
                .orElseThrow(() -> new IllegalStateException("No open attendance record found for employee"));

        record.setAttendanceEnd(Instant.now());
        return attendanceRecordRepository.save(record);
    }

    @Transactional
    public AttendanceRecord updateRecord(Long id, AttendanceRecordDto dto) {
        AttendanceRecord record = attendanceRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found with ID: " + id));

        record.setAttendanceStart(dto.getAttendanceStart());
        record.setAttendanceEnd(dto.getAttendanceEnd());
        if (dto.getAttendanceType() != null) {
            record.setAttendanceType(dto.getAttendanceType());
        }

        return attendanceRecordRepository.save(record);
    }

    @Transactional
    public void deleteRecord(Long id) {
        if (!attendanceRecordRepository.existsById(id)) {
            throw new IllegalArgumentException("Record not found with ID: " + id);
        }
        attendanceRecordRepository.deleteById(id);
    }

    @Transactional
    public AttendanceRecord createManualRecord(AttendanceRecordDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + dto.getEmployeeId()));

        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .attendanceStart(dto.getAttendanceStart())
                .attendanceEnd(dto.getAttendanceEnd())
                .attendanceType(dto.getAttendanceType() != null ? dto.getAttendanceType() : AttendanceType.IN_WORK)
                .build();

        return attendanceRecordRepository.save(record);
    }
}
