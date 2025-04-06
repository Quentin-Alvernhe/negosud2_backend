package negosud.backend.negosud.repository;

import negosud.backend.negosud.entity.Client;
import negosud.backend.negosud.entity.CommandeClient;
import negosud.backend.negosud.entity.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommandeClientRepository extends JpaRepository<CommandeClient, UUID> {
    List<CommandeClient> findAllByClient(Client client);
    List<CommandeClient> findByStatutCommande(StatutCommande statut);
    List<CommandeClient> findAllByClientAndStatutCommande(Client client, StatutCommande statut);
    List<CommandeClient> findByHorodatageCreationCommandeAfterOrderByIndexNumeroCommande(LocalDateTime horodatageCreation);
}
