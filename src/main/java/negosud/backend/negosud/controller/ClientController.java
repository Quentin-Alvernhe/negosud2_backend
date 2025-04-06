package negosud.backend.negosud.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import negosud.backend.negosud.RequestValidators.ClientPost;
import negosud.backend.negosud.RequestValidators.ClientPut;
import negosud.backend.negosud.View.AdresseListeView;
import negosud.backend.negosud.View.ClientView;
import negosud.backend.negosud.entity.Client;
import negosud.backend.negosud.entity.CommandeClient;
import negosud.backend.negosud.repository.ClientRepository;
import negosud.backend.negosud.security.AppUserDetails;
import negosud.backend.negosud.security.IsClientAndNotEmploye;
import negosud.backend.negosud.security.IsConnected;
import negosud.backend.negosud.security.IsEmployeOrUpper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RestController
@RequestMapping("/api/utilisateur/client")
@Tag(name = "Client", description = "Opérations concernant les clients.")
public class ClientController {
    private ClientRepository clientRepository;
    private BCryptPasswordEncoder encoder;

    @Autowired
    public ClientController(ClientRepository clientRepository, BCryptPasswordEncoder encoder) {
        this.clientRepository = clientRepository;
        this.encoder = encoder;
    }

    @IsEmployeOrUpper
    @GetMapping("/all")
    @Operation(
            summary = "Liste les clients",
            description = "Récupère la liste de tous les clients."
    )
    @JsonView(ClientView.class)
    public List<Client> getAllClient() {
        return clientRepository.findAll();
    }

    @PostMapping("/new")
    @Operation(
            summary = "Nouveau client",
            description = "Création d'un nouveau client."
    )
    @JsonView({ClientView.class, AdresseListeView.class})
    public ResponseEntity<Client> addClient(
            @Validated(ClientPost.class) @RequestPart("client") @Valid Client nouveauClient,
            @Nullable @RequestPart("photo") MultipartFile photo) {
        if (nouveauClient == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (clientRepository.existsByEmail(nouveauClient.getEmail())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        //Hash du mot de passe
        nouveauClient.setMotDePasse(encoder.encode(nouveauClient.getMotDePasse()));

        savePhoto(nouveauClient, photo);

        clientRepository.save(nouveauClient);
        return new ResponseEntity<>(nouveauClient, HttpStatus.CREATED);
    }

    @IsConnected
    @PutMapping("/{id}/edit")
    @Operation(
            summary = "Modification d'un client",
            description = "Modification des informations du client excepté pour le mot de passe."
    )
    @JsonView({ClientView.class, AdresseListeView.class})
    public ResponseEntity<Client> updateClient(
                                           @PathVariable UUID id,
                                           @Validated(ClientPut.class) @RequestPart("client") @Valid Client modifiedClient,
                                           @Nullable @RequestPart("photo") MultipartFile photo) {
        if (modifiedClient == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Vérification de l'existence du client en bdd
        Optional<Client> foundClient = clientRepository.findById(id);
        if (foundClient.isEmpty()) {
            return new ResponseEntity<>(modifiedClient, HttpStatus.NOT_FOUND);
        }

        Client clientDB = foundClient.get();

        editPhoto(clientDB, modifiedClient, photo);

        modifiedClient.setId(foundClient.get().getId());
        modifiedClient.setMotDePasse(clientDB.getMotDePasse());
        modifiedClient.setAdresses(clientDB.getAdresses());
        modifiedClient.setHorodatageCreation(clientDB.getHorodatageCreation());
        modifiedClient.setStatut(clientDB.getStatut());

        clientRepository.save(modifiedClient);
        return ResponseEntity.ok(modifiedClient);
    }

    @IsClientAndNotEmploye
    @GetMapping("/commandes")
    @Operation(
            summary = "Récupération des commandes d'un client",
            description = "Modification des informations du client excepté pour le mot de passe."
    )
    @JsonView(CommandeClient.class)
    public ResponseEntity<List<CommandeClient>> getCommandesClient(
            @AuthenticationPrincipal AppUserDetails client) {
        if (client == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Vérification de l'existence du client en bdd
        Optional<Client> foundClient = clientRepository.findById(client.getClient().getId());
        if (foundClient.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Client clientDB = foundClient.get();

        List<CommandeClient> commandesClient = clientDB.getCommandesClient();

        return ResponseEntity.ok(commandesClient);
    }

    public void savePhoto(Client client, MultipartFile photo) {
        if (photo != null) {
            try {
                // Define the path where you want to save the file
                String directoryPath = "C://negosud/images/clients/";
                String fileName = UUID.randomUUID().toString() + ".jpg";
                String filePath = directoryPath + fileName;
                File file = new File(filePath);

                // Create the directory if it doesn't exist
                file.getParentFile().mkdirs();

                // Write the file to the specified path
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(photo.getBytes());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                client.setPhotoReference("/images/clients/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void editPhoto(Client clientDB, Client modifiedClient, MultipartFile photo) {
        if (!clientDB.getPhotoReference().isEmpty() || clientDB.getPhotoReference() != null) {
            Path filePath = Path.of("C://negosud/images/" + clientDB.getPhotoReference());
            if (photo != null) {
                try {
                    if (clientDB.getPhotoReference() != null) {
                        if (Files.exists(filePath)) {
                            if (isSamePhoto(photo, String.valueOf(filePath))) {
                                modifiedClient.setPhotoReference(clientDB.getPhotoReference());
                                return;
                            } else {
                                deletePhoto(clientDB);
                            }
                        }
                    }
                    savePhoto(clientDB, photo);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            } else {
                if (Files.exists(filePath)) {
                    deletePhoto(clientDB);
                    modifiedClient.setPhotoReference("");
                }
            }
        } else {
            savePhoto(modifiedClient, photo);
        }
    }

    public void deletePhoto(Client client) {
        File file = new File("C://negosud" + client.getPhotoReference());
        file.delete();
    }

    public boolean isSamePhoto(MultipartFile receivedFile, String existingFilePath) throws IOException, NoSuchAlgorithmException {
        byte[] receivedHash = computeHash(receivedFile.getBytes());
        byte[] existingHash = computeHash(Files.readAllBytes(Path.of(existingFilePath)));

        return Arrays.equals(receivedHash, existingHash);
    }

    private byte[] computeHash(byte[] fileData) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(fileData);
    }

}