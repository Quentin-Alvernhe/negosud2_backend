package negosud.backend.negosud.entity;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.RequestValidators.EmployePost;
import negosud.backend.negosud.RequestValidators.EmployePut;
import negosud.backend.negosud.View.CommandeFournisseurView;
import negosud.backend.negosud.View.EmployeView;

import java.util.List;

@Entity
public class Employe extends Utilisateur {

    @Schema(description = "Référence d'une photo", example = "/utilisateur/{id}/image.png")
    @Column(name = "Photo_Reference_VC", length = 255)
    @JsonView(EmployeView.class)
    private String photoReference;

    @NotNull(groups = {EmployePost.class, EmployePut.class})
    @Schema(description = "Nom du rôle", example = "EMPLOYE")
    @Column(name = "Role_I", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @JsonView({EmployeView.class, CommandeFournisseurView.class})
    private Role role;

    @Schema(hidden = true)
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandeFournisseur> commandesFournisseurs;

    // GETTER & SETTER
    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String employePhotoReference) {
        this.photoReference = employePhotoReference;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
