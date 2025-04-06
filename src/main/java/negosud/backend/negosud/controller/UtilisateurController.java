package negosud.backend.negosud.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import negosud.backend.negosud.View.UtilisateurView;
import negosud.backend.negosud.entity.StatutUtilisateur;
import negosud.backend.negosud.entity.Utilisateur;
import negosud.backend.negosud.repository.UtilisateurRepository;
import negosud.backend.negosud.security.AppUserDetails;
import negosud.backend.negosud.security.IsConnected;
import negosud.backend.negosud.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/utilisateur")
@Tag(name = "Utilisateur", description = "Opérations concernant les utilisateurs en général.")
public class UtilisateurController {

    private UtilisateurRepository utilisateurRepository;
    private AuthenticationProvider authenticationProvider;
    private JwtUtils jwtUtils;
    private BCryptPasswordEncoder encoder;

    @Autowired
    public UtilisateurController(UtilisateurRepository utilisateurRepository,
                                 AuthenticationProvider authenticationProvider,
                                 JwtUtils jwtUtils,
                                 BCryptPasswordEncoder encoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.authenticationProvider = authenticationProvider;
        this.jwtUtils = jwtUtils;
        this.encoder = encoder;
    }

    @PostMapping("/connect")
    @Operation(
            summary = "Connexion",
            description = "Connexion d'un employé avec son nom d'utilisateur et son mot de passe."
    )
    public ResponseEntity<String> connectEmploye(@RequestBody @Valid UtilisateurConnect utilisateur) {

        try {
            AppUserDetails userDetails = (AppUserDetails) authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            utilisateur.getEmail(),
                            utilisateur.getMotDePasse()
                    )
            ).getPrincipal();

            return ResponseEntity.ok(jwtUtils.generateToken(userDetails));
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @IsConnected
    @PatchMapping("/change_password")
    @Operation(
            summary = "Modification d'un client",
            description = "Modification des informations du client excepté pour le mot de passe."
    )
    @JsonView(UtilisateurView.class)
    public ResponseEntity<Utilisateur> changePassword(
            @AuthenticationPrincipal AppUserDetails client,
            @RequestBody @Valid ChangePasswordUtilisateur modifiedPassword) {
        Optional<Utilisateur> foundUtilisateur = utilisateurRepository.findByEmail(client.getUsername());
        if (foundUtilisateur.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (modifiedPassword.getMotDePasse() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Utilisateur modifiedUtilisateur = foundUtilisateur.get();
        modifiedUtilisateur.setMotDePasse(encoder.encode(modifiedPassword.getMotDePasse()));
        modifiedUtilisateur.setRefreshTokenHorodatageExpiration(LocalDateTime.now());
        utilisateurRepository.save(modifiedUtilisateur);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @IsConnected
    @PatchMapping("/{id}/disable")
    @Operation(
            summary = "Désactivation d'un utilisateur",
            description = "Désactivation d'un utilisateur"
    )
    @JsonView(UtilisateurView.class)
    public ResponseEntity<Utilisateur> desactiverUtilisateur(@PathVariable UUID id){
        Utilisateur utilisateur = utilisateurRepository.findById(id).orElse(null);
        if (utilisateur == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            utilisateur.setStatut(StatutUtilisateur.VEROUILLE);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    static class UtilisateurConnect {

        @NotBlank
        @NotNull
        private String email;

        @NotBlank
        @NotNull
        private String motDePasse;

        public @NotBlank @NotNull String getEmail() {
            return email;
        }

        public void setEmail(@NotBlank @NotNull String email) {
            this.email = email;
        }

        public @NotBlank @NotNull String getMotDePasse() {
            return motDePasse;
        }

        public void setMotDePasse(@NotBlank @NotNull String password) {
            this.motDePasse = password;
        }
    }

    static class ChangePasswordUtilisateur {
        @NotBlank
        private String motDePasse;

        public String getMotDePasse() {
            return motDePasse;
        }

        public void setMotDePasse(String motDePasse) {
            this.motDePasse = motDePasse;
        }
    }
}
