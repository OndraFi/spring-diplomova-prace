package upce.fei.attendance.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import upce.fei.attendance.domain.Employee;
import upce.fei.attendance.enums.Role;
import upce.fei.attendance.service.EmployeeService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void getCurrentEmployee_ReturnsOk() throws Exception {
        Employee employee = new Employee();
        employee.setEmail("test@test.cz");
        employee.setRole(Role.EMPLOYEE);

        mockMvc.perform(get("/employees/me")
                        .with(user(employee)))
                .andExpect(status().isOk());
    }

    @Test
    void getCurrentEmployee_UnauthorizedWithoutUser() throws Exception {
        mockMvc.perform(get("/employees/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void searchEmployees_ReturnsOk() throws Exception {
        Employee employee = new Employee();
        employee.setEmail("test@test.cz");
        employee.setRole(Role.EMPLOYEE);

        mockMvc.perform(get("/employees/search")
                        .param("query", "test")
                        .with(user(employee)))
                .andExpect(status().isOk());
    }
}
