package upce.fei.attendance.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import upce.fei.attendance.domain.Department;
import upce.fei.attendance.domain.Employee;
import upce.fei.attendance.enums.Role;
import upce.fei.attendance.service.AttendanceListService;
import upce.fei.attendance.service.EmployeeService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AttendanceListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceListService attendanceListService;

    @MockitoBean
    private EmployeeService employeeService;

    private Department department;
    private Employee manager;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("IT");

        manager = new Employee();
        manager.setEmail("manager@test.cz");
        manager.setRole(Role.MANAGER);
        manager.setDepartment(department);
    }

    @Test
    void getDepartmentAttendanceLists_ReturnsOk() throws Exception {
        mockMvc.perform(get("/attendance-list/department")
                        .with(user(manager)))
                .andExpect(status().isOk());
    }

    @Test
    void getDepartmentAttendanceLists_ForbiddenForEmployee() throws Exception {
        Employee employee = new Employee();
        employee.setEmail("user@test.cz");
        employee.setRole(Role.EMPLOYEE);

        mockMvc.perform(get("/attendance-list/department")
                        .with(user(employee)))
                .andExpect(status().isForbidden());
    }

    @Test
    void generateDepartmentList_ReturnsOk() throws Exception {
        mockMvc.perform(post("/attendance-list/generate")
                        .param("month", "MAY")
                        .param("year", "2024")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(manager)))
                .andExpect(status().isOk());
    }
}
