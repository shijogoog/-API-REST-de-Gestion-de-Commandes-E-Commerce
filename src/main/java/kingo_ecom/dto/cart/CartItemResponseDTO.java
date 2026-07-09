package kingo_ecom.dto.cart;

import java.math.BigDecimal;
import java.util.UUID;

 import lombok.AllArgsConstructor;
 import lombok.Data;
 import lombok.NoArgsConstructor;

 @Data
 @NoArgsConstructor
 @AllArgsConstructor

public class CartItemResponseDTO {
    private UUID productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice; // <-- AJOUTER CETTE LIGNE
    private BigDecimal subtotal;
    
    
}