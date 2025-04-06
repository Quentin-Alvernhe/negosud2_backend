package negosud.backend.negosud.repository;

import negosud.backend.negosud.entity.Article;
import negosud.backend.negosud.entity.PrixHistorique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrixHistoriqueRepository extends JpaRepository<PrixHistorique, Long> {
    PrixHistorique findFirstPrixHistoriqueArticleByArticleOrderByHorodatageCreationDesc(Article article);
}
