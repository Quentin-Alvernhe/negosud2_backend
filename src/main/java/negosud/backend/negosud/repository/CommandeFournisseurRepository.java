package negosud.backend.negosud.repository;

import negosud.backend.negosud.entity.CommandeFournisseur;
import negosud.backend.negosud.entity.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommandeFournisseurRepository extends JpaRepository<CommandeFournisseur, UUID> {
    List<CommandeFournisseur> findByStatutCommande(StatutCommande statut);
    List<CommandeFournisseur> findByHorodatageCreationCommandeAfterOrderByIndexNumeroCommande(LocalDateTime horodatageCreation);
}
