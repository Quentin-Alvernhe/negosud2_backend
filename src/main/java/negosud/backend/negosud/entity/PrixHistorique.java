package negosud.backend.negosud.entity;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.View.PrixHistoriqueView;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class PrixHistorique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(hidden = true)
    @Column(name = "Id_Prix_Historique_P_C_A", unique = true, nullable = false, updatable = false, length = 36)
    private Long id;

    @NotNull
    @Schema(description = "Prix du produit", example = "150.0")
    @Column(name = "Prix_N", nullable = false)
    @JsonView(PrixHistoriqueView.class)
    private Double prix;

    @NotNull
    @Schema(description = "Taux de TVA applicable", example = "20.0")
    @Column(name = "Taux_TVA_N", nullable = false)
    @JsonView(PrixHistoriqueView.class)
    private Double tauxTVA;

    @Schema(hidden = true)
    @Column(name = "Horodatage_Creation_TT", nullable = false, updatable = false)
    private LocalDateTime horodatageCreation;

    // GETTERS & SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public Double getTauxTVA() {
        return tauxTVA;
    }

    public void setTauxTVA(Double tauxTVA) {
        this.tauxTVA = tauxTVA;
    }

    public LocalDateTime getHorodatageCreation() {
        return horodatageCreation;
    }

    public void setHorodatageCreation(LocalDateTime horodatageCreation) {
        this.horodatageCreation = horodatageCreation;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public List<LigneCommandeFournisseur> getLignesCommandeFournisseur() {
        return lignesCommandeFournisseur;
    }

    public void setLignesCommandeFournisseur(List<LigneCommandeFournisseur> lignesCommandeFournisseur) {
        this.lignesCommandeFournisseur = lignesCommandeFournisseur;
    }

    public List<LigneCommandeClient> getLignesCommandeClient() {
        return lignesCommandeClient;
    }

    public void setLignesCommandeClient(List<LigneCommandeClient> lignesCommandeClient) {
        this.lignesCommandeClient = lignesCommandeClient;
    }

    @ManyToOne()
    @JoinColumn(name= "Id_Article_F_C",updatable = false)
    private Article article;

    @OneToMany(mappedBy = "prixHistorique", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommandeFournisseur> lignesCommandeFournisseur;

    @OneToMany(mappedBy = "prixHistorique", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommandeClient> lignesCommandeClient;

    @PrePersist
    protected void onCreate() {
        horodatageCreation = LocalDateTime.now();
    }
}
