package kingo_ecom.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kingo_ecom.entity.Cart;


public interface CartRepository extends JpaRepository<Cart, UUID> {

    // RÈGLE 4.1 : Clé primaire UUID
    
    // Retrouver le panier actif d'un utilisateur spécifique
    Optional<Cart> findByUserId(UUID userId);
    
    // Optionnel : supprimer le panier d'un utilisateur (utile après une commande validée ou un nettoyage)
    void deleteByUserId(UUID userId);
}