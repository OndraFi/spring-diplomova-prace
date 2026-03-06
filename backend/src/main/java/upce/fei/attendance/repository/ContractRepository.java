package upce.fei.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upce.fei.attendance.domain.Contract;

public interface ContractRepository extends JpaRepository<Contract, Long> {
}
