package negosud.backend.negosud.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import negosud.backend.negosud.RequestValidators.FournisseurPost;
import negosud.backend.negosud.View.FournisseurCreationView;
import negosud.backend.negosud.View.FournisseurView;
import negosud.backend.negosud.entity.Fournisseur;
import negosud.backend.negosud.repository.FournisseurRepository;
import negosud.backend.negosud.security.IsEmployeOrUpper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/fournisseur")
@Tag(name = "Fournisseur", description = "Opérations concernant les fournisseur.")
public class FournisseurController {
    private FournisseurRepository fournisseurRepository;

    @Autowired
    public FournisseurController(FournisseurRepository fournisseurRepository) {
        this.fournisseurRepository = fournisseurRepository;
    }

    @IsEmployeOrUpper
    @GetMapping("/all")
    @Operation(
            summary = "Récupérer tous les fournisseurs",
            description = "Récupère la liste de tous les fournisseurs."
    )
    @JsonView(FournisseurView.class)
    public List<Fournisseur> getAllSuppliers(
            @RequestParam(required = false, value="active") Boolean statut
    ) {
        if (statut != null) {
            return fournisseurRepository.findSuppliersByStatut(statut);
        }
        return fournisseurRepository.findAll();
    }

    @IsEmployeOrUpper
    @GetMapping("/{id}")
    @Operation(
            summary = "Recupérer un fournisseur",
            description = "Récupérer les informations du fournisseur."
    )
    @JsonView(FournisseurView.class)
    public ResponseEntity<Fournisseur> getOneSupplier(
            @PathVariable UUID id) {

        Optional<Fournisseur> foundSupplier = fournisseurRepository.findById(id);

        if (foundSupplier.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Fournisseur fournisseur = foundSupplier.get();

        return ResponseEntity.ok(fournisseur);
    }

    @IsEmployeOrUpper
    @PostMapping("/new")
    @Operation(
            summary = "Nouveau fournisseur",
            description = "Création d'un fournisseur."
    )
    public ResponseEntity<Fournisseur> addSupplier(@Validated(FournisseurPost.class) @RequestBody @Valid Fournisseur newFournisseur) {
        if (newFournisseur == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        newFournisseur.setStatut(true);
        fournisseurRepository.save(newFournisseur);
        return new ResponseEntity<>(newFournisseur, HttpStatus.CREATED);
    }

    @IsEmployeOrUpper
    @PatchMapping("/{id}/edit")
    @Operation(
            summary = "Modification d'un fournisseur",
            description = "Modification des informations du fournisseur."
    )
    public ResponseEntity<Fournisseur> updateSupplier(
                                           @PathVariable UUID id,
                                           @RequestBody @Valid Fournisseur modifiedFournisseur) {
        if (modifiedFournisseur == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Fournisseur> foundSupplier = fournisseurRepository.findById(id);

        if (foundSupplier.isEmpty()) {
            return new ResponseEntity<>(modifiedFournisseur, HttpStatus.NOT_FOUND);
        }

        Fournisseur fournisseurDb = foundSupplier.get();
        modifiedFournisseur.setId(fournisseurDb.getId());

        fournisseurRepository.save(modifiedFournisseur);

        return ResponseEntity.ok(modifiedFournisseur);
    }

    @IsEmployeOrUpper
    @PatchMapping("/{id}/disable")
    @Operation(
            summary = "Désactiver un fournisseur",
            description = "Désactive un fournisseur en mettant son statut à false au lieu de le supprimer."
    )

    public ResponseEntity<Fournisseur> disableSupplier(@PathVariable UUID id) {
        Optional<Fournisseur> foundSupplier = fournisseurRepository.findById(id);

        if (foundSupplier.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Fournisseur fournisseur = foundSupplier.get();
        fournisseur.setStatut(false); // Désactiver le fournisseur
        fournisseurRepository.save(fournisseur);

        return ResponseEntity.ok(fournisseur);
    }

}
