package upce.fei.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import upce.fei.attendance.repository.AttendanceListRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceRecordService {

    private final AttendanceListRepository attendanceListRepository;
}
