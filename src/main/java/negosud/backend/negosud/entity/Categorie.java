package negosud.backend.negosud.entity;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.View.CategorieView;

import java.util.UUID;

@Entity
@JsonView(CategorieView.class)
public class Categorie {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique de la categorie (uuid)", example = "d3ee3229-312b-4097-af84-695a0e69b8e1")
    @Column(name = "Id_Categorie_P_C_A", unique = true, nullable = false, updatable = false, length = 36)
    private UUID id;

    @NotNull
    @NotBlank
    @Schema(description = "Nom", example = "Rouge")
    @Column(name = "Nom_Categorie_VC", nullable = false, length = 255)
    private String nom;

    @NotNull
    @Schema(description = "Nom", example = "Rouge")
    @Column(name = "Active_B", nullable = false, length = 255)
    private Boolean active;

    // setter getter

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public @NotNull @NotBlank String getNom() {
        return nom;
    }

    public void setNom(@NotNull @NotBlank String nom) {
            this.nom = nom;
    }

    public @NotNull Boolean getActive() {
        return active;
    }

    public void setActive(@NotNull Boolean active) {
        this.active = active;
    }
}
