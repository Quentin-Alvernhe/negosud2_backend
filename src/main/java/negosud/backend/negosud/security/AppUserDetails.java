package negosud.backend.negosud.security;

import negosud.backend.negosud.entity.Client;
import negosud.backend.negosud.entity.Employe;
import negosud.backend.negosud.entity.StatutUtilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AppUserDetails implements UserDetails {

    private Client client;
    private Employe employe;

    private String email;
    private String motDePasse;
    private Boolean enabled;
    private Collection<? extends GrantedAuthority> role;
    private String Prenom;
    private String Nom;

    public AppUserDetails(Client client) {
        this.client = client;
        this.email = client.getEmail();
        this.motDePasse = client.getMotDePasse();
        this.enabled = client.getStatut().equals(StatutUtilisateur.ACTIF);
        this.role = getAuthorities();
        this.Prenom = client.getPrenom();
        this.Nom = client.getNom();
    }
    public AppUserDetails(Employe employe) {
        this.employe = employe;
        this.email = employe.getEmail();
        this.motDePasse = employe.getMotDePasse();
        this.enabled = employe.getStatut().equals(StatutUtilisateur.ACTIF);
        this.role = getAuthorities();
        this.Prenom = employe.getPrenom();
        this.Nom = employe.getNom();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        // Si c'est un client, il a le rôle CLIENT, sinon le nom de rôle vient de la table ROLE liée à employé
        if (client != null) {
            return List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.employe.getRole()));
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Client getClient() {
        return client;
    }

    public String getPrenom() {
        return Prenom;
    }

    public String getNom() {
        return Nom;
    }

    public Collection<? extends GrantedAuthority> getRole() {
        return role;
    }
}
