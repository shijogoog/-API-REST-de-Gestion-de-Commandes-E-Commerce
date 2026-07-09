package kingo_ecom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kingo_ecom.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    // Récupérer tous les articles d'un panier spécifique (pour afficher le panier)
    List<CartItem> findByCartId(UUID cartId);

    // Vérifier si un produit est déjà dans le panier (pour incrémenter la quantité au lieu de créer un doublon)
    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

    // Vider le panier (supprimer tous les articles) après une commande validée
    void deleteByCartId(UUID cartId);
}