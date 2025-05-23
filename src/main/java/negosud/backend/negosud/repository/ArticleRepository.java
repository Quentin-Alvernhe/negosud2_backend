package negosud.backend.negosud.repository;

import negosud.backend.negosud.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {
    List<Article> findByActif(boolean actif);
}