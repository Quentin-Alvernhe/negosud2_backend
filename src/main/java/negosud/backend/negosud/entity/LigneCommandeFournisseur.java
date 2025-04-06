package negosud.backend.negosud.entity;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.RequestValidators.CommandeFournisseurPost;
import negosud.backend.negosud.View.CommandeFournisseurCreationView;
import negosud.backend.negosud.View.CommandeFournisseurView;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class LigneCommandeFournisseur {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique d'une ligne de commande fournisseur (uuid)", example = "a3a9693a-03d6-4cb9-b9cd-90c6008cd85f")
    @Column(name = "Id_Ligne_Commande_Fournisseur_P_C_A", unique = true, nullable = false, updatable = false, length = 36)
    @JsonView(CommandeFournisseurView.class)
    private UUID id;

    @NotNull(groups = CommandeFournisseurPost.class)
    @Schema(description = "Quantité de l'article lié à une ligne de commande fournisseur", example = "2025-01-01 19:12:54")
    @Column(name = "Quantite_I", nullable = false, length = 255)
    @JsonView({CommandeFournisseurCreationView.class, CommandeFournisseurView.class})
    private Integer quantite;

    @CreationTimestamp
    @Schema(hidden = true)
    @Column(name = "Horodatage_Ajout_Ligne_TT", nullable = false, updatable = false)
    @JsonView({CommandeFournisseurCreationView.class, CommandeFournisseurView.class})
    private LocalDateTime horodatageCreation;

    @ManyToOne
    @JoinColumn(name= "Id_Commande_Fournisseur_F_C", updatable = false)
    @JsonView()
    private CommandeFournisseur commandeFournisseur;

    @Transient
    @JsonView(CommandeFournisseurCreationView.class)
    private Double prixLigne;

    @Transient
    @JsonView(CommandeFournisseurCreationView.class)
    private Double prixUnitaire;

    @ManyToOne
    @JoinColumn(name= "Id_Prix_Historique_F_C", updatable = false)
    private PrixHistorique prixHistorique;

    @Transient
    @NotNull(groups = CommandeFournisseurCreationView.class)
    @JsonView(CommandeFournisseurCreationView.class)
    private Article article;

    // GETTER & SETTER

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public @NotNull(groups = CommandeFournisseurPost.class) Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(@NotNull(groups = CommandeFournisseurPost.class) Integer quantite) {
        this.quantite = quantite;
    }

    public LocalDateTime getHorodatageCreation() {
        return horodatageCreation;
    }

    public void setHorodatageCreation(LocalDateTime horodatageCreation) {
        this.horodatageCreation = horodatageCreation;
    }

    public CommandeFournisseur getCommandeFournisseur() {
        return commandeFournisseur;
    }

    public void setCommandeFournisseur(CommandeFournisseur commandeFournisseur) {
        this.commandeFournisseur = commandeFournisseur;
    }

    public Double getPrixLigne() {
        return prixLigne;
    }

    public void setPrixLigne(Double prixLigne) {
        this.prixLigne = prixLigne;
    }

    public Double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(Double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public PrixHistorique getPrixHistorique() {
        return prixHistorique;
    }

    public void setPrixHistorique(PrixHistorique prixHistorique) {
        this.prixHistorique = prixHistorique;
    }

    public @NotNull(groups = CommandeFournisseurCreationView.class) Article getArticle() {
        return article;
    }

    public void setArticle(@NotNull(groups = CommandeFournisseurCreationView.class) Article article) {
        this.article = article;
    }
}
