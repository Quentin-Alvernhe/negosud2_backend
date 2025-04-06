package negosud.backend.negosud.repository;

import negosud.backend.negosud.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.UUID;

@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, UUID> {
    List<Fournisseur> findSuppliersByStatut(boolean statut);
}