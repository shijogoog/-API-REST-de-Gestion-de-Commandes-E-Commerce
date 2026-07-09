package kingo_ecom.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional; // Enum à créer (PENDING, PAID, FAILED)
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kingo_ecom.entity.Order;
import kingo_ecom.entity.enums.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    // RÈGLE 4.1 : Clé primaire UUID (héritée de JpaRepository<Order, UUID>)

    // Récupérer l'historique des commandes d'un utilisateur spécifique
Page<Order> findByUserId(UUID userId, Pageable pageable);
    // RÈGLE 6 (Module Paiement) : Trouver les commandes par statut
    // Indispensable pour le module Dummy qui doit traiter les commandes PENDING
    List<Order> findByStatus(OrderStatus status);

    // Optionnel : Vérifier si une commande appartient bien à un utilisateur (Sécurité)
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);
}