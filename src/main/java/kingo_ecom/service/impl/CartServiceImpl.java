package kingo_ecom.service.impl;

import kingo_ecom.dto.cart.CartItemRequestDTO;
import kingo_ecom.dto.cart.CartItemResponseDTO;
import kingo_ecom.entity.Cart;
import kingo_ecom.entity.CartItem;
import kingo_ecom.entity.Product;
import kingo_ecom.entity.User;
import kingo_ecom.exception.ResourceNotFoundException;
import kingo_ecom.exception.StockInsuffisantException;
import kingo_ecom.exception.UnauthorizedException;
import kingo_ecom.repository.CartItemRepository;
import kingo_ecom.repository.CartRepository;
import kingo_ecom.repository.ProductRepository;
import kingo_ecom.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    public List<CartItemResponseDTO> getCart() {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);
        return cart.getItems().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addItemToCart(CartItemRequestDTO request) {
        User currentUser = getCurrentUser();
        Cart cart = getOrCreateCart(currentUser);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            validateStock(item);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            validateStock(newItem);
            cartItemRepository.save(newItem);
        }
    }

    @Override
    @Transactional
    public void updateCartItem(UUID productId, CartItemRequestDTO request) {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé dans le panier"));

        item.setQuantity(request.getQuantity());
        validateStock(item);
        cartItemRepository.save(item);
    }

    @Override
    @Transactional
    public void removeCartItem(UUID productId) {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé dans le panier"));

        cartItemRepository.delete(item);
    }

    private void validateStock(CartItem item) {
        if (item.getProduct().getStockQuantity() < item.getQuantity()) {
            throw new StockInsuffisantException("Stock insuffisant pour le produit: " + item.getProduct().getName());
        }
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Utilisateur non authentifié");
        }
        return (User) authentication.getPrincipal();
    }

    private CartItemResponseDTO mapToDTO(CartItem item) {
        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setUnitPrice(item.getProduct().getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        return dto;
    }
}
