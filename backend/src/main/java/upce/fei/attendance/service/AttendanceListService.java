package upce.fei.attendance.service;

import upce.fei.attendance.repository.AttendanceListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceListService {

    private final AttendanceListRepository attendanceListRepository;
}
