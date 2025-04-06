package negosud.backend.negosud.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import negosud.backend.negosud.RequestValidators.ArticlePost;
import negosud.backend.negosud.RequestValidators.ArticlePut;
import negosud.backend.negosud.View.ArticleCreationView;
import negosud.backend.negosud.View.ArticleView;
import negosud.backend.negosud.entity.Article;
import negosud.backend.negosud.entity.Categorie;
import negosud.backend.negosud.entity.Fournisseur;
import negosud.backend.negosud.entity.PrixHistorique;
import negosud.backend.negosud.repository.ArticleRepository;
import negosud.backend.negosud.repository.CategorieRepository;
import negosud.backend.negosud.repository.FournisseurRepository;
import negosud.backend.negosud.repository.PrixHistoriqueRepository;
import negosud.backend.negosud.security.AppUserDetails;
import negosud.backend.negosud.security.IsEmployeOrUpper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/article")
@Tag(name = "Article", description = "Opérations concernant les articles.")
public class ArticleController {
    private final ArticleRepository articleRepository;
    private final PrixHistoriqueRepository prixHistoriqueRepository;
    private final FournisseurRepository fournisseurRepository;
    private final CategorieRepository categorieRepository;

    @Autowired
    public ArticleController(ArticleRepository articleRepository,
                             PrixHistoriqueRepository prixHistoriqueRepository,
                             FournisseurRepository fournisseurRepository,
                             CategorieRepository categorieRepository) {
        this.articleRepository = articleRepository;
        this.prixHistoriqueRepository = prixHistoriqueRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.categorieRepository = categorieRepository;
    }

    @GetMapping("/all")
    @Operation(
            summary = "Récupérer les articles",
            description = "Récupère la liste de tous les articles."
    )
    @JsonView({ArticleView.class})
    public List<Article> getAllArticle(
            @RequestParam(required = false, value = "actif") Boolean actif,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        String userRole = "ROLE_CLIENT";
        if (user != null) {
            userRole = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_CLIENT");
        }

        if (userRole.equals("ROLE_CLIENT")) {
            return setPrixListeArticle(articleRepository.findByActif(true));
        }
        if (actif != null) {
            return setPrixListeArticle(articleRepository.findByActif(actif));
        } else {
            return setPrixListeArticle(articleRepository.findAll());
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Recupérer un article",
            description = "Récupérer les informations de l'article."
    )
    @JsonView(ArticleView.class)
    public ResponseEntity<Article> getOneArticle(
            @PathVariable UUID id) {

        Optional<Article> foundArticle = articleRepository.findById(id);

        if (foundArticle.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Article article = foundArticle.get();

        return ResponseEntity.ok(setPrixArticle(article));
    }

    @IsEmployeOrUpper
    @PostMapping("/new")
    @Operation(
            summary = "Nouvel article",
            description = "Création d'un nouvel article avec le fournisseur Champagne champomy par défaut."
    )
    @JsonView(ArticleCreationView.class)
    public ResponseEntity<Article> addArticle(
            @Validated(ArticlePost.class)
            @RequestPart("article") @Valid Article nouvelArticle,
            @Nullable @RequestPart("photo") MultipartFile photo
    ) {
        if (nouvelArticle == null || nouvelArticle.getPrix() <= 0 || nouvelArticle.getPrixHistorique() != null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Categorie> foundCategorie = categorieRepository.findById(nouvelArticle.getCategorie().getId());
        if (foundCategorie.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        nouvelArticle.setQuantitePrevisionnelle(nouvelArticle.getQuantite());

        Article article = articleRepository.save(nouvelArticle);

        PrixHistorique prixHistorique = new PrixHistorique();
        prixHistorique.setArticle(article);
        prixHistorique.setPrix(nouvelArticle.getPrix());
        prixHistorique.setTauxTVA(nouvelArticle.getTauxTva());
        prixHistoriqueRepository.save(prixHistorique);

        savePhoto(nouvelArticle, photo);

        return new ResponseEntity<>(setPrixArticle(nouvelArticle), HttpStatus.CREATED);
    }

    @IsEmployeOrUpper
    @PutMapping("/{id}/edit")
    @Operation(
            summary = "Modification d'un article",
            description = "Modification des informations de l'article."
    )
    @JsonView(ArticleView.class)
    public ResponseEntity<Article> updateArticle(
            @PathVariable UUID id,
            @Validated(ArticlePut.class) @RequestPart("article") @Valid Article modifiedArticle,
            @Nullable @RequestPart("photo") MultipartFile photo) {
        if (modifiedArticle == null || modifiedArticle.getPrixHistorique() != null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Article> foundArticle = articleRepository.findById(id);

        if (foundArticle.isEmpty()) {
            return new ResponseEntity<>(modifiedArticle, HttpStatus.NOT_FOUND);
        }

        Article articleDb = foundArticle.get();

        // Si le prix a changé, on crée un nouvel enregistrement dans l'historique des prix en le liant à l'article à modifier
        if (prixHistoriqueRepository.findFirstPrixHistoriqueArticleByArticleOrderByHorodatageCreationDesc(articleDb) == null || modifiedArticle.getPrix() != prixHistoriqueRepository.findFirstPrixHistoriqueArticleByArticleOrderByHorodatageCreationDesc(articleDb).getPrix() && modifiedArticle.getTauxTva() != prixHistoriqueRepository.findFirstPrixHistoriqueArticleByArticleOrderByHorodatageCreationDesc(articleDb).getTauxTVA()) {
            PrixHistorique prixHistorique = new PrixHistorique();
            prixHistorique.setArticle(articleDb);
            prixHistorique.setPrix(modifiedArticle.getPrix());
            prixHistorique.setTauxTVA(modifiedArticle.getTauxTva());
            prixHistoriqueRepository.save(prixHistorique);
        }

        if (modifiedArticle.getQuantite() != articleDb.getQuantite()) {
            modifiedArticle.setQuantitePrevisionnelle(articleDb.getQuantitePrevisionnelle() + (modifiedArticle.getQuantite() - articleDb.getQuantite()));
        } else {
            modifiedArticle.setQuantitePrevisionnelle(articleDb.getQuantitePrevisionnelle());
        }

        modifiedArticle.setId(articleDb.getId());
        modifiedArticle.setPrixHistorique(articleDb.getPrixHistorique());

        if (modifiedArticle.getFournisseur() == null) {
            modifiedArticle.setFournisseur(articleDb.getFournisseur());
        } else {
            Optional<Fournisseur> foundFournisseur = fournisseurRepository.findById(modifiedArticle.getFournisseur().getId());
            if (foundFournisseur.isEmpty()) {
                return new ResponseEntity<>(modifiedArticle, HttpStatus.NOT_FOUND);
            }
            modifiedArticle.setFournisseur(foundFournisseur.get());
        }

        if (modifiedArticle.getCategorie() == null) {
            modifiedArticle.setCategorie(articleDb.getCategorie());
        } else {
            Optional<Categorie> foundCategory = categorieRepository.findById(modifiedArticle.getCategorie().getId());
            if (foundCategory.isEmpty()) {
                return new ResponseEntity<>(modifiedArticle, HttpStatus.NOT_FOUND);
            }
            modifiedArticle.setCategorie(foundCategory.get());
        }

        // Remplacement de la photo d'un article
        editPhoto(articleDb, modifiedArticle, photo);

        articleRepository.save(modifiedArticle);

        return ResponseEntity.ok(setPrixArticle(modifiedArticle));
    }

    @IsEmployeOrUpper
    @PatchMapping("/{id}/{action}")
    @Operation(
            summary = "Désactiver un article",
            description = "Désactive un article en mettant son statut à false au lieu de le supprimer."
    )
    @JsonView(ArticleView.class)
    public ResponseEntity<Article> disableArticle(@PathVariable UUID id,
                                                  @PathVariable String action) {
        if (action.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Article> foundArticle = articleRepository.findById(id);

        if (foundArticle.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Article article = foundArticle.get();
        if (action.equals("disable")) {
            article.setActif(false); // Désactiver l'article
        } else if (action.equals("enable")) {
            article.setActif(true);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        articleRepository.save(article);

        return ResponseEntity.ok(article);
    }

    private List<Article> setPrixListeArticle(List<Article> articles) {
        return articles.stream()
                .map(this::setPrixArticle)
                .collect(Collectors.toList());
    }

    private Article setPrixArticle(Article article) {
        PrixHistorique prixHistorique = prixHistoriqueRepository.findFirstPrixHistoriqueArticleByArticleOrderByHorodatageCreationDesc(article);

        if (prixHistorique == null) {
            article.setPrix(0);
            article.setTauxTva(0);
        } else {
            article.setPrix(prixHistorique.getPrix());
            article.setTauxTva(prixHistorique.getTauxTVA());
        }
        return article;
    }

    public void savePhoto(Article article, MultipartFile photo) {
        if (photo != null) {
            try {
                // Define the path where you want to save the file
                String directoryPath = "C://negosud/images/articles/";
                String fileName = UUID.randomUUID() + ".jpg";
                String filePath = directoryPath + fileName;
                File file = new File(filePath);

                // Create the directory if it doesn't exist
                file.getParentFile().mkdirs();

                // Write the file to the specified path
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(photo.getBytes());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                article.setPhotoReference("/images/articles/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void editPhoto(Article articleDB, Article modifiedArticle, MultipartFile photo) {
        if (!articleDB.getPhotoReference().isEmpty() && articleDB.getPhotoReference() != null) {
            Path filePath = Path.of("C://negosud/images/" + articleDB.getPhotoReference());
            if (photo != null) {
                try {
                    if (Files.exists(filePath)) {
                        if (isSamePhoto(photo, String.valueOf(filePath))) {
                            modifiedArticle.setPhotoReference(articleDB.getPhotoReference());
                            return;
                        } else {
                            deletePhoto(articleDB);
                        }
                    }
                    savePhoto(modifiedArticle, photo);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            } else {
                if (Files.exists(filePath)) {
                    deletePhoto(articleDB);
                    modifiedArticle.setPhotoReference("");
                }
            }
        } else {
            savePhoto(modifiedArticle, photo);
        }
    }

    public void deletePhoto(Article article) {
        File file = new File("C://negosud" + article.getPhotoReference());
        file.delete();
    }

    public boolean isSamePhoto(MultipartFile receivedFile, String existingFilePath) throws IOException, NoSuchAlgorithmException {
        byte[] receivedHash = computeHash(receivedFile.getBytes());
        byte[] existingHash = computeHash(Files.readAllBytes(Path.of(existingFilePath)));

        return Arrays.equals(receivedHash, existingHash);
    }

    private byte[] computeHash(byte[] fileData) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(fileData);
    }

}