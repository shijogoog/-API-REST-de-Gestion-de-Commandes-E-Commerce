package kingo_ecom.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kingo_ecom.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    // RÈGLE 4.1 : Clé primaire UUID (héritée de JpaRepository<User, UUID>)
    
    // Indispensable pour le filtre JWT / Spring Security
    Optional<User> findByEmail(String email);

    // Vérifier si un email est déjà utilisé lors de l'inscription
    boolean existsByEmail(String email);
}