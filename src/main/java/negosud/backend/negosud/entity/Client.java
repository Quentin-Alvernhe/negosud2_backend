package negosud.backend.negosud.entity;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.RequestValidators.ClientPost;
import negosud.backend.negosud.RequestValidators.ClientPut;
import negosud.backend.negosud.View.ClientView;

import java.sql.Timestamp;
import java.util.List;

@Entity
public class Client extends Utilisateur{
    @NotNull(groups = {ClientPost.class, ClientPut.class})
    @NotBlank(groups = {ClientPost.class, ClientPut.class})
    @Schema(description = "Pseudonyme", example = "Le_Bon_Vivant")
    @Column(name = "Pseudo_VC", nullable = false)
    @JsonView(ClientView.class)
    private String pseudo;

    @Schema(description = "Horodatage de la dernière connection de l'utilisateur", example = "23/11/2024T13:28:55")
    @Column(name = "Horodatage_Derniere_Connection_TT", nullable = false)
    @JsonView(ClientView.class)
    private Timestamp horodatageDerniereConnection;

    @NotNull(groups = {ClientPost.class, ClientPut.class})
    @NotBlank(groups = {ClientPost.class, ClientPut.class})
    @Schema(description = "Numéro de téléphone", example = "06.00.00.00.00")
    @Column(name = "Numero_Telephone_VC", nullable = false)
    @JsonView(ClientView.class)
    private String numeroTelephone;

    @Schema(description = "Référence d'une photo", example = "/client/{id}/image.png")
    @Column(name = "Client_Photo_Reference_VC")
    @JsonView(ClientView.class)
    private String photoReference;

    public @NotNull @NotBlank String getPseudo() {
        return pseudo;
    }

    public void setPseudo(@NotNull @NotBlank String pseudo) {
        this.pseudo = pseudo;
    }

    public Timestamp getHorodatageDerniereConnection() {
        return horodatageDerniereConnection;
    }

    public void setHorodatageDerniereConnection(Timestamp horodatageDerniereConnection) {
        this.horodatageDerniereConnection = horodatageDerniereConnection;
    }

    public @NotNull @NotBlank String getNumeroTelephone() {
        return numeroTelephone;
    }

    public void setNumeroTelephone(@NotNull @NotBlank String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String clientPhotoReference) {
        this.photoReference = clientPhotoReference;
    }

    public List<Adresse> getAdresses() {
        return adresses;
    }

    public void setAdresses(List<Adresse> adresses) {
        this.adresses = adresses;
    }

    public List<CommandeClient> getCommandesClient() {
        return commandesClient;
    }

    public void setCommandesClient(List<CommandeClient> commandesClient) {
        this.commandesClient = commandesClient;
    }

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonView(ClientView.class)
    private List<Adresse> adresses;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonView(ClientView.class)
    private List<CommandeClient> commandesClient;

}