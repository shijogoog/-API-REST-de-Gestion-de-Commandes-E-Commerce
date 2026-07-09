package kingo_ecom.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kingo_ecom.dto.order.OrderItemResponseDTO;
import kingo_ecom.dto.order.OrderRequestDTO;
import kingo_ecom.dto.order.OrderResponseDTO;
import kingo_ecom.entity.Cart;
import kingo_ecom.entity.CartItem;
import kingo_ecom.entity.Order;
import kingo_ecom.entity.OrderItem;
import kingo_ecom.entity.Product;
import kingo_ecom.entity.User;
import kingo_ecom.entity.enums.OrderStatus;
import kingo_ecom.exception.PaymentFailedException;
import kingo_ecom.exception.ResourceNotFoundException;
import kingo_ecom.exception.StockInsuffisantException;
import kingo_ecom.exception.UnauthorizedException;
import kingo_ecom.payment.PaymentRequest;
import kingo_ecom.payment.PaymentResponse;
import kingo_ecom.payment.PaymentService;
import kingo_ecom.repository.CartItemRepository;
import kingo_ecom.repository.CartRepository;
import kingo_ecom.repository.OrderItemRepository;
import kingo_ecom.repository.OrderRepository;
import kingo_ecom.repository.ProductRepository;
import kingo_ecom.service.IOrderService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional(noRollbackFor = PaymentFailedException.class)
    public OrderResponseDTO checkout(OrderRequestDTO request) {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        if (cart.getItems().isEmpty()) {
            throw new StockInsuffisantException("Le panier est vide, impossible de commander");
        }

        // 1. Création de la commande avec statut PENDING
        Order order = new Order();
        order.setUser(currentUser);
        order.setStatus(OrderStatus.PENDING);
order.setPaymentMethod(kingo_ecom.entity.enums.PaymentMethod.valueOf(request.getPaymentMethod()));        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 2. Traitement des articles avec Verrou Pessimiste
        for (CartItem cartItem : cart.getItems()) {
            // Utilisation du verrou pessimiste pour éviter les race conditions
            Product product = productRepository.findByIdWithPessimisticLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new StockInsuffisantException("Stock insuffisant pour le produit: " + product.getName());
            }

            // Déduction du stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Création de l'OrderItem avec le prix figé (Snapshot)
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setPriceAtPurchase(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItems.add(orderItem);

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        orderItemRepository.saveAll(orderItems);
        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);
        orderRepository.save(order);

        // 3. Vidage du panier
        cartItemRepository.deleteAll(cart.getItems());

        // 4. Appel au module de paiement découplé
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(order.getId());
        paymentRequest.setAmount(totalAmount);
        paymentRequest.setPaymentMethod(request.getPaymentMethod());

        PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

        // 5. Gestion déterministe du statut et rollback si échec
        if (paymentResponse.isSuccess()) {
            order.setStatus(OrderStatus.PAID);
            // order.setTransactionId(paymentResponse.getTransactionId()); // Si le champ
            // existe
        } else {
            order.setStatus(OrderStatus.FAILED);
            restoreStock(orderItems); // Rollback automatique des stocks
            throw new PaymentFailedException("Échec du paiement: " + paymentResponse.getFailureReason());
        }

        orderRepository.save(order);
        return mapToDTO(order);
    }

    @Override
    public Page<OrderResponseDTO> getOrderHistory(Pageable pageable) {
        User currentUser = getCurrentUser();
        return orderRepository.findByUserId(currentUser.getId(), pageable).map(this::mapToDTO);
    }

    @Override
    public OrderResponseDTO getOrderById(UUID id) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée ou accès non autorisé"));
        return mapToDTO(order);
    }

    private void restoreStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProduct().getId()).orElseThrow();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Utilisateur non authentifié");
        }
        return (User) authentication.getPrincipal();
    }

    private OrderResponseDTO mapToDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
dto.setPaymentMethod(order.getPaymentMethod().name()); 
// Ou .toString() selon comment ton Enum est structuré        dto.setCreatedAt(order.getCreatedAt());
        dto.setItems(order.getItems().stream().map(this::mapItemToDTO).collect(Collectors.toList()));
        return dto;
    }

    private OrderItemResponseDTO mapItemToDTO(OrderItem item) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setPriceAtPurchase(item.getPriceAtPurchase());
        dto.setQuantity(item.getQuantity());
        return dto;
    }
}
