package kingo_ecom.controller;

import kingo_ecom.dto.cart.CartItemRequestDTO;
import kingo_ecom.dto.cart.CartItemResponseDTO;
import kingo_ecom.service.ICartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @GetMapping
    public ResponseEntity<List<CartItemResponseDTO>> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/items")
    public ResponseEntity<Void> addItem(@Valid @RequestBody CartItemRequestDTO request) {
        cartService.addItemToCart(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<Void> updateItem(@PathVariable UUID productId,
            @Valid @RequestBody CartItemRequestDTO request) {
        cartService.updateCartItem(productId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID productId) {
        cartService.removeCartItem(productId);
        return ResponseEntity.noContent().build();
    }
}