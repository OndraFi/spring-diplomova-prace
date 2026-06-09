package upce.fei.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upce.fei.attendance.domain.*;
import upce.fei.attendance.repository.AttendanceListRepository;
import upce.fei.attendance.repository.AttendanceRecordRepository;

import java.time.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceListService {

    private final AttendanceListRepository attendanceListRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final upce.fei.attendance.repository.ContractRepository contractRepository;
    private final upce.fei.attendance.repository.EmployeeRepository employeeRepository;

    public Duration calculateWorkedTime(Employee employee, Instant start, Instant end) {
        List<AttendanceRecord> records = attendanceRecordRepository.findAllByEmployeeAndAttendanceStartBetween(employee, start, end);

        long totalMinutes = 0;
        for (AttendanceRecord record : records) {
            if (record.getAttendanceStart() != null && record.getAttendanceEnd() != null) {
                long workMinutes = Duration.between(record.getAttendanceStart(), record.getAttendanceEnd()).toMinutes();
                
                // Subtract breaks
                long breakMinutes = 0;
                if (record.getBreaks() != null) {
                    for (BreakRecord b : record.getBreaks()) {
                        if (b.getBreakStart() != null && b.getBreakEnd() != null) {
                            breakMinutes += Duration.between(b.getBreakStart(), b.getBreakEnd()).toMinutes();
                        }
                    }
                }
                totalMinutes += (workMinutes - breakMinutes);
            }
        }
        return Duration.ofMinutes(totalMinutes);
    }

    private int countWorkingDays(Month month, int year) {
        int workingDays = 0;
        LocalDate date = LocalDate.of(year, month, 1);
        int daysInMonth = date.lengthOfMonth();
        for (int i = 1; i <= daysInMonth; i++) {
            DayOfWeek dayOfWeek = date.withDayOfMonth(i).getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                workingDays++;
            }
        }
        return workingDays;
    }

    @Transactional
    public AttendanceList generateAttendanceListForDepartment(Department department, Month month, Integer year) {
        Optional<AttendanceList> existing = attendanceListRepository.findByDepartmentAndMonthAndYear(department, month, year);
        if (existing.isPresent() && existing.get().getApproved()) {
            throw new IllegalStateException("Schválenou uzávěrku nelze přegenerovat.");
        }

        List<Employee> employees = employeeRepository.findAllByDepartment(department);
        ZonedDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        int workingDays = countWorkingDays(month, year);
        long standardMinutes = (long) workingDays * 8 * 60;

        String base64Report = "";
        try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            com.lowagie.text.Document document = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(document, out);
            document.open();

            com.lowagie.text.Font titleFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, "cp1250", true, 16);
            com.lowagie.text.Font headerFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, "cp1250", true, 12);
            com.lowagie.text.Font normalFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, "cp1250", true, 11);

            document.add(new com.lowagie.text.Paragraph("DOCHÁZKOVÝ LIST - " + department.getName(), titleFont));
            document.add(new com.lowagie.text.Paragraph("Období: " + month + " " + year, normalFont));
            document.add(new com.lowagie.text.Paragraph(" ")); // Empty line

            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(4);
            table.setWidthPercentage(100);
            
            table.addCell(new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase("Zaměstnanec", headerFont)));
            table.addCell(new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase("Odpracováno", headerFont)));
            table.addCell(new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase("Pauzy", headerFont)));
            table.addCell(new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase("Přesčas", headerFont)));

            for (Employee employee : employees) {
                Duration workedTime = calculateWorkedTime(employee, startOfMonth.toInstant(), endOfMonth.toInstant());
                long overtimeMinutes = Math.max(0, workedTime.toMinutes() - standardMinutes);
                
                long totalBreakMinutes = 0;
                List<AttendanceRecord> records = attendanceRecordRepository.findAllByEmployeeAndAttendanceStartBetween(employee, startOfMonth.toInstant(), endOfMonth.toInstant());
                for (AttendanceRecord record : records) {
                    if (record.getBreaks() != null) {
                        for (BreakRecord b : record.getBreaks()) {
                            if (b.getBreakStart() != null && b.getBreakEnd() != null) {
                                totalBreakMinutes += Duration.between(b.getBreakStart(), b.getBreakEnd()).toMinutes();
                            }
                        }
                    }
                }

                table.addCell(new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(employee.getName(), normalFont)));
                table.addCell(new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(formatDuration(workedTime), normalFont)));
                table.addCell(new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(totalBreakMinutes + "m", normalFont)));
                table.addCell(new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(formatDuration(Duration.ofMinutes(overtimeMinutes)), normalFont)));
            }

            document.add(table);
            document.close();

            base64Report = java.util.Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            log.error("Failed to generate PDF report", e);
            throw new RuntimeException("Chyba při generování PDF dokumentu.");
        }

        AttendanceList attendanceList = existing.orElse(new AttendanceList());
        attendanceList.setDepartment(department);
        attendanceList.setMonth(month);
        attendanceList.setYear(year);
        attendanceList.setCreatedAt(Instant.now());
        attendanceList.setApproved(false);
        attendanceList.setPdfData(base64Report);

        return attendanceListRepository.save(attendanceList);
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%dh %dm", hours, minutes);
    }

    private void updateVacationAccrual(Employee employee, Duration monthlyWorkedTime) {
        upce.fei.attendance.domain.Contract contract = employee.getContract();
        if (contract == null) return;

        double weeklyStandard = 40.0;
        double annualAllowance = 160.0; // 20 days

        if (contract.getType() == upce.fei.attendance.enums.ContractType.DPP || 
            contract.getType() == upce.fei.attendance.enums.ContractType.DPC) {
            weeklyStandard = 20.0;
        }

        double workedHours = monthlyWorkedTime.toMinutes() / 60.0;
        double accrual = (workedHours / weeklyStandard) * (annualAllowance / 52.0);

        double currentEarned = employee.getEarnedVacationHours() != null ? employee.getEarnedVacationHours() : 0.0;
        employee.setEarnedVacationHours(currentEarned + accrual);
        employeeRepository.save(employee);
    }

    @Transactional
    public AttendanceList approveAttendanceList(Long id) {
        AttendanceList list = attendanceListRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attendance list not found"));
        
        if (list.getApproved()) {
            return list;
        }

        // Calculate and update vacation only on approval
        List<Employee> employees = employeeRepository.findAllByDepartment(list.getDepartment());
        ZonedDateTime startOfMonth = LocalDate.of(list.getYear(), list.getMonth(), 1).atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        for (Employee employee : employees) {
            Duration workedTime = calculateWorkedTime(employee, startOfMonth.toInstant(), endOfMonth.toInstant());
            updateVacationAccrual(employee, workedTime);
        }

        list.setApproved(true);
        return attendanceListRepository.save(list);
    }

    public List<AttendanceList> getAttendanceListsForDepartment(Department department) {
        return attendanceListRepository.findByDepartmentOrderByYearDescMonthDesc(department);
    }
}
