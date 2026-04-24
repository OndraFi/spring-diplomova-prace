package upce.fei.attendance.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import upce.fei.attendance.domain.Department;
import upce.fei.attendance.domain.Employee;
import upce.fei.attendance.repository.DepartmentRepository;
import upce.fei.attendance.repository.EmployeeRepository;

import java.util.List;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public DataInitializer(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Data init running...");
        if(!departmentRepository.existsById(1L)){
            Department department = new Department("Management");
            departmentRepository.save(department);
            log.info("Creating department {}", department.getName());

            if(!employeeRepository.existsById(1L)){
                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String hash = passwordEncoder.encode("heslo123");
                Employee admin = new Employee("admin","admin@gmail.com",hash);
                admin.setDepartment(department);
                employeeRepository.save(admin);
                log.info("Creating employee {}", admin.getEmail());
            }
        }
    }

}
