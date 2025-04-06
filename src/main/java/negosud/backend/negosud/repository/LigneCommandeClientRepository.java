package negosud.backend.negosud.repository;

import negosud.backend.negosud.entity.LigneCommandeClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LigneCommandeClientRepository extends JpaRepository<LigneCommandeClient, UUID> {
}
