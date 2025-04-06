package negosud.backend.negosud.entity;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.RequestValidators.CommandeClientPost;
import negosud.backend.negosud.View.CommandeClientCreationView;
import negosud.backend.negosud.View.CommandeClientView;
import negosud.backend.negosud.View.UtilisateurView;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class LigneCommandeClient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique d'une ligne de commande fournisseur (uuid)", example = "a3a9693a-03d6-4cb9-b9cd-90c6008cd85f")
    @Column(name = "Id_Ligne_Commande_Client_P_C_A", unique = true, nullable = false, updatable = false, length = 36)
    @JsonView(CommandeClientView.class)
    private UUID id;

    @NotNull(groups = CommandeClientPost.class)
    @Schema(description = "Quantité de l'article lié à une ligne de commande fournisseur", example = "2025-01-01 19:12:54")
    @Column(name = "Quantite_I", nullable = false, length = 255)
    @JsonView({CommandeClientCreationView.class, CommandeClientView.class})
    private Integer quantite;

    @CreationTimestamp
    @CreatedDate
    @Schema(hidden = true)
    @Column(name = "Horodatage_Ajout_Ligne_TT", nullable = false, updatable = false)
    @JsonView({CommandeClientCreationView.class, CommandeClientView.class})
    private LocalDateTime horodatageCreation;

    @Transient
    @JsonView(CommandeClientCreationView.class)
    private Double prixLigne;

    @Transient
    @JsonView(CommandeClientCreationView.class)
    private Double prixUnitaire;

    @Transient
    @NotNull(groups = CommandeClientPost.class)
    @JsonView(CommandeClientCreationView.class)
    private Article article;

    @ManyToOne
    @JoinColumn(name= "Id_Prix_Historique_F_C", updatable = false)
    @JsonView()
    private PrixHistorique prixHistorique;

    @ManyToOne
    @JoinColumn(name= "Id_Commande_Client_F_C", updatable = false)
    @JsonView()
    private CommandeClient commandeClient;

    // GETTER & SETTER

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public @NotNull(groups = CommandeClientPost.class) Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(@NotNull(groups = CommandeClientPost.class) Integer quantite) {
        this.quantite = quantite;
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

    public @NotNull(groups = CommandeClientPost.class) Article getArticle() {
        return article;
    }

    public void setArticle(@NotNull(groups = CommandeClientPost.class) Article article) {
        this.article = article;
    }

    public PrixHistorique getPrixHistorique() {
        return prixHistorique;
    }

    public void setPrixHistorique(PrixHistorique prixHistorique) {
        this.prixHistorique = prixHistorique;
    }

    public CommandeClient getCommandeClient() {
        return commandeClient;
    }

    public void setCommandeClient(CommandeClient commandeClient) {
        this.commandeClient = commandeClient;
    }
}
