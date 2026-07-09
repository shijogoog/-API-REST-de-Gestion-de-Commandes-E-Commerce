package kingo_ecom.entity.enums;

public enum OrderStatus {
    PENDING,    // Commande créée, en attente de paiement
    PAID,       // Paiement confirmé
    FAILED,     // Paiement échoué
    SHIPPED,    // Commande expédiée
    DELIVERED,  // Commande livrée
    CANCELLED   // Commande annulée
}