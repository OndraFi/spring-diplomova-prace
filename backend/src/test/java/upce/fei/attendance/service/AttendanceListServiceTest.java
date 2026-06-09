package upce.fei.attendance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upce.fei.attendance.domain.AttendanceRecord;
import upce.fei.attendance.domain.BreakRecord;
import upce.fei.attendance.domain.Employee;
import upce.fei.attendance.repository.AttendanceRecordRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceListServiceTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @InjectMocks
    private AttendanceListService attendanceListService;

    private Employee employee;
    private Instant start;
    private Instant end;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        start = Instant.parse("2024-05-01T00:00:00Z");
        end = Instant.parse("2024-05-31T23:59:59Z");
    }

    @Test
    void calculateWorkedTime_WithBreaks_ReturnsCorrectDuration() {
        // Attendance from 08:00 to 16:00 (8 hours = 480 minutes)
        Instant workStart = Instant.parse("2024-05-10T08:00:00Z");
        Instant workEnd = Instant.parse("2024-05-10T16:00:00Z");
        
        AttendanceRecord record = new AttendanceRecord();
        record.setAttendanceStart(workStart);
        record.setAttendanceEnd(workEnd);
        record.setBreaks(new ArrayList<>());

        // Break from 12:00 to 12:30 (30 minutes)
        BreakRecord break1 = new BreakRecord();
        break1.setBreakStart(Instant.parse("2024-05-10T12:00:00Z"));
        break1.setBreakEnd(Instant.parse("2024-05-10T12:30:00Z"));
        record.getBreaks().add(break1);

        when(attendanceRecordRepository.findAllByEmployeeAndAttendanceStartBetween(eq(employee), any(), any()))
                .thenReturn(List.of(record));

        Duration result = attendanceListService.calculateWorkedTime(employee, start, end);

        // 480 - 30 = 450 minutes
        assertEquals(450, result.toMinutes());
    }

    @Test
    void calculateWorkedTime_MultipleRecords_ReturnsCorrectDuration() {
        // Day 1: 8h
        AttendanceRecord record1 = new AttendanceRecord();
        record1.setAttendanceStart(Instant.parse("2024-05-10T08:00:00Z"));
        record1.setAttendanceEnd(Instant.parse("2024-05-10T16:00:00Z"));

        // Day 2: 4h
        AttendanceRecord record2 = new AttendanceRecord();
        record2.setAttendanceStart(Instant.parse("2024-05-11T08:00:00Z"));
        record2.setAttendanceEnd(Instant.parse("2024-05-11T12:00:00Z"));

        when(attendanceRecordRepository.findAllByEmployeeAndAttendanceStartBetween(eq(employee), any(), any()))
                .thenReturn(List.of(record1, record2));

        Duration result = attendanceListService.calculateWorkedTime(employee, start, end);

        assertEquals(12 * 60, result.toMinutes());
    }
}
