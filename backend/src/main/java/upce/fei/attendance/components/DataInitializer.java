package upce.fei.attendance.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import upce.fei.attendance.domain.*;
import upce.fei.attendance.enums.ContractType;
import upce.fei.attendance.enums.Role;
import upce.fei.attendance.repository.*;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ContractRepository contractRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final BreakRecordRepository breakRecordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Data initialization starting...");

        if (departmentRepository.count() == 0) {
            Department itDept = Department.builder().name("IT").build();
            Department hrDept = Department.builder().name("HR").build();
            departmentRepository.save(itDept);
            departmentRepository.save(hrDept);

            String commonPassword = passwordEncoder.encode("password");

            Employee manager = Employee.builder()
                    .name("Jan Manažer")
                    .email("manager@example.com")
                    .passwordHash(commonPassword)
                    .role(Role.MANAGER)
                    .department(itDept)
                    .cardId("CARD_MANAGER")
                    .earnedVacationHours(0.0)
                    .usedVacationHours(0.0)
                    .build();
            employeeRepository.save(manager);

            itDept.setManager(manager);
            departmentRepository.save(itDept);

            Employee employee = Employee.builder()
                    .name("Petr Zaměstnanec")
                    .email("employee@example.com")
                    .passwordHash(commonPassword)
                    .role(Role.EMPLOYEE)
                    .department(itDept)
                    .cardId("CARD_EMPLOYEE")
                    .earnedVacationHours(0.0)
                    .usedVacationHours(0.0)
                    .build();
            employeeRepository.save(employee);

            // Create Contract for employee
            Contract contract = Contract.builder()
                    .employee(employee)
                    .type(ContractType.HPP_NA_DOBU_NEURCITOU)
                    .startDate(java.sql.Date.valueOf(LocalDate.of(2025, 1, 1)))
                    .build();
            contractRepository.save(contract);
            employee.setContract(contract);
            employeeRepository.save(employee);

            // Generate attendance records for MAY 2026
            log.info("Generating attendance records for May 2026...");
            generateMonthlyData(employee, 2026, Month.MAY);

            log.info("Test data initialized: manager@example.com / password, employee@example.com / password");
        } else {
            log.info("Data already exists, skipping initialization.");
        }
    }

    private void generateMonthlyData(Employee employee, int year, Month month) {
        LocalDate date = LocalDate.of(year, month, 1);
        int daysInMonth = date.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDay = LocalDate.of(year, month, day);
            if (currentDay.getDayOfWeek() != DayOfWeek.SATURDAY && currentDay.getDayOfWeek() != DayOfWeek.SUNDAY) {
                // Work day: 08:00 - 16:30
                Instant start = currentDay.atTime(8, 0).atZone(ZoneId.systemDefault()).toInstant();
                Instant end = currentDay.atTime(16, 30).atZone(ZoneId.systemDefault()).toInstant();

                AttendanceRecord record = AttendanceRecord.builder()
                        .employee(employee)
                        .attendanceStart(start)
                        .attendanceEnd(end)
                        .build();
                attendanceRecordRepository.save(record);

                // Break: 12:00 - 12:30
                Instant breakStart = currentDay.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant();
                Instant breakEnd = currentDay.atTime(12, 30).atZone(ZoneId.systemDefault()).toInstant();

                BreakRecord breakRecord = BreakRecord.builder()
                        .attendanceRecord(record)
                        .breakStart(breakStart)
                        .breakEnd(breakEnd)
                        .build();
                breakRecordRepository.save(breakRecord);
                
                // Set the list to avoid null pointer if accessed before reload
                record.setBreaks(List.of(breakRecord));
            }
        }
    }
}
