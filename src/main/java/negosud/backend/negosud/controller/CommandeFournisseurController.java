package negosud.backend.negosud.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import negosud.backend.negosud.RequestValidators.CommandeFournisseurPost;
import negosud.backend.negosud.View.CommandeFournisseurCreationView;
import negosud.backend.negosud.View.CommandeFournisseurView;
import negosud.backend.negosud.entity.*;
import negosud.backend.negosud.repository.*;
import negosud.backend.negosud.security.IsEmployeOrUpper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController

@RequestMapping("/api/commande/fournisseur")
@Tag(name = "Commandes fournisseurs", description = "Endpoints pour gérer les commandes fournisseurs.")
public class CommandeFournisseurController {
    private final FournisseurRepository fournisseurRepository;
    private final EmployeRepository employeRepository;
    private final ArticleRepository articleRepository;
    private final PrixHistoriqueRepository prixHistoriqueRepository;
    private final LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository;
    private CommandeFournisseurRepository commandeFournisseurRepository;

    public CommandeFournisseurController(CommandeFournisseurRepository commandeFournisseurRepository, FournisseurRepository fournisseurRepository, EmployeRepository employeRepository, ArticleRepository articleRepository, PrixHistoriqueRepository prixHistoriqueRepository, LigneCommandeFournisseurRepository ligneCommandeFournisseurRepository) {
        this.commandeFournisseurRepository = commandeFournisseurRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.employeRepository = employeRepository;
        this.articleRepository = articleRepository;
        this.prixHistoriqueRepository = prixHistoriqueRepository;
        this.ligneCommandeFournisseurRepository = ligneCommandeFournisseurRepository;
    }

    @IsEmployeOrUpper
    @GetMapping("/all")
    @Operation(
            summary = "Liste les commandes fournisseurs",
            description = "Récupère la liste de toutes les commandes fournisseurs."
    )
    @JsonView(CommandeFournisseurView.class)
    public List<CommandeFournisseur> getAllCommandesFournisseurs(@RequestParam(required = false, value="statut") StatutCommande statutCommande) {
        List<CommandeFournisseur> commandeFournisseurList;
        if (statutCommande == null) {
            commandeFournisseurList = commandeFournisseurRepository.findAll();
        } else {
            commandeFournisseurList = commandeFournisseurRepository.findByStatutCommande(statutCommande);
        }
        for (CommandeFournisseur commandeFournisseur : commandeFournisseurList){
            parseLignesCommandeFournisseurList(commandeFournisseur.getLignesCommandeFournisseur());
        }
        updateCommandeFournisseurListPrices(commandeFournisseurList);
        return commandeFournisseurList;
    }

    @IsEmployeOrUpper
    @PostMapping("/new")
    @Operation(
            summary = "Crée une commande fournisseur",
            description = "Récupère la liste de toutes les commandes fournisseurs."
    )
    @JsonView(CommandeFournisseurCreationView.class)
    public ResponseEntity<CommandeFournisseur> newCommandeFournisseur(@Validated(CommandeFournisseurPost.class) @RequestBody @Valid CommandeFournisseur nouvelleCommandeFournisseur) {
        if (nouvelleCommandeFournisseur == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<Fournisseur> foundFournissseur = fournisseurRepository.findById(nouvelleCommandeFournisseur.getFournisseur().getId());
        Optional<Employe> foundEmploye = employeRepository.findById(nouvelleCommandeFournisseur.getEmploye().getId());
        if (foundFournissseur.isPresent() && foundEmploye.isPresent()) {
            nouvelleCommandeFournisseur.setFournisseur(foundFournissseur.get());
            nouvelleCommandeFournisseur.setEmploye(foundEmploye.get());
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Mise du statut sur CREE par défaut
        nouvelleCommandeFournisseur.setStatutCommande(StatutCommande.CREE);

        List<LigneCommandeFournisseur> ligneCommandeFournisseurList = compactedLigneCommandeFournisseurList(nouvelleCommandeFournisseur.getLignesCommandeFournisseur());
        nouvelleCommandeFournisseur.setLignesCommandeFournisseur(null);

        if (ligneCommandeFournisseurList.isEmpty()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<CommandeFournisseur> lastCommandeFournisseur = commandeFournisseurRepository.findByHorodatageCreationCommandeAfterOrderByIndexNumeroCommande(LocalDate.now().atStartOfDay());
            if (lastCommandeFournisseur.size() == 0) {
                nouvelleCommandeFournisseur.setIndexLastNumeroCommande(0);
            } else {
                nouvelleCommandeFournisseur.setIndexLastNumeroCommande(lastCommandeFournisseur.getLast().getIndexNumeroCommande());
            }

        // Création de la commande fournisseur pour obtenir un id et permettre l'enregistrement des lignes de commandes
        UUID idCommandeFournisseurCree = commandeFournisseurRepository.saveAndFlush(nouvelleCommandeFournisseur).getId();
        Optional<CommandeFournisseur> commandeFournisseurCree = commandeFournisseurRepository.findById(idCommandeFournisseurCree);

        if (!ligneCommandeFournisseurList.isEmpty() && commandeFournisseurCree.isPresent()) {
            nouvelleCommandeFournisseur = commandeFournisseurCree.get();
            for (LigneCommandeFournisseur ligneCommandeFournisseur : ligneCommandeFournisseurList) {
                if (ligneCommandeFournisseur.getQuantite() > 0) {
                    ligneCommandeFournisseur.setCommandeFournisseur(nouvelleCommandeFournisseur);
                    Optional<Article> foundArticle = articleRepository.findById(ligneCommandeFournisseur.getArticle().getId());
                    if (foundArticle.isEmpty()) {
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    }
                    ligneCommandeFournisseur.setPrixHistorique(prixHistoriqueRepository.findFirstPrixHistoriqueArticleByArticleOrderByHorodatageCreationDesc(foundArticle.get()));
                    ligneCommandeFournisseur.setPrixUnitaire(ligneCommandeFournisseur.getPrixHistorique().getPrix());
                    ligneCommandeFournisseur.setPrixLigne(ligneCommandeFournisseur.getPrixUnitaire() * ligneCommandeFournisseur.getQuantite());
                    ligneCommandeFournisseurRepository.save(ligneCommandeFournisseur);
                }
            }
            nouvelleCommandeFournisseur.setLignesCommandeFournisseur(ligneCommandeFournisseurList);
            nouvelleCommandeFournisseur.setNumeroCommande(commandeFournisseurRepository.findById(nouvelleCommandeFournisseur.getId()).get().getNumeroCommande());
        }
        updateCommandeFournisseurPrice(nouvelleCommandeFournisseur);
        updateCommandeFournisseur(nouvelleCommandeFournisseur);

        return new ResponseEntity<>(nouvelleCommandeFournisseur, HttpStatus.OK);
    }

    @IsEmployeOrUpper
    @PatchMapping("/{id}/update")
    @Operation(
            summary = "Crée une commande fournisseur",
            description = "Récupère la liste de toutes les commandes fournisseurs."
    )
    @JsonView(CommandeFournisseurView.class)
    public ResponseEntity<CommandeFournisseur> newCommandeFournisseur(
            @PathVariable UUID id,
            @RequestParam(required = true, value="statut") StatutCommande nouveauStatutCommande) {
        Optional<CommandeFournisseur> foundCommandeFournisseur = commandeFournisseurRepository.findById(id);
        if (foundCommandeFournisseur.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        CommandeFournisseur commandeFournisseur = foundCommandeFournisseur.get();
        updateCommandeFournisseurPrice(commandeFournisseur);
        StatutCommande ancienStatutCommande = commandeFournisseur.getStatutCommande();

        if (ancienStatutCommande == StatutCommande.CREE && nouveauStatutCommande == StatutCommande.PREPARATION){
            commandeFournisseur.setStatutCommande(StatutCommande.PREPARATION);
        } else if (ancienStatutCommande == StatutCommande.PREPARATION && nouveauStatutCommande == StatutCommande.LIVRAISON) {
            commandeFournisseur.setStatutCommande(StatutCommande.LIVRAISON);
        } else if (ancienStatutCommande == StatutCommande.LIVRAISON && nouveauStatutCommande == StatutCommande.LIVRE) {
            commandeFournisseur.setStatutCommande(StatutCommande.LIVRE);
            commandeFournisseur.setHorodatageLivraison(LocalDateTime.now());
        } else if (ancienStatutCommande == StatutCommande.LIVRE && nouveauStatutCommande == StatutCommande.RETOUR) {
            commandeFournisseur.setStatutCommande(StatutCommande.RETOUR);
        } else if (ancienStatutCommande != StatutCommande.ANNULE && ancienStatutCommande != StatutCommande.RETOUR && nouveauStatutCommande == StatutCommande.ANNULE) {
            commandeFournisseur.setStatutCommande(StatutCommande.ANNULE);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        CommandeFournisseur updatedCommandeFournisseur = commandeFournisseurRepository.save(commandeFournisseur);
        updateCommandeFournisseur(commandeFournisseurRepository.findById(updatedCommandeFournisseur.getId()).get());
        return new ResponseEntity<>(updatedCommandeFournisseur, HttpStatus.OK);
    }

    private List<LigneCommandeFournisseur> compactedLigneCommandeFournisseurList (List<LigneCommandeFournisseur> ligneCommandeFournisseurList) {
        List<LigneCommandeFournisseur> compactedLigneCommandeFournisseurList;
        if (!ligneCommandeFournisseurList.isEmpty()) {
            compactedLigneCommandeFournisseurList = new ArrayList<>();
            for (LigneCommandeFournisseur ligneCommandeFournisseur : ligneCommandeFournisseurList) {
                if (ligneCommandeFournisseur.getQuantite() > 0 && ligneCommandeFournisseur.getQuantite() != null) {
                    compactedLigneCommandeFournisseurList.stream()
                            .filter(
                                    currentLigneCommandeFournisseur -> currentLigneCommandeFournisseur.getArticle().getId().equals(ligneCommandeFournisseur.getArticle().getId())
                            )
                            .findFirst()
                            .map(
                                    currentLigneCommandeFournisseur -> {
                                        currentLigneCommandeFournisseur.setQuantite(currentLigneCommandeFournisseur.getQuantite() + ligneCommandeFournisseur.getQuantite());
                                        return currentLigneCommandeFournisseur;
                                    }
                            )
                            .orElseGet(() -> {
                                        compactedLigneCommandeFournisseurList.add(ligneCommandeFournisseur);
                                        return ligneCommandeFournisseur;
                                    }
                            );
                }
            }
        } else {
            compactedLigneCommandeFournisseurList = null;
        }
        return compactedLigneCommandeFournisseurList;
    }

    private void updateCommandeFournisseurListPrices(List<CommandeFournisseur> commandeFournisseurList) {
        if (!commandeFournisseurList.isEmpty()) {
            for (CommandeFournisseur commandeFournisseur : commandeFournisseurList) {
                updateCommandeFournisseurPrice(commandeFournisseur);
            }
        }
    }

    private void updateCommandeFournisseurPrice(CommandeFournisseur commandeFournisseur) {
        parseLignesCommandeFournisseurList(commandeFournisseur.getLignesCommandeFournisseur());
        Double prixTotal = commandeFournisseur.getLignesCommandeFournisseur()
                .stream()
                .mapToDouble(LigneCommandeFournisseur::getPrixLigne)
                .sum();
        commandeFournisseur.setPrixTotal(prixTotal);
    }

    private void parseLignesCommandeFournisseurList(List<LigneCommandeFournisseur> ligneCommandeFournisseurList) {
        if (!ligneCommandeFournisseurList.isEmpty()) {
            for (LigneCommandeFournisseur ligneCommandeFournisseur : ligneCommandeFournisseurList) {
                ligneCommandeFournisseur.setArticle(ligneCommandeFournisseur.getPrixHistorique().getArticle());
                ligneCommandeFournisseur.setPrixUnitaire(ligneCommandeFournisseur.getPrixHistorique().getPrix());
                ligneCommandeFournisseur.setPrixLigne(ligneCommandeFournisseur.getPrixUnitaire() * ligneCommandeFournisseur.getQuantite());
            }
        }
    }

    private void updateCommandeFournisseur(CommandeFournisseur commandeFournisseur) {
        if (commandeFournisseur != null) {
            List<LigneCommandeFournisseur> ligneCommandeFournisseurList = commandeFournisseur.getLignesCommandeFournisseur();
            for (LigneCommandeFournisseur ligneCommandeFournisseur : ligneCommandeFournisseurList) {
                updateArticleQuantite(ligneCommandeFournisseur, commandeFournisseur.getStatutCommande());
            }
        }
    }

    private void updateArticleQuantite(LigneCommandeFournisseur ligneCommandeFournisseur, StatutCommande statutCommandeFournisseur) {
        Article article = ligneCommandeFournisseur.getArticle();
        Integer quantite = ligneCommandeFournisseur.getQuantite();
        Optional<Article> foundArticle = articleRepository.findById(article.getId());
        if (foundArticle.isPresent() && quantite > 0) {
            article = foundArticle.get();
            if (statutCommandeFournisseur == StatutCommande.CREE) {
                article.setQuantitePrevisionnelle(article.getQuantitePrevisionnelle() + quantite);
            } else if (statutCommandeFournisseur == StatutCommande.LIVRE) {
                article.setQuantite(article.getQuantite() + quantite);
            } else if (statutCommandeFournisseur == StatutCommande.ANNULE) {
                article.setQuantitePrevisionnelle(article.getQuantitePrevisionnelle() - quantite);
            } else if (statutCommandeFournisseur == StatutCommande.RETOUR) {
                article.setQuantite(article.getQuantite() - quantite);
                article.setQuantitePrevisionnelle(article.getQuantitePrevisionnelle() - quantite);
            }
            articleRepository.save(article);
        }
    }
}
