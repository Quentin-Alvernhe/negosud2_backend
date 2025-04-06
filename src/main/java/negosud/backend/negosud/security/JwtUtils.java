package negosud.backend.negosud.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import negosud.backend.negosud.entity.StatutUtilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import negosud.backend.negosud.repository.UtilisateurRepository;
import java.time.LocalDateTime;
import java.util.Date;

@Service
public class JwtUtils {
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public String generateToken (AppUserDetails userDetails) {

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, "azeazeaze")
                .claim("Nom", userDetails.getPrenom())
                .claim("Prenom", userDetails.getNom())
                .claim("Role", userDetails.getRole())
                .compact();

    }

    public String getSubject (String token) {
        return Jwts.parser()
                .setSigningKey("azeazeaze")
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateUser (UserDetails userDetails) {
        if (userDetails.getUsername() == null) {
            return false;
        }
        if (utilisateurRepository.findByEmail(userDetails.getUsername()).get().getRefreshTokenHorodatageExpiration() != null){
            if (utilisateurRepository.findByEmail(userDetails.getUsername()).get().getRefreshTokenHorodatageExpiration().isBefore(LocalDateTime.now())) {
                return false;
            }
        } else if (utilisateurRepository.findByEmail(userDetails.getUsername()).get().getStatut() == StatutUtilisateur.VEROUILLE) {

        }
        return true;
    }

}
