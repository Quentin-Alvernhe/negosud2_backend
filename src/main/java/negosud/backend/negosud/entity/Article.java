package negosud.backend.negosud.entity;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.RequestValidators.ArticlePost;
import negosud.backend.negosud.RequestValidators.ArticlePut;
import negosud.backend.negosud.RequestValidators.CommandeFournisseurPost;
import negosud.backend.negosud.View.*;

import java.util.List;
import java.util.UUID;

@Entity
public class Article {
    @Id
    @NotEmpty(groups = CommandeFournisseurPost.class)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identifiant unique de l'article (uuid)", example = "d3ee2929-212b-4077-af84-694a0e69b8e1")
    @Column(name = "Id_Article_P_C_A", unique = true, nullable = false, updatable = false, length = 36)
    @JsonView({ArticleView.class, CommandeFournisseurCreationView.class, FournisseurView.class, CommandeClientCreationView.class})
    private UUID id;

    @NotNull(groups = {ArticlePost.class, ArticlePut.class})
    @NotBlank(groups = {ArticlePost.class, ArticlePut.class})
    @Schema(description = "Référence interne de l'article", example = "ref_12345")
    @Column(name = "Reference_Article_VC", nullable = false, length = 255)
    @JsonView(ArticleView.class)
    private String reference;

    @NotNull(groups = {ArticlePost.class, ArticlePut.class})
    @NotBlank(groups = {ArticlePost.class, ArticlePut.class})
    @Schema(description = "Nom de l'article", example = "Bollinger 1923")
    @Column(name = "Nom_Article_VC", nullable = false, length = 255)
    @JsonView({ArticleView.class, FournisseurView.class, CommandeFournisseurView.class})
    private String nom;

    @NotNull(groups = {ArticlePost.class, ArticlePut.class})
    @NotBlank(groups = {ArticlePost.class, ArticlePut.class})
    @Schema(description = "Référence du fournisseur de l'article", example = "ref_12345")
    @Column(name = "Reference_Fournisseur_VC", nullable = false, length = 255)
    @JsonView(ArticleView.class)
    private String referenceFournisseur;

    @NotNull(groups = ArticlePost.class)
    @Schema(description = "Quantité de l'article", example = "12")
    @Column(name = "Quantite_Disponible_I", nullable = false)
    @JsonView(ArticleView.class)
    private Integer quantite;

    @NotNull(groups = {ArticlePost.class, ArticlePut.class})
    @Schema(description = "Quantité minimum de l'article", example = "5")
    @Column(name = "Quantite_Stockage_Minimum_I", nullable = false)
    @JsonView(ArticleView.class)
    private Integer quantiteMinimum;

    @NotNull(groups = ArticlePost.class)
    @Schema(description = "Quantité prévisionnelle de l'article", example = "12")
    @Column(name = "Quantite_Previsionnelle_I", nullable = false)
    @JsonView(ArticleView.class)
    private Integer quantitePrevisionnelle;

    @NotNull(groups = {ArticlePost.class, ArticlePut.class})
    @NotBlank(groups = {ArticlePost.class, ArticlePut.class})
    @Schema(description = "Description de l'article", example = "Un champagne d'exception qui coûte une blinde")
    @Column(name = "Description_Article_VC", nullable = false, length = 255)
    @JsonView(ArticleView.class)
    private String description;

    @Schema(description = "Photo de l'article", example = "C://photodelarticle.jpg")
    @Column(name = "Photo_Reference_VC", length = 255)
    @JsonView(ArticleView.class)
    private String photoReference;

    @NotNull(groups = ArticlePost.class)
    @Schema(description = "Article actif ou non", example = "true")
    @Column(name = "Actif_B", nullable = false)
    @JsonView(ArticleView.class)
    private Boolean actif = true;

    @Transient
    @NotNull(groups = {ArticlePost.class, ArticlePut.class})
    @Schema(description = "Prix de l'article", example = "10.0")
    @JsonView(ArticleView.class)
    private double prix;

    @Transient
    @NotNull(groups = {ArticlePost.class, ArticlePut.class})
    @Schema(description = "Taux de TVA de l'article", example = "20")
    @JsonView(ArticleView.class)
    private double tauxTva;

    // GETTER & SETTER
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public @NotNull @NotBlank String getReference() {
        return reference;
    }

    public void setReference(@NotNull @NotBlank String reference) {
        this.reference = reference;
    }

    public @NotNull @NotBlank String getNom() {
        return nom;
    }

    public void setNom(@NotNull @NotBlank String nom) {
        this.nom = nom;
    }

    public @NotNull @NotBlank String getReferenceFournisseur() {
        return referenceFournisseur;
    }

    public void setReferenceFournisseur(@NotNull @NotBlank String referenceFournisseur) {
        this.referenceFournisseur = referenceFournisseur;
    }

    public @NotNull Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(@NotNull Integer quantite) {
        this.quantite = quantite;
    }

    public @NotNull Integer getQuantiteMinimum() {
        return quantiteMinimum;
    }

    public void setQuantiteMinimum(@NotNull Integer quantiteMinimum) {
        this.quantiteMinimum = quantiteMinimum;
    }

    public @NotNull Integer getQuantitePrevisionnelle() {
        return quantitePrevisionnelle;
    }

    public void setQuantitePrevisionnelle(@NotNull Integer quantitePrevisionnelle) {
        this.quantitePrevisionnelle = quantitePrevisionnelle;
    }

    public @NotNull @NotBlank String getDescription() {
        return description;
    }

    public void setDescription(@NotNull @NotBlank String description) {
        this.description = description;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photo) {
        this.photoReference = photo;
    }

    public @NotNull Boolean getActif() {
        return actif;
    }

    public void setActif(@NotNull Boolean actif) {
        this.actif = actif;
    }

    @NotNull
    public double getPrix() {
        return prix;
    }

    public void setPrix(@NotNull double prix) {
        this.prix = prix;
    }

    @NotNull
    public double getTauxTva() {
        return tauxTva;
    }

    public void setTauxTva(@NotNull double tauxTva) {
        this.tauxTva = tauxTva;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public List<PrixHistorique> getPrixHistorique() {
        return prixHistorique;
    }

    public void setPrixHistorique(List<PrixHistorique> prixHistorique) {
        this.prixHistorique = prixHistorique;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name= "Id_Fournisseur_F_C")
    @JsonView({ArticleView.class, ArticleCreationView.class})
    private Fournisseur fournisseur;

    @ManyToOne(optional = false)
    @JoinColumn(name="Id_Categorie_F_C")
    @JsonView({ArticleView.class, ArticleCreationView.class})
    private Categorie categorie;

    @Schema(hidden = true)
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrixHistorique> prixHistorique;
}
