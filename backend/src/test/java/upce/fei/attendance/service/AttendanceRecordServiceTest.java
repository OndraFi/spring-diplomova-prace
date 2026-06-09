package upce.fei.attendance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upce.fei.attendance.domain.AttendanceRecord;
import upce.fei.attendance.domain.Employee;
import upce.fei.attendance.repository.AttendanceRecordRepository;
import upce.fei.attendance.repository.EmployeeRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceRecordServiceTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceRecordService attendanceRecordService;

    private Employee employee;
    private final String cardId = "123456789";

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setCardId(cardId);
    }

    @Test
    void clockIn_Successful_CreatesRecord() {
        when(employeeRepository.findByCardId(cardId)).thenReturn(Optional.of(employee));
        when(attendanceRecordRepository.findFirstByEmployeeAndAttendanceEndIsNullOrderByAttendanceStartDesc(employee))
                .thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecord.class))).thenAnswer(i -> i.getArguments()[0]);

        AttendanceRecord result = attendanceRecordService.clockIn(cardId);

        assertNotNull(result);
        assertEquals(employee, result.getEmployee());
        assertNotNull(result.getAttendanceStart());
        assertNull(result.getAttendanceEnd());
        verify(attendanceRecordRepository, times(1)).save(any());
    }

    @Test
    void clockIn_AlreadyClockedIn_ThrowsException() {
        when(employeeRepository.findByCardId(cardId)).thenReturn(Optional.of(employee));
        when(attendanceRecordRepository.findFirstByEmployeeAndAttendanceEndIsNullOrderByAttendanceStartDesc(employee))
                .thenReturn(Optional.of(new AttendanceRecord()));

        assertThrows(IllegalStateException.class, () -> attendanceRecordService.clockIn(cardId));
    }

    @Test
    void clockOut_Successful_UpdatesRecord() {
        AttendanceRecord openRecord = new AttendanceRecord();
        openRecord.setEmployee(employee);
        openRecord.setAttendanceStart(java.time.Instant.now().minusSeconds(3600));

        when(employeeRepository.findByCardId(cardId)).thenReturn(Optional.of(employee));
        when(attendanceRecordRepository.findFirstByEmployeeAndAttendanceEndIsNullOrderByAttendanceStartDesc(employee))
                .thenReturn(Optional.of(openRecord));
        when(attendanceRecordRepository.save(any(AttendanceRecord.class))).thenAnswer(i -> i.getArguments()[0]);

        AttendanceRecord result = attendanceRecordService.clockOut(cardId);

        assertNotNull(result.getAttendanceEnd());
        verify(attendanceRecordRepository, times(1)).save(openRecord);
    }

    @Test
    void clockOut_NoOpenRecord_ThrowsException() {
        when(employeeRepository.findByCardId(cardId)).thenReturn(Optional.of(employee));
        when(attendanceRecordRepository.findFirstByEmployeeAndAttendanceEndIsNullOrderByAttendanceStartDesc(employee))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> attendanceRecordService.clockOut(cardId));
    }
}
