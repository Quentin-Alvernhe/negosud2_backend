package negosud.backend.negosud.security;

import negosud.backend.negosud.entity.Client;
import negosud.backend.negosud.entity.Employe;
import negosud.backend.negosud.repository.ClientRepository;
import negosud.backend.negosud.repository.EmployeRepository;
import negosud.backend.negosud.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Client> optionalClient = clientRepository.findByEmail(email);

        if (optionalClient.isEmpty()) {
            Optional<Employe> optionalEmploye = employeRepository.findByEmail(email);

            // Si l'utilisateur n'est ni client, ni employé cela signifie qu'il n'existe pas
            if (optionalEmploye.isEmpty()) {
                throw new UsernameNotFoundException("Email introuvable: " + email);
            }

            return new AppUserDetails(optionalEmploye.get());
        } else {
            return new AppUserDetails(optionalClient.get());
        }
    }
}
