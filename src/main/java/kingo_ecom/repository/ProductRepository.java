package kingo_ecom.repository;


import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import kingo_ecom.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * RÈGLE 4.4 : Verrou Pessimiste (SELECT ... FOR UPDATE)
     * Bloque la ligne en base de données pendant la transaction pour éviter 
     * les Race Conditions lors de la vérification/décrément du stock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithPessimisticLock(@Param("id") UUID id);

    // Méthode standard pour les lectures simples (catalogue) sans verrou
    // Optional<Product> findById(UUID id); // Hérité de JpaRepository
}