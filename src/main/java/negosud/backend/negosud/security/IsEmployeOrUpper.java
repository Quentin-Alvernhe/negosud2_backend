package negosud.backend.negosud.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
// Définition de la cible de l'annotation, qui peut être un type (classe, interface) ou une méthode
@Target({ElementType.METHOD, ElementType.TYPE})
// L'annotation sera disponible au moment de l'exécution
@Retention(RetentionPolicy.RUNTIME)
// Donner l'accès aux utilisateurs ayant le rôle 'ROLE_ADMINISTRATEUR'
@PreAuthorize("hasAnyRole('ROLE_EMPLOYE', 'ROLE_ADMINISTRATEUR')")
public @interface IsEmployeOrUpper {
}