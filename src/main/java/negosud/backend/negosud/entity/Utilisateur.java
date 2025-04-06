package negosud.backend.negosud.entity;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.RequestValidators.CommandeFournisseurPost;
import negosud.backend.negosud.RequestValidators.UtilisateurPut;
import negosud.backend.negosud.RequestValidators.UtilisateurPost;
import negosud.backend.negosud.View.CommandeFournisseurCreationView;
import negosud.backend.negosud.View.CommandeFournisseurView;
import negosud.backend.negosud.View.UtilisateurView;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Utilisateur {
    @Id
    @NotEmpty(groups = CommandeFournisseurPost.class)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique de l'utilisateur (uuid)", example = "d3ee2929-212b-4077-af84-694a0e69b8e1")
    @Column(name = "Id_Utilisateur_P_C_A", unique = true, nullable = false, updatable = false, length = 36)
    @JsonView({UtilisateurView.class, CommandeFournisseurCreationView.class})
    private UUID id;

    @NotNull(groups = {UtilisateurPost.class, UtilisateurPut.class})
    @NotBlank(groups = {UtilisateurPost.class, UtilisateurPut.class})
    @Schema(description = "Prénom", example = "Jean")
    @Column(name = "Prenom_VC", nullable = false, length = 255)
    @JsonView({UtilisateurView.class, CommandeFournisseurView.class})
    private String prenom;

    @NotNull(groups = {UtilisateurPost.class, UtilisateurPut.class})
    @NotBlank(groups = {UtilisateurPost.class, UtilisateurPut.class})
    @Schema(description = "Nom", example = "Mire")
    @Column(name = "Nom_VC", nullable = false, length = 255)
    @JsonView({UtilisateurView.class, CommandeFournisseurView.class})
    private String nom;

    @NotNull(groups = UtilisateurPost.class)
    @NotBlank(groups = UtilisateurPost.class)
    @Schema(description = "Email", example = "jean.mire@gmail.com")
    @Column(name = "Email_VC", nullable = false, length = 255)
    @JsonView(UtilisateurView.class)
    private String email;

    @NotNull(groups = UtilisateurPost.class)
    @NotBlank(groups = UtilisateurPost.class)
    @Schema(description = "Mot de passe", example = "123@Soleil.")
    @Column(name = "Mot_De_Passe_VC", nullable = false, length = 255)
    @JsonView()
    private String motDePasse;

    @NotNull(groups = {UtilisateurPost.class, UtilisateurPut.class})
    @Schema(description = "Date de naissance", example = "1998-12-21")
    @Column(name = "Date_Naissance_D", nullable = false)
    @JsonView(UtilisateurView.class)
    private LocalDate dateNaissance;


    @NotBlank(groups = {UtilisateurPost.class, UtilisateurPut.class})
    @Schema(description = "Genre", example = "Homme")
    @Column(name = "Genre_VC", nullable = false)
    @JsonView(UtilisateurView.class)
    private String genreUtilisateur;

    @CreationTimestamp
    @CreatedDate
    @Schema(hidden = true)
    @Column(name = "Horodatage_Creation_TT", nullable = false, updatable = false)
    @JsonView(UtilisateurView.class)
    private LocalDateTime horodatageCreation;

    @Schema(hidden = true)
    @Column(name = "Refresh_Token_Horodatage_Expiration_TT")
    private LocalDateTime refreshTokenHorodatageExpiration;

    @Schema(hidden = true)
    @Column(name = "Statut_I", length = 100)
    @JsonView(UtilisateurView.class)
    private StatutUtilisateur statut;

    // GETTER & SETTER

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public @NotNull @NotBlank String getPrenom() {
        return prenom;
    }

    public void setPrenom(@NotNull @NotBlank String prenom) {
        this.prenom = prenom;
    }

    public @NotNull @NotBlank String getNom() {
        return nom;
    }

    public void setNom(@NotNull @NotBlank String nom) {
        this.nom = nom;
    }

    public @NotNull @NotBlank String getEmail() {
        return email;
    }

    public void setEmail(@NotNull @NotBlank String email) {
        this.email = email;
    }

    public @NotNull @NotBlank String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(@NotNull @NotBlank String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public @NotNull String getGenreUtilisateur() {
        return genreUtilisateur;
    }

    public void setGenreUtilisateur(@NotNull String genreUtilisateur) {
        this.genreUtilisateur = genreUtilisateur;
    }

    public LocalDateTime getHorodatageCreation() {
        return horodatageCreation;
    }

    public void setHorodatageCreation(LocalDateTime horodatageCreation) {
        this.horodatageCreation = horodatageCreation;
    }

    public LocalDateTime getRefreshTokenHorodatageExpiration() {
        return refreshTokenHorodatageExpiration;
    }

    public void setRefreshTokenHorodatageExpiration(LocalDateTime refreshTokenHorodatageExpiration) {
        this.refreshTokenHorodatageExpiration = refreshTokenHorodatageExpiration;
    }

    public StatutUtilisateur getStatut() {
        return statut;
    }

    public void setStatut(StatutUtilisateur statut) {
        this.statut = statut;
    }

    @PrePersist
    public void initUser(){
        this.statut = StatutUtilisateur.ACTIF;
    }
}