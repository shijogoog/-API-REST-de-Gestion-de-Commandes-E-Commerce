package kingo_ecom.dto.order;

import java.math.BigDecimal;
import java.util.UUID;

 import lombok.AllArgsConstructor;
 import lombok.Data;
 import lombok.NoArgsConstructor;

 @Data
 @NoArgsConstructor
 @AllArgsConstructor
public class OrderItemResponseDTO {
    private UUID productId;
    private String productName;
    private BigDecimal priceAtPurchase; // Figé au moment de l'achat
    private Integer quantity;
   
}