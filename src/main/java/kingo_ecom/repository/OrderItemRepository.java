package kingo_ecom.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kingo_ecom.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    // RÈGLE 4.1 : Clé primaire UUID

    // Récupérer le détail (les articles) d'une commande spécifique
    // Permet de reconstruire le panier finalisé avec les prix figés (price_at_purchase)
    List<OrderItem> findByOrderId(UUID orderId);
}