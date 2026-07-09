package kingo_ecom.dto.product;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
 import lombok.AllArgsConstructor;
 import lombok.Data;
 import lombok.NoArgsConstructor;

 @Data
 @NoArgsConstructor
 @AllArgsConstructor
// import lombok.Data; // Si vous utilisez Lombok

// @Data
public class ProductRequestDTO {

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(max = 255, message = "Le nom du produit ne peut pas dépasser 255 caractères")
    private String name;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", inclusive = true, message = "Le prix doit être strictement supérieur à 0")
    private BigDecimal price;

    @NotNull(message = "La quantité en stock est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stockQuantity;

    // L'ID de la catégorie est un UUID pour respecter la règle des PK/FK
    @NotNull(message = "La catégorie est obligatoire")
    private UUID categoryId;

    private String imageUrl; // Optionnel, URL de l'image du produit

}