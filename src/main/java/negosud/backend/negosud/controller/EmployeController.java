package negosud.backend.negosud.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import negosud.backend.negosud.RequestValidators.EmployePost;
import negosud.backend.negosud.RequestValidators.EmployePut;
import negosud.backend.negosud.View.EmployeView;
import negosud.backend.negosud.entity.Employe;
import negosud.backend.negosud.entity.Role;
import negosud.backend.negosud.repository.EmployeRepository;
import negosud.backend.negosud.security.IsAdministrateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/utilisateur/employe")
@Tag(name = "Employé", description = "Opérations concernant les employés.")
@JsonView(EmployeView.class)
public class EmployeController {
    private final EmployeRepository employeRepository;
    private final BCryptPasswordEncoder encoder;

    @Autowired
    public EmployeController(EmployeRepository employeRepository, BCryptPasswordEncoder encoder) {
        this.employeRepository = employeRepository;
        this.encoder = encoder;
    }

    @IsAdministrateur
    @GetMapping("/all")
    @Operation(
            summary = "Récupérer les employés",
            description = "Récupère la liste de tous les employés avec leur rôle."
    )
    @JsonView(EmployeView.class)
    public List<Employe> getAllEmploye() {
        return employeRepository.findAll();
    }

    @IsAdministrateur
    @PostMapping("/new")
    @Operation(
            summary = "Nouvel employé",
            description = "Création d'un nouvel employé avec le rôle \"EMPLOYE\" par défaut."
    )
    @JsonView(EmployeView.class)
    public ResponseEntity<Employe> addEmploye(
            @Validated(EmployePost.class) @RequestPart("employe") @Valid Employe nouvelEmploye,
            @Nullable @RequestPart("photo") MultipartFile photo) {
        if (nouvelEmploye == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if (nouvelEmploye.getRole() == null) {
            // Assign the default role to the employe
            nouvelEmploye.setRole(Role.EMPLOYE);
        }
        if (employeRepository.existsByEmail(nouvelEmploye.getEmail())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        nouvelEmploye.setMotDePasse(encoder.encode(nouvelEmploye.getMotDePasse()));
        savePhoto(nouvelEmploye, photo);
        employeRepository.save(nouvelEmploye);
        return new ResponseEntity<>(nouvelEmploye, HttpStatus.CREATED);
    }

    @IsAdministrateur
    @PutMapping("/{id}/edit")
    @Operation(
            summary = "Modification d'un employé",
            description = "Modification des informations de l'employé excepté celle pour le mot de passe."
    )
    @JsonView(EmployeView.class)
    public ResponseEntity<Employe> updateEmploye(
            @PathVariable UUID id,
            @Validated(EmployePut.class) @RequestPart("employe") @Valid Employe modifiedEmploye,
            @Nullable @RequestPart("photo") MultipartFile photo) {
        if (modifiedEmploye == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<Employe> foundEmploye = employeRepository.findById(id);
        if (foundEmploye.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Employe employeDB = foundEmploye.get();
        modifiedEmploye.setId(employeDB.getId());
        modifiedEmploye.setEmail(employeDB.getEmail());
        modifiedEmploye.setRole(employeDB.getRole());
        modifiedEmploye.setMotDePasse(employeDB.getMotDePasse());
        modifiedEmploye.setHorodatageCreation(employeDB.getHorodatageCreation());
        modifiedEmploye.setStatut(employeDB.getStatut());
        modifiedEmploye.setRefreshTokenHorodatageExpiration(employeDB.getRefreshTokenHorodatageExpiration());

        editPhoto(employeDB, modifiedEmploye, photo);

        employeRepository.save(modifiedEmploye);
        return ResponseEntity.ok(employeDB);
    }

    public void savePhoto(Employe employe, MultipartFile photo) {
        if (photo != null) {
            try {
                // Define the path where you want to save the file
                String directoryPath = "C://negosud/images/employes/";
                String fileName = UUID.randomUUID() + ".jpg";
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
                employe.setPhotoReference("/images/employes/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void editPhoto(Employe employeDB, Employe modifiedEmploye, MultipartFile photo) {
        if (!employeDB.getPhotoReference().isEmpty() && employeDB.getPhotoReference() != null) {
            Path filePath = Path.of("C://negosud/images/" + employeDB.getPhotoReference());
            if (photo != null) {
                try {
                    if (Files.exists(filePath)) {
                        if (isSamePhoto(photo, String.valueOf(filePath))) {
                            modifiedEmploye.setPhotoReference(employeDB.getPhotoReference());
                            return;
                        } else {
                            deletePhoto(employeDB);
                        }
                    }
                    savePhoto(modifiedEmploye, photo);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            } else {
                if (Files.exists(filePath)) {
                    deletePhoto(employeDB);
                    modifiedEmploye.setPhotoReference("");
                }
            }
        } else {
            savePhoto(modifiedEmploye, photo);
        }
    }

    public void deletePhoto(Employe employe) {
        File file = new File("C://negosud" + employe.getPhotoReference());
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