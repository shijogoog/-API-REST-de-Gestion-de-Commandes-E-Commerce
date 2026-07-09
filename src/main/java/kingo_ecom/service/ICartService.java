package kingo_ecom.service;
import java.util.List;
import java.util.UUID;

import kingo_ecom.dto.cart.CartItemRequestDTO;
import kingo_ecom.dto.cart.CartItemResponseDTO;


public interface ICartService {
    List<CartItemResponseDTO> getCart();

    void addItemToCart(CartItemRequestDTO request);

    void updateCartItem(UUID productId, CartItemRequestDTO request);

    void removeCartItem(UUID productId);
}
