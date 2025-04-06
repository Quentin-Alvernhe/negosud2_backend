package negosud.backend.negosud.entity;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.RequestValidators.CommandeFournisseurPost;
import negosud.backend.negosud.View.CommandeFournisseurCreationView;
import negosud.backend.negosud.View.CommandeFournisseurView;
import org.hibernate.annotations.CreationTimestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

@Entity
public class CommandeFournisseur {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique de la commande fournisseur (uuid)", example = "d3ee2929-212b-4077-af84-694a0e69b8e1")
    @Column(name = "Id_Commande_Fournisseur_P_C_A", unique = true, nullable = false, updatable = false, length = 36)
    @JsonView(CommandeFournisseurView.class)
    private UUID id;

    @Schema(description = "Index de la dernière commande passé le jour même", example = "21")
    @Column(name = "Index_Dernier_Numero_Commande_I_A", updatable = false)
    @JsonView()
    private Integer indexLastNumeroCommande;

    @Schema(description = "Index de la commande passé depuis le début du jour", example = "1")
    @Column(name = "Index_Numero_Commande_I_A", nullable = false, updatable = false)
    @JsonView(CommandeFournisseurCreationView.class)
    private Integer indexNumeroCommande;

    @Schema(description = "Numero de la commande (Automatique)", example = "1")
    @Column(name = "Numero_Commande_T_A", nullable = false, updatable = false, unique = true)
    @JsonView(CommandeFournisseurCreationView.class)
    private String numeroCommande;

    @CreationTimestamp
    @Schema(description = "Horodatage de la création de la commande", example = "2025-01-01 19:12:54")
    @Column(name = "Horodatage_Creation_Commande_TT", nullable = false, updatable = false)
    @JsonView({CommandeFournisseurCreationView.class, CommandeFournisseurView.class})
    private LocalDateTime horodatageCreationCommande;

    @NotNull(groups = CommandeFournisseurPost.class)
    @Schema(description = "Horodatage prévisionel de livraison de la commande", example = "2025-01-03 12:02:00")
    @Column(name = "Horodatage_Prevision_Livraison_TT", nullable = false, length = 255)
    @JsonView(CommandeFournisseurCreationView.class)
    private LocalDateTime horodatagePrevisionLivraison;

    @Schema(description = "Horodatage de la livraison", example = "2025-01-03T12:00:37")
    @Column(name = "Horodatage_Livraison_DT", length = 255)
    @JsonView(CommandeFournisseurView.class)
    private LocalDateTime horodatageLivraison;

    @Schema(description = "Commentaire", example = "1 bouteille manquante")
    @Column(name = "Commentaire_VC", nullable = false, length = 255)
    @JsonView(CommandeFournisseurCreationView.class)
    private String commentaire;

    @NotNull()
    @Schema(description = "Statut de la commande", example = "CREE")
    @Column(name = "Statut_Commande_I", nullable = false, length = 255)
    @JsonView(CommandeFournisseurView.class)
    private StatutCommande statutCommande;

    @Transient
    @JsonView(CommandeFournisseurCreationView.class)
    private Double prixTotal;

    @ManyToOne
    @JoinColumn(name= "Id_Employe_F_C", updatable = false)
    @JsonView(CommandeFournisseurCreationView.class)
    private Employe employe;

    @ManyToOne
    @JoinColumn(name= "Id_Fournisseur_F_C", updatable = false)
    @JsonView(CommandeFournisseurCreationView.class)
    private Fournisseur fournisseur;

    @OneToMany(mappedBy = "commandeFournisseur", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonView(CommandeFournisseurCreationView.class)
    private List<LigneCommandeFournisseur> lignesCommandeFournisseur;

    // GETTER & SETTER

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getIndexNumeroCommande() {
        return indexNumeroCommande;
    }

    public void setIndexNumeroCommande(Integer indexNumeroCommande) {
        this.indexNumeroCommande = indexNumeroCommande;
    }

    public String getNumeroCommande() {
        return numeroCommande;
    }

    public void setNumeroCommande(String numeroCommande) {
        this.numeroCommande = numeroCommande;
    }

    public Integer getIndexLastNumeroCommande() {
        return indexLastNumeroCommande;
    }

    public void setIndexLastNumeroCommande(Integer indexLastNumeroCommande) {
        this.indexLastNumeroCommande = indexLastNumeroCommande;
    }

    public LocalDateTime getHorodatageCreationCommande() {
        return horodatageCreationCommande;
    }

    public void setHorodatageCreationCommande(LocalDateTime horodatageCreationCommande) {
        this.horodatageCreationCommande = horodatageCreationCommande;
    }

    public @NotNull(groups = CommandeFournisseurPost.class) LocalDateTime getHorodatagePrevisionLivraison() {
        return horodatagePrevisionLivraison;
    }

    public void setHorodatagePrevisionLivraison(@NotNull(groups = CommandeFournisseurPost.class) LocalDateTime horodatagePrevisionLivraison) {
        this.horodatagePrevisionLivraison = horodatagePrevisionLivraison;
    }

    public LocalDateTime getHorodatageLivraison() {
        return horodatageLivraison;
    }

    public void setHorodatageLivraison(LocalDateTime horodatageLivraison) {
        this.horodatageLivraison = horodatageLivraison;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public @NotNull() StatutCommande getStatutCommande() {
        return statutCommande;
    }

    public void setStatutCommande(@NotNull() StatutCommande statutCommande) {
        this.statutCommande = statutCommande;
    }

    public Double getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(Double prixTotal) {
        this.prixTotal = prixTotal;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public List<LigneCommandeFournisseur> getLignesCommandeFournisseur() {
        return lignesCommandeFournisseur;
    }

    public void setLignesCommandeFournisseur(List<LigneCommandeFournisseur> lignesCommandeFournisseur) {
        this.lignesCommandeFournisseur = lignesCommandeFournisseur;
    }

    @PrePersist
    private void prePersist() {
        if (this.indexLastNumeroCommande != null) {
            this.indexNumeroCommande = this.indexLastNumeroCommande + 1;
        } else {
            this.indexNumeroCommande = 1;
        }
        DateFormat df = new SimpleDateFormat("yy"); // Just the year, with 2 digits
        String formattedDate = df.format(Calendar.getInstance().getTime());
        this.numeroCommande = "CF-" + formattedDate + "-" +
                this.fournisseur.getNom().substring(0, Math.min(this.fournisseur.getNom().length(), 3)).toUpperCase() + "-" +
                String.format("%03d", LocalDate.now().getDayOfYear()) + "-" + String.format("%03d", this.indexNumeroCommande);
    }
}