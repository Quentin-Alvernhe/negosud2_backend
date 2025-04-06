package negosud.backend.negosud.repository;

import negosud.backend.negosud.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, UUID> {
    Optional<Categorie> findByNom(String nom);
    List<Categorie> findAllByActive(Boolean active);
}