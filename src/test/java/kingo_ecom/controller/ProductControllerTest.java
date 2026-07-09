package kingo_ecom.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kingo_ecom.dto.product.ProductResponseDTO;
import kingo_ecom.exception.ResourceNotFoundException;
import kingo_ecom.service.IProductService;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IProductService productService;

    @Test
    void shouldReturnProducts() throws Exception {

        ProductResponseDTO product = new ProductResponseDTO(
                UUID.randomUUID(),
                "Smartphone",
                "Très bon téléphone",
                new BigDecimal("999.99"),
                50
        );

        Page<ProductResponseDTO> page = new PageImpl<>(List.of(product));

        when(productService.getProducts(org.springframework.data.domain.Pageable.unpaged()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnProductById() throws Exception {

        UUID id = UUID.randomUUID();

        ProductResponseDTO product = new ProductResponseDTO(
                id,
                "Smartphone",
                "Très bon téléphone",
                new BigDecimal("999.99"),
                50
        );

        when(productService.getProductById(id))
                .thenReturn(product);

        mockMvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Smartphone"));
    }

    @Test
    void shouldReturn400WhenIdIsInvalidUUID() throws Exception {

        mockMvc.perform(get("/api/v1/products/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {

        UUID id = UUID.randomUUID();

        when(productService.getProductById(id))
                .thenThrow(new ResourceNotFoundException("Produit introuvable"));

        mockMvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isNotFound());
    }

}