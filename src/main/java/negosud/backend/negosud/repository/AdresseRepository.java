package negosud.backend.negosud.repository;

import negosud.backend.negosud.entity.Adresse;
import negosud.backend.negosud.entity.Client;
import negosud.backend.negosud.entity.StatutAdresse;
import negosud.backend.negosud.entity.StatutUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdresseRepository extends JpaRepository<Adresse, UUID> {
    List<Adresse> findByClientAndStatutEquals(Client client, StatutAdresse statut);
    List<Adresse> findByClientAndStatutEqualsOrStatutEquals(Client client, StatutAdresse statut, StatutAdresse statut2);
    List<Adresse> findByClient(Client client);
    List<Adresse> findByClientAndId(Client client, UUID id);
}
