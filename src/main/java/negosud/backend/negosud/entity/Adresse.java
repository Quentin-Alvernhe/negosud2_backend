package negosud.backend.negosud.entity;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.RequestValidators.AdressePost;
import negosud.backend.negosud.RequestValidators.AdressePut;
import negosud.backend.negosud.View.AdresseListeView;
import negosud.backend.negosud.View.AdresseView;
import negosud.backend.negosud.View.CommandeClientCreationView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
public class Adresse {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique de l'article (uuid)", example = "d3ee2929-212b-4077-af84-694a0e69b8e1")
    @Column(name = "Id_Adresse_P_C_A", unique = true, nullable = false, updatable = false, length = 36)
    @JsonView({AdresseListeView.class, CommandeClientCreationView.class})
    private UUID id;

    @NotNull(groups = {AdressePost.class, AdressePut.class})
    @Schema(description = "Code de la commune", example = "54395")
    @Column(name = "Code_Commune_N", nullable = false)
    @JsonView(AdresseListeView.class)
    private String codeCommune;

    @NotNull(groups = {AdressePost.class, AdressePut.class})
    @Schema(description = "Code postal", example = "5400 et 54100")
    @Column(name = "Code_Postal_N", nullable = false)
    @JsonView(AdresseListeView.class)
    private String codePostal;

    @NotBlank(groups = {AdressePost.class, AdressePut.class})
    @NotNull(groups = {AdressePost.class, AdressePut.class})
    @Schema(description = "Nom de la ville", example = "Nancy")
    @Column(name = "Nom_Ville_VC", nullable = false, length = 255)
    @JsonView(AdresseListeView.class)
    private String nomVille;

    @NotBlank(groups = {AdressePost.class, AdressePut.class})
    @NotNull(groups = {AdressePost.class, AdressePut.class})
    @Schema(description = "Nom de la rue", example = "Rue des chamaux")
    @Column(name = "Nom_Rue_VC", nullable = false, length = 255)
    @JsonView(AdresseListeView.class)
    private String nomRue;

    @NotNull(groups = {AdressePost.class, AdressePut.class})
    @NotBlank(groups = {AdressePost.class, AdressePut.class})
    @Schema(description = "Numero du bâtiment", example = "69")
    @Column(name = "Numero_Batiment_VC", nullable = false, length = 255)
    @JsonView(AdresseListeView.class)
    private String numeroBatiment;

    @NotNull(groups = {AdressePost.class, AdressePut.class})
    @Schema(description = "Numéro de l'appartement si existant", example = "31")
    @Column(name = "Numero_Appartement_N", length = 255)
    @JsonView(AdresseListeView.class)
    private String numeroAppartement;

    @Schema(description = "Horodatage de la création de l'adresse", example = "2025-01-01T19:12:54", hidden = true)
    @Column(name = "Horodatage_Ajout_TT", nullable = false)
    @JsonView(AdresseListeView.class)
    private LocalDateTime horodatageAjout;

    @NotNull(groups = {AdressePost.class, AdressePut.class})
    @Schema(description = "Statut de l'adresse", example = "PRINCIPALE")
    @Column(name = "Statut_I", length = 255)
    @JsonView(AdresseListeView.class)
    private StatutAdresse statut;

    // Getter & Setter

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public @NotNull String getCodeCommune() {
        return codeCommune;
    }

    public void setCodeCommune(@NotNull String codeCommune) {
        this.codeCommune = codeCommune;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(@NotNull String codePostal) {
        this.codePostal = codePostal;
    }

    public @NotBlank @NotNull String getNomVille() {
        return nomVille;
    }

    public void setNomVille(@NotBlank @NotNull String nomVille) {
        this.nomVille = nomVille;
    }

    public @NotBlank @NotNull String getNomRue() {
        return nomRue;
    }

    public void setNomRue(@NotBlank @NotNull String nomRue) {
        this.nomRue = nomRue;
    }

    public @NotNull @NotBlank String getNumeroBatiment() {
        return numeroBatiment;
    }

    public void setNumeroBatiment(@NotNull @NotBlank String numeroBatiment) {
        this.numeroBatiment = numeroBatiment;
    }

    public @NotNull String getNumeroAppartement() {
        return numeroAppartement;
    }

    public void setNumeroAppartement(@NotNull String numeroAppartement) {
        this.numeroAppartement = numeroAppartement;
    }

    public LocalDateTime getHorodatageAjout() {
        return horodatageAjout;
    }

    public void setHorodatageAjout(LocalDateTime horodatageAjout) {
        this.horodatageAjout = horodatageAjout;
    }

    public @NotNull StatutAdresse getStatut() {
        return statut;
    }

    public void setStatut(@NotNull StatutAdresse statut) {
        this.statut = statut;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name= "Id_Client_F_C")
    @JsonView(AdresseView.class)
    private Client client;

    @PrePersist
    @PreUpdate
    private void initUser() {
        this.horodatageAjout = LocalDateTime.now();
    }
}
