package negosud.backend.negosud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import negosud.backend.negosud.entity.Categorie;
import negosud.backend.negosud.repository.CategorieRepository;
import negosud.backend.negosud.security.AppUserDetails;
import negosud.backend.negosud.security.IsAdministrateur;
import negosud.backend.negosud.security.IsEmployeOrUpper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/categorie")
@Tag(name = "Catégories", description = "Opérations concernant les catégories.")
public class CategorieController {
    private CategorieRepository categorieRepository;

    @Autowired
    public CategorieController(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    @GetMapping("/all")
    @Operation(
            summary = "Récupérer toutes les catégories",
            description = "Récupère la liste de toutes les catégories."
    )
    public List<Categorie> getAllCategories(
            @RequestParam(required = false, value="active") Boolean active,
            @AuthenticationPrincipal AppUserDetails user
    ) {String userRole = "ROLE_CLIENT";
        if (user != null) {
            userRole = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_CLIENT");
        }

        if (userRole.equals("ROLE_CLIENT")) {
            return categorieRepository.findAllByActive(true);
        }
        if (active != null) {
            return categorieRepository.findAllByActive(active);
        } else {
            return categorieRepository.findAll();
        }
    }

    @IsEmployeOrUpper
    @GetMapping("/{id}")
    @Operation(
            summary = "Recupérer une categorie",
            description = "Récupérer les informations de la catégorie."
    )
    public ResponseEntity<Categorie> getOneCategorie(
            @PathVariable UUID id) {

        Optional<Categorie> foundCategorie = categorieRepository.findById(id);

        if (foundCategorie.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Categorie categorie = foundCategorie.get();

        return ResponseEntity.ok(categorie);
    }

    @IsEmployeOrUpper
    @PostMapping("/new")
    @Operation(
            summary = "Nouvelle catégories",
            description = "Création d'une catégorie."
    )
    public ResponseEntity<Categorie> addCategorie(@RequestBody @Valid Categorie newCategorie) {
        if (newCategorie == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<Categorie> existingCategorie = categorieRepository.findByNom(newCategorie.getNom());

        if (existingCategorie.isPresent()) {
            newCategorie.setNom(newCategorie.getNom() + " est un duplicata.");
            return new ResponseEntity<>(newCategorie, HttpStatus.CONFLICT);
        }

        categorieRepository.save(newCategorie);
        return new ResponseEntity<>(newCategorie, HttpStatus.CREATED);
    }

    @IsEmployeOrUpper
    @PutMapping("/{id}/edit")
    @Operation(
            summary = "Modification d'une catégorie",
            description = "Modification des informations de la catégorie."
    )
    public ResponseEntity<Categorie> updateCategorie(
            @PathVariable UUID id,
            @RequestBody @Valid Categorie modifiedCategorie) {
        if (modifiedCategorie == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Categorie> foundCategorie = categorieRepository.findById(id);

        if (foundCategorie.isEmpty()) {
            return new ResponseEntity<>(modifiedCategorie, HttpStatus.NOT_FOUND);
        }

        Optional<Categorie> existingCategorie = categorieRepository.findByNom(modifiedCategorie.getNom());

        if (existingCategorie.isPresent() && !existingCategorie.get().getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        Categorie categorieDb = foundCategorie.get();
        modifiedCategorie.setId(categorieDb.getId());
        modifiedCategorie.setActive(categorieDb.getActive());

        categorieRepository.save(modifiedCategorie);

        return ResponseEntity.ok(modifiedCategorie);
    }

    @IsAdministrateur
    @PatchMapping("/{id}/{action}")
    @Operation(
            summary = "Désactivation ou activation d'une catégorie",
            description = "Désactivation ou activation d'une catégorie."
    )
    public ResponseEntity<Categorie> updateCategorie(
            @PathVariable UUID id,
            @PathVariable String action) {
        if (action == null || action.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Categorie> foundCategorie = categorieRepository.findById(id);

        if (foundCategorie.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Categorie categorieDb = foundCategorie.get();

        if (action.equals("enable")) {
            categorieDb.setActive(true);
        } else if (action.equals("disable")) {
            categorieDb.setActive(false);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        categorieRepository.save(categorieDb);

        return ResponseEntity.ok(categorieDb);
    }

}