package upce.fei.attendance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import upce.fei.attendance.domain.Employee;
import upce.fei.attendance.dto.LoginRequest;
import upce.fei.attendance.repository.EmployeeRepository;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public Employee authenticate(LoginRequest input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return employeeRepository.findByEmail(input.getEmail())
                .orElseThrow();
    }

}
