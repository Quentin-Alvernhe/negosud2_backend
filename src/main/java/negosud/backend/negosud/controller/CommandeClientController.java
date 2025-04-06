package negosud.backend.negosud.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import negosud.backend.negosud.RequestValidators.CommandeClientPost;
import negosud.backend.negosud.View.CommandeClientCreationView;
import negosud.backend.negosud.View.CommandeClientView;
import negosud.backend.negosud.View.CommandeFournisseurView;
import negosud.backend.negosud.entity.*;
import negosud.backend.negosud.repository.*;
import negosud.backend.negosud.security.AppUserDetails;
import negosud.backend.negosud.security.IsClientAndNotEmploye;
import negosud.backend.negosud.security.IsEmployeOrUpper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController

@RequestMapping("/api/commande/client")
@Tag(name = "Commandes client", description = "Endpoints pour gérer les commandes client.")
public class CommandeClientController {
    private final ClientRepository clientRepository;
    private final ArticleRepository articleRepository;
    private final PrixHistoriqueRepository prixHistoriqueRepository;
    private final LigneCommandeClientRepository ligneCommandeClientRepository;
    private final AdresseRepository adresseRepository;
    private CommandeClientRepository commandeClientRepository;

    public CommandeClientController(ClientRepository clientRepository,
                                    ArticleRepository articleRepository,
                                    PrixHistoriqueRepository prixHistoriqueRepository,
                                    LigneCommandeClientRepository ligneCommandeClientRepository,
                                    CommandeClientRepository commandeClientRepository,
                                    AdresseRepository adresseRepository) {
        this.clientRepository = clientRepository;
        this.articleRepository = articleRepository;
        this.prixHistoriqueRepository = prixHistoriqueRepository;
        this.ligneCommandeClientRepository = ligneCommandeClientRepository;
        this.commandeClientRepository = commandeClientRepository;
        this.adresseRepository = adresseRepository;
    }

    @IsClientAndNotEmploye
    @GetMapping("/all")
    @Operation(
            summary = "Liste les commandes client",
            description = "Récupère la liste de toutes les commandes fournisseurs."
    )
    @JsonView(CommandeClientView.class)
    public List<CommandeClient> getAllCommandesClient(
            @AuthenticationPrincipal AppUserDetails user,
            @RequestParam(required = false, value="statut") StatutCommande statutCommande) {
        List<CommandeClient> commandeClientList = new ArrayList<>();

        Optional<Client> client = clientRepository.findByEmail(user.getUsername());

        if (client.isPresent()) {
            Client foundClient = client.get();
            if (statutCommande == null) {
                commandeClientList = commandeClientRepository.findAllByClient(foundClient);
            } else {
                commandeClientList = commandeClientRepository.findByStatutCommande(statutCommande);
            }
            for (CommandeClient commandeClient : commandeClientList){
                parseLignesCommandeClientList(commandeClient.getLignesCommandeClient());
            }
            updateCommandeClientListPrices(commandeClientList);
        }


        return commandeClientList;
    }

    @IsClientAndNotEmploye
    @PostMapping("/new")
    @Operation(
            summary = "Crée une commande client",
            description = "Création d'une nouvelle commande pour un client."
    )
    @JsonView(CommandeClientCreationView.class)
    public ResponseEntity<CommandeClient> newCommandeClient(
            @AuthenticationPrincipal AppUserDetails user,
            @Validated(CommandeClientPost.class) @RequestBody @Valid CommandeClient nouvelleCommandeClient) {

        if (nouvelleCommandeClient == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Client> client = clientRepository.findByEmail(user.getUsername());
        if (!client.isPresent()){
            return new ResponseEntity<>(nouvelleCommandeClient, HttpStatus.UNAUTHORIZED);
        }
        Client foundClient = client.get();
        nouvelleCommandeClient.setClient(foundClient);
        
        List<Adresse> adresseLivraison = adresseRepository.findByClientAndId(foundClient, nouvelleCommandeClient.getAdresseLivraison().getId());
        if (adresseLivraison.isEmpty()){
            return new ResponseEntity<>(nouvelleCommandeClient, HttpStatus.BAD_REQUEST);
        }

        List<Adresse> adresseFacturation = adresseRepository.findByClientAndId(foundClient, nouvelleCommandeClient.getAdresseFacturation().getId());
        if (adresseLivraison.isEmpty()){
            return new ResponseEntity<>(nouvelleCommandeClient, HttpStatus.BAD_REQUEST);
        }

        // Mise du statut sur CREE par défaut
        nouvelleCommandeClient.setStatutCommande(StatutCommande.CREE);

        List<LigneCommandeClient> ligneCommandeClientList = compactedLigneCommandeClientList(nouvelleCommandeClient.getLignesCommandeClient());
        nouvelleCommandeClient.setLignesCommandeClient(null);

        if (ligneCommandeClientList.isEmpty()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Paramètrage de l'index du numéro de commande pour recommencer à 1 chaque jour
        List<CommandeClient> lastCommandeClient = commandeClientRepository.findByHorodatageCreationCommandeAfterOrderByIndexNumeroCommande(LocalDate.now().atStartOfDay());
            if (lastCommandeClient.size() == 0) {
                nouvelleCommandeClient.setIndexLastNumeroCommande(0);
            } else {
                nouvelleCommandeClient.setIndexLastNumeroCommande(lastCommandeClient.getLast().getIndexNumeroCommande());
            }

        // Création de la commande fournisseur pour obtenir un id et permettre l'enregistrement des lignes de commandes
        UUID idCommandeClientCree = commandeClientRepository.saveAndFlush(nouvelleCommandeClient).getId();
        Optional<CommandeClient> commandeClientCree = commandeClientRepository.findById(idCommandeClientCree);

        // Création des lignes de commande client
        if (!ligneCommandeClientList.isEmpty() && commandeClientCree.isPresent()) {
            nouvelleCommandeClient = commandeClientCree.get();
            for (LigneCommandeClient ligneCommandeClient : ligneCommandeClientList) {
                if (ligneCommandeClient.getQuantite() > 0) {
                    ligneCommandeClient.setCommandeClient(nouvelleCommandeClient);
                    Optional<Article> foundArticle = articleRepository.findById(ligneCommandeClient.getArticle().getId());
                    if (foundArticle.isEmpty()) {
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                    ligneCommandeClient.setPrixHistorique(prixHistoriqueRepository.findFirstPrixHistoriqueArticleByArticleOrderByHorodatageCreationDesc(foundArticle.get()));
                    ligneCommandeClient.setPrixUnitaire(ligneCommandeClient.getPrixHistorique().getPrix());
                    ligneCommandeClient.setPrixLigne(ligneCommandeClient.getPrixUnitaire() * ligneCommandeClient.getQuantite());
                    ligneCommandeClientRepository.save(ligneCommandeClient);
                }
            }
            nouvelleCommandeClient.setLignesCommandeClient(ligneCommandeClientList);
            nouvelleCommandeClient.setNumeroCommande(commandeClientRepository.findById(nouvelleCommandeClient.getId()).get().getNumeroCommande());
        }
        updateCommandeClientPrice(nouvelleCommandeClient);
        updateCommandeClient(nouvelleCommandeClient);

        return new ResponseEntity<>(nouvelleCommandeClient, HttpStatus.OK);
    }

    @IsEmployeOrUpper
    @PatchMapping("/{id}/update")
    @Operation(
            summary = "Crée une commande fournisseur",
            description = "Récupère la liste de toutes les commandes fournisseurs."
    )
    @JsonView(CommandeFournisseurView.class)
    public ResponseEntity<CommandeClient> updateCommandeClient(
            @PathVariable UUID id,
            @RequestParam(required = true, value="statut") StatutCommande nouveauStatutCommande) {
        Optional<CommandeClient> foundCommandeClient = commandeClientRepository.findById(id);
        if (foundCommandeClient.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        CommandeClient commandeClient = foundCommandeClient.get();
        updateCommandeClientPrice(commandeClient);
        StatutCommande ancienStatutCommande = commandeClient.getStatutCommande();

        if (ancienStatutCommande == StatutCommande.CREE && nouveauStatutCommande == StatutCommande.PREPARATION){
            commandeClient.setStatutCommande(StatutCommande.PREPARATION);
        } else if (ancienStatutCommande == StatutCommande.PREPARATION && nouveauStatutCommande == StatutCommande.LIVRAISON) {
            commandeClient.setStatutCommande(StatutCommande.LIVRAISON);
        } else if (ancienStatutCommande == StatutCommande.LIVRAISON && nouveauStatutCommande == StatutCommande.LIVRE) {
            commandeClient.setStatutCommande(StatutCommande.LIVRE);
            commandeClient.setHorodatageLivraison(LocalDateTime.now());
        } else if (ancienStatutCommande == StatutCommande.LIVRE && nouveauStatutCommande == StatutCommande.RETOUR) {
            commandeClient.setStatutCommande(StatutCommande.RETOUR);
        } else if (ancienStatutCommande != StatutCommande.ANNULE && ancienStatutCommande != StatutCommande.RETOUR && nouveauStatutCommande == StatutCommande.ANNULE) {
            commandeClient.setStatutCommande(StatutCommande.ANNULE);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        CommandeClient updatedCommandeClient = commandeClientRepository.save(commandeClient);
        updateCommandeClient(commandeClientRepository.findById(updatedCommandeClient.getId()).get());
        return new ResponseEntity<>(updatedCommandeClient, HttpStatus.OK);
    }

    private List<LigneCommandeClient> compactedLigneCommandeClientList (List<LigneCommandeClient> ligneCommandeClientList) {
        List<LigneCommandeClient> compactedLigneCommandeClientList;
        if (!ligneCommandeClientList.isEmpty()) {
            compactedLigneCommandeClientList = new ArrayList<>();
            for (LigneCommandeClient ligneCommandeClient : ligneCommandeClientList) {
                if (ligneCommandeClient.getQuantite() > 0 && ligneCommandeClient.getQuantite() != null) {
                    compactedLigneCommandeClientList.stream()
                            .filter(
                                    currentLigneCommandeFournisseur -> currentLigneCommandeFournisseur.getArticle().getId().equals(ligneCommandeClient.getArticle().getId())
                            )
                            .findFirst()
                            .map(
                                    currentLigneCommandeFournisseur -> {
                                        currentLigneCommandeFournisseur.setQuantite(currentLigneCommandeFournisseur.getQuantite() + ligneCommandeClient.getQuantite());
                                        return currentLigneCommandeFournisseur;
                                    }
                            )
                            .orElseGet(() -> {
                                compactedLigneCommandeClientList.add(ligneCommandeClient);
                                        return ligneCommandeClient;
                                    }
                            );
                }
            }
        } else {
            compactedLigneCommandeClientList = null;
        }
        return compactedLigneCommandeClientList;
    }

    private void updateCommandeClientListPrices(List<CommandeClient> commandeClientList) {
        if (!commandeClientList.isEmpty()) {
            for (CommandeClient commandeClient: commandeClientList) {
                updateCommandeClientPrice(commandeClient);
            }
        }
    }

    private void updateCommandeClientPrice(CommandeClient commandeClient) {
        parseLignesCommandeClientList(commandeClient.getLignesCommandeClient());
        Double prixTotal = commandeClient.getLignesCommandeClient()
                .stream()
                .mapToDouble(LigneCommandeClient::getPrixLigne)
                .sum();
        commandeClient.setPrixTotal(prixTotal);
    }

    private void parseLignesCommandeClientList(List<LigneCommandeClient> ligneCommandeClientList) {
        if (!ligneCommandeClientList.isEmpty()) {
            for (LigneCommandeClient ligneCommandeClient : ligneCommandeClientList) {
                ligneCommandeClient.setArticle(ligneCommandeClient.getPrixHistorique().getArticle());
                ligneCommandeClient.setPrixUnitaire(ligneCommandeClient.getPrixHistorique().getPrix());
                ligneCommandeClient.setPrixLigne(ligneCommandeClient.getPrixUnitaire() * ligneCommandeClient.getQuantite());
            }
        }
    }

    private void updateCommandeClient(CommandeClient commandeClient) {
        if (commandeClient != null) {
            List<LigneCommandeClient> ligneCommandeClientList = commandeClient.getLignesCommandeClient();
            for (LigneCommandeClient ligneCommandeClient : ligneCommandeClientList) {
                updateArticleQuantite(ligneCommandeClient, commandeClient.getStatutCommande());
            }
        }
    }

    private void updateArticleQuantite(LigneCommandeClient ligneCommandeClient, StatutCommande statutCommandeClient) {
        Article article = ligneCommandeClient.getArticle();
        Integer quantite = ligneCommandeClient.getQuantite();
        Optional<Article> foundArticle = articleRepository.findById(article.getId());
        if (foundArticle.isPresent() && quantite > 0) {
            article = foundArticle.get();
            if (statutCommandeClient == StatutCommande.CREE) {
                article.setQuantitePrevisionnelle(article.getQuantitePrevisionnelle() - quantite);
            } else if (statutCommandeClient == StatutCommande.LIVRE) {
                article.setQuantite(article.getQuantite() - quantite);
            } else if (statutCommandeClient == StatutCommande.ANNULE) {
                article.setQuantitePrevisionnelle(article.getQuantitePrevisionnelle() + quantite);
            } else if (statutCommandeClient == StatutCommande.RETOUR) {
                article.setQuantite(article.getQuantite() + quantite);
                article.setQuantitePrevisionnelle(article.getQuantitePrevisionnelle() + quantite);
            }
            articleRepository.save(article);
        }
    }
}
