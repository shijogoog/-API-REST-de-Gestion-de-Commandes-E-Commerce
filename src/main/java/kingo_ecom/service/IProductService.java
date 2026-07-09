package kingo_ecom.service;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kingo_ecom.dto.product.ProductResponseDTO;
public interface IProductService {
    Page<ProductResponseDTO> getProducts(Pageable pageable);

    ProductResponseDTO getProductById(UUID id);
}

