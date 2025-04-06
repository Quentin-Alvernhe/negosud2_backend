package negosud.backend.negosud.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, UUID> {
    Optional<Employe> findByEmail(@NotNull @NotBlank String email);
    boolean existsByEmail(String email);
}