package negosud.backend.negosud.entity;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.RequestValidators.CommandeClientPost;
import negosud.backend.negosud.RequestValidators.CommandeFournisseurPost;
import negosud.backend.negosud.View.CommandeClientCreationView;
import negosud.backend.negosud.View.CommandeClientView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

@Entity
public class CommandeClient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique de la commande client (uuid)", example = "d3ee2929-212b-4077-af84-694a0e69b8e1")
    @Column(name = "Id_Commande_Client_P_C_A", unique = true, nullable = false, updatable = false, length = 36)
    @JsonView(CommandeClientView.class)
    private UUID id;

    @Schema(description = "Index de la dernière commande passée le jour même", example = "21")
    @Column(name = "Index_Dernier_Numero_Commande_I_A", updatable = false)
    @JsonView()
    private Integer indexLastNumeroCommande;

    @Schema(description = "Index de la commande passée depuis le début du jour", example = "1")
    @Column(name = "Index_Numero_Commande_I_A", nullable = false, updatable = false)
    @JsonView(CommandeClientCreationView.class)
    private Integer indexNumeroCommande;

    @Schema(description = "Numero de la commande (Automatique)", example = "1")
    @Column(name = "Numero_Commande_I_A", nullable = false, updatable = false, unique = true)
    @JsonView(CommandeClientCreationView.class)
    private String numeroCommande;

    @Schema(description = "Horodatage de la création de la commande", example = "2025-01-01 19:12:54")
    @Column(name = "Horodatage_Creation_Commande_TT", nullable = false, updatable = false)
    @JsonView({CommandeClientCreationView.class, CommandeClientView.class})
    private LocalDateTime horodatageCreationCommande;

    @NotNull(groups = CommandeClientPost.class)
    @Schema(description = "Horodatage prévisionel de livraison de la commande", example = "2025-01-03 12:02:00")
    @Column(name = "Horodatage_Prevision_Livraison_TT", nullable = false, length = 255)
    @JsonView(CommandeClientCreationView.class)
    private LocalDateTime horodatagePrevisionLivraison;

    @Schema(description = "Horodatage de la livraison", example = "2025-01-03T12:00:37")
    @Column(name = "Horodatage_Livraison_DT", length = 255)
    @JsonView(CommandeClientView.class)
    private LocalDateTime horodatageLivraison;

    @NotNull()
    @Schema(description = "Statut de la commande", example = "CREE")
    @Column(name = "Statut_Commande_I", nullable = false, length = 255)
    @JsonView(CommandeClientView.class)
    private StatutCommande statutCommande;

    @Transient
    @JsonView(CommandeClientView.class)
    private Double prixTotal;

    @ManyToOne
    @JoinColumn(name= "Id_Client_F_C", updatable = false)
    @JsonView()
    private Client client;

    @ManyToOne
    @JoinColumn(name= "Id_Adresse_Client_Livraison_F_C", updatable = false)
    @JsonView(CommandeClientCreationView.class)
    private Adresse adresseLivraison;

    @ManyToOne
    @JoinColumn(name= "Id_Adresse_Client_Facturation_F_C", updatable = false)
    @JsonView(CommandeClientCreationView.class)
    private Adresse adresseFacturation;

    @OneToMany(mappedBy = "commandeClient", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonView(CommandeClientCreationView.class)
    private List<LigneCommandeClient> lignesCommandeClient;

    // GETTER & SETTER

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getIndexLastNumeroCommande() {
        return indexLastNumeroCommande;
    }

    public void setIndexLastNumeroCommande(Integer indexLastNumeroCommande) {
        this.indexLastNumeroCommande = indexLastNumeroCommande;
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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Adresse getAdresseLivraison() {
        return adresseLivraison;
    }

    public void setAdresseLivraison(Adresse adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    public Adresse getAdresseFacturation() {
        return adresseFacturation;
    }

    public void setAdresseFacturation(Adresse adresseFacturation) {
        this.adresseFacturation = adresseFacturation;
    }

    public List<LigneCommandeClient> getLignesCommandeClient() {
        return lignesCommandeClient;
    }

    public void setLignesCommandeClient(List<LigneCommandeClient> lignesCommandeClient) {
        this.lignesCommandeClient = lignesCommandeClient;
    }

    @PrePersist
    private void prePersist() {

        this.horodatageCreationCommande = LocalDateTime.now();
        if (this.indexLastNumeroCommande != null) {
            this.indexNumeroCommande = this.indexLastNumeroCommande + 1;
        } else {
            this.indexNumeroCommande = 1;
        }
        DateFormat df = new SimpleDateFormat("yy"); // Just the year, with 2 digits
        String formattedDate = df.format(Calendar.getInstance().getTime());
        this.numeroCommande = "CC-" + formattedDate + "-" + String.format("%03d", LocalDate.now().getDayOfYear()) + "-" + String.format("%03d", this.indexNumeroCommande);
    }
}
