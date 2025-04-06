package negosud.backend.negosud.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import negosud.backend.negosud.RequestValidators.AdressePost;
import negosud.backend.negosud.RequestValidators.AdressePut;
import negosud.backend.negosud.View.AdresseListeView;
import negosud.backend.negosud.View.AdresseView;
import negosud.backend.negosud.entity.Adresse;
import negosud.backend.negosud.entity.Client;
import negosud.backend.negosud.entity.StatutAdresse;
import negosud.backend.negosud.repository.AdresseRepository;
import negosud.backend.negosud.repository.ClientRepository;
import negosud.backend.negosud.security.AppUserDetails;
import negosud.backend.negosud.security.IsConnected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/utilisateur/client/adresse")
@Tag(name = "Adresse", description = "Opérations concernant les adresses.")
public class AdresseController {
    private final AdresseRepository adresseRepository;
    private final ClientRepository clientRepository;

    @Autowired
    public AdresseController(AdresseRepository adresseRepository, ClientRepository clientRepository) {
        this.adresseRepository = adresseRepository;
        this.clientRepository = clientRepository;
    }

    @IsConnected
    @GetMapping("/all")
    @Operation(
            summary = "Récupérer toutes les adresses",
            description = "Récupère la liste de toutes les adresses."
    )
    @JsonView(AdresseListeView.class)
    public List<Adresse> getAllAdresses() {
        return adresseRepository.findAll();
    }

    @IsConnected
    @GetMapping("/livraison")
    @Operation(
            summary = "Récupérer toutes les adresses de livraison de l'utilisateur réalisant la requête",
            description = "Récupère la liste de toutes les adresses d'un client exceptées les adresses de facturation."
    )
    @JsonView(AdresseListeView.class)
    public List<Adresse> getAllLocalisationAdresses(@AuthenticationPrincipal AppUserDetails client) {
        Optional<Client> foundClient = clientRepository.findByEmail(client.getUsername());
        return adresseRepository.findByClientAndStatutEqualsOrStatutEquals(foundClient.get(), StatutAdresse.PRINCIPALE, StatutAdresse.SECONDAIRE);
    }

    @IsConnected
    @GetMapping("/facturation")
    @Operation(
            summary = "Récupérer toutes les adresses de facturation de l'utilisateur réalisant la requête",
            description = "Récupère la liste de toutes les adresses de facturation d'un client."
    )
    @JsonView(AdresseListeView.class)
    public List<Adresse> getAllFacturationAdresses(@AuthenticationPrincipal AppUserDetails client) {
        return adresseRepository.findByClientAndStatutEquals(client.getClient(), StatutAdresse.FACTURATION);
    }

    @IsConnected
    @PostMapping("/new")
    @Operation(
            summary = "Nouvelle adresse",
            description = "Création d'une adresse."
    )
    @JsonView(AdresseView.class)
    public ResponseEntity<Adresse> addAdresse(@AuthenticationPrincipal AppUserDetails client,
                                              @Validated(AdressePost.class) @RequestBody @Valid Adresse nouvelleAdresse) {
        try {
            if (nouvelleAdresse == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                Client foundClient = clientRepository.findByEmail(client.getUsername()).get();
                nouvelleAdresse.setClient(foundClient);
                ExampleMatcher matcher = ExampleMatcher.matching()
                                .withIgnorePaths("id","horodatageAjout")
                                .withIgnoreCase();
                Example<Adresse> adresseExample = Example.of(nouvelleAdresse, matcher);
                if (adresseRepository.exists(adresseExample)) {
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
                }
                Adresse createdAdresse = adresseRepository.save(nouvelleAdresse);
                return new ResponseEntity<Adresse>(createdAdresse, HttpStatus.CREATED);
            }
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @IsConnected
    @PutMapping("/{id}/edit")
    @Operation(
            summary = "Modification d'une adresse",
            description = "Modification des informations d'une adresse."
    )
    @JsonView(AdresseView.class)
    public ResponseEntity<Adresse> updateAdresse(
            @PathVariable UUID id,
            @Validated(AdressePut.class) @RequestBody @Valid Adresse modifiedAdresse) {
        if (modifiedAdresse == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Adresse> foundAdresse = adresseRepository.findById(id);

        if (foundAdresse.isEmpty()) {
            return new ResponseEntity<>(modifiedAdresse, HttpStatus.NOT_FOUND);
        }

        Adresse adresseDb = foundAdresse.get();
        modifiedAdresse.setId(adresseDb.getId());
        modifiedAdresse.setClient(adresseDb.getClient());
        adresseRepository.save(modifiedAdresse);
        return new ResponseEntity<>(modifiedAdresse, HttpStatus.OK);
    }

    @IsConnected
    @DeleteMapping("/{id}/delete")
    @Operation(
            summary = "Supprimer une adresse",
            description = "Supprime une adresse par son ID."
    )
    @JsonView(AdresseView.class)
    public ResponseEntity<Adresse> deleteAdresse(@PathVariable UUID id) {
        Optional<Adresse> foundAdresse = adresseRepository.findById(id);
        if (foundAdresse.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adresseRepository.delete(foundAdresse.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
