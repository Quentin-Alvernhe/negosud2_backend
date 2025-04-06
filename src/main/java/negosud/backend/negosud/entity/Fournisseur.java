package negosud.backend.negosud.entity;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.RequestValidators.CommandeFournisseurPost;
import negosud.backend.negosud.RequestValidators.FournisseurPost;
import negosud.backend.negosud.View.*;

import java.util.List;
import java.util.UUID;

@Entity
public class Fournisseur {
    @Id
    @NotEmpty(groups = CommandeFournisseurPost.class)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique du fournisseur (uuid)", example = "a3a9693a-03d6-4cb9-b9cd-90c6008cd85f")
    @Column(name = "Id_Fournisseur_P_C_A", unique = true, nullable = false, updatable = false, length = 36)
    @JsonView({FournisseurCreationView.class, ArticleFournisseurView.class, CommandeFournisseurCreationView.class})
    private UUID id;

    @NotNull(groups = FournisseurPost.class)
    @NotBlank(groups = FournisseurPost.class)
    @Schema(description = "Nom", example = "Champagne negociant")
    @Column(name = "Nom_Fournisseur_VC", nullable = false, length = 255)
    @JsonView({FournisseurCreationView.class, ArticleFournisseurView.class, CommandeFournisseurView.class})
    private String nom;

    @NotNull(groups = FournisseurPost.class)
    @NotBlank(groups = FournisseurPost.class)
    @Schema(description = "Email", example = "champagne.negociant@email.com")
    @Column(name = "Email_VC", nullable = false, length = 255)
    @JsonView(FournisseurCreationView.class)
    private String email;

    @NotNull(groups = FournisseurPost.class)
    @NotBlank(groups = FournisseurPost.class)
    @Schema(description = "Telephone", example = "0383456763")
    @Column(name = "Numero_Telephone_VC", nullable = false, length = 255)
    @JsonView(FournisseurCreationView.class)
    private String phone;

    @Schema(description = "Site web fournisseur", example = "https://champagnenegociant.com")
    @Column(name = "Web_Site_VC", nullable = false, length = 255)
    @JsonView(FournisseurView.class)
    private String website;

    @Schema(description = "Code postal fournisseur", example = "54000")
    @Column(name = "Code_Postal_N", nullable = false)
    @JsonView(FournisseurView.class)
    private String codePostal;

    @Schema(description = "Code commune INSEE fournisseur", example = "1223")
    @Column(name = "Code_Commune_N", nullable = false)
    @JsonView(FournisseurView.class)
    private String codeCommune;

    @Schema(description = "Numero et nom de la rue fournisseur", example = "12 Rue du champagne")
    @Column(name = "Adresse_Nom_Rue_VC", nullable = false, length = 255)
    @JsonView(FournisseurView.class)
    private String adresse;

    @Schema(description = "Numero batiment fournisseur", example = "22")
    @Column(name = "Numero_Batiment_N", nullable = false)
    @JsonView(FournisseurView.class)
    private String numeroBatiment;

    @Schema(description = "Nom ville fournisseur", example = "Châlon en champagne")
    @Column(name = "Nom_Ville_VC", nullable = false, length = 255)
    @JsonView(FournisseurView.class)
    private String ville;

    @NotNull
    @Schema(description = "Statut du fournisseur", example = "true")
    @Column(name = "Statut_B", nullable = false)
    @JsonView(FournisseurView.class)
    private Boolean statut;

    // GETTER & SETTER


    public @NotEmpty(groups = CommandeFournisseurPost.class) UUID getId() {
        return id;
    }

    public void setId(@NotEmpty(groups = CommandeFournisseurPost.class) UUID id) {
        this.id = id;
    }

    public @NotNull(groups = FournisseurPost.class) @NotBlank(groups = FournisseurPost.class) String getNom() {
        return nom;
    }

    public void setNom(@NotNull(groups = FournisseurPost.class) @NotBlank(groups = FournisseurPost.class) String nom) {
        this.nom = nom;
    }

    public @NotNull(groups = FournisseurPost.class) @NotBlank(groups = FournisseurPost.class) String getEmail() {
        return email;
    }

    public void setEmail(@NotNull(groups = FournisseurPost.class) @NotBlank(groups = FournisseurPost.class) String email) {
        this.email = email;
    }

    public @NotNull(groups = FournisseurPost.class) @NotBlank(groups = FournisseurPost.class) String getPhone() {
        return phone;
    }

    public void setPhone(@NotNull(groups = FournisseurPost.class) @NotBlank(groups = FournisseurPost.class) String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getCodeCommune() {
        return codeCommune;
    }

    public void setCodeCommune(String codeCommune) {
        this.codeCommune = codeCommune;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNumeroBatiment() {
        return numeroBatiment;
    }

    public void setNumeroBatiment(String numeroBatiment) {
        this.numeroBatiment = numeroBatiment;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public @NotNull Boolean getStatut() {
        return statut;
    }

    public void setStatut(@NotNull Boolean statut) {
        this.statut = statut;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public List<CommandeFournisseur> getCommandesFournisseurs() {
        return commandesFournisseurs;
    }

    public void setCommandesFournisseurs(List<CommandeFournisseur> commandesFournisseurs) {
        this.commandesFournisseurs = commandesFournisseurs;
    }

    @OneToMany(mappedBy = "fournisseur", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonView(FournisseurView.class)
    private List<Article> articles;

    @Schema(hidden = true)
    @OneToMany(mappedBy = "fournisseur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandeFournisseur> commandesFournisseurs;
}
