package kingo_ecom.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import kingo_ecom.dto.order.OrderRequestDTO;
import kingo_ecom.dto.order.OrderResponseDTO;
import kingo_ecom.entity.Cart;
import kingo_ecom.entity.CartItem;
import kingo_ecom.entity.Order;
import kingo_ecom.entity.Product;
import kingo_ecom.entity.User;
import kingo_ecom.entity.enums.OrderStatus;
import kingo_ecom.exception.StockInsuffisantException;
import kingo_ecom.payment.PaymentRequest;
import kingo_ecom.payment.PaymentResponse;
import kingo_ecom.payment.PaymentService;
import kingo_ecom.repository.CartItemRepository;
import kingo_ecom.repository.CartRepository;
import kingo_ecom.repository.OrderItemRepository;
import kingo_ecom.repository.OrderRepository;
import kingo_ecom.repository.ProductRepository;
import kingo_ecom.service.impl.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository; // Ajouté car utilisé dans checkout()

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository; // Ajouté pour le vidage du panier

    @Mock
    private PaymentService paymentService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User mockUser;
    private Cart mockCart;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        // Init de l'utilisateur simulé pour le SecurityContextHolder
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("lead@kingo.com");

        // Configuration globale du Mock SecurityContext pour le cast User
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(mockUser);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        // Init un produit standard
        mockProduct = new Product();
        mockProduct.setId(UUID.randomUUID());
        mockProduct.setName("Smartphone Kingo");
        mockProduct.setPrice(new BigDecimal("150000.00"));
        mockProduct.setStockQuantity(10); // Correction du nom de l'attribut
    }

    @Test
    void checkout_ShouldSucceed_WhenStockAndPaymentAreValid() {
        // Arrange
        OrderRequestDTO request = new OrderRequestDTO();
        request.setPaymentMethod("MOBILE_MONEY");

        CartItem cartItem = new CartItem();
        cartItem.setProduct(mockProduct);
        cartItem.setQuantity(2); // Demande 2 articles, stock dispo = 10

        mockCart = new Cart();
        mockCart.setId(UUID.randomUUID());
        mockCart.setUser(mockUser);
        mockCart.setItems(new ArrayList<>());
        mockCart.getItems().add(cartItem);

        // 1. Mock du Panier (avec l'ID de l'utilisateur)
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        
        // 2. Mock du Produit (Verrou Pessimiste)
        when(productRepository.findByIdWithPessimisticLock(mockProduct.getId())).thenReturn(Optional.of(mockProduct));
        
        // 3. Mock du Service de Paiement
        PaymentResponse mockPaymentResponse = new PaymentResponse();
        mockPaymentResponse.setSuccess(true);
        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(mockPaymentResponse);
        
        // 4. Mock de la sauvegarde (Retourne l'objet qu'on lui passe)
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponseDTO response = orderService.checkout(request); // Utilisation de checkout()

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.PAID.name(), response.getStatus());
        assertEquals(new BigDecimal("300000.00"), response.getTotalAmount()); // 150000 * 2
        assertEquals(8, mockProduct.getStockQuantity()); // 10 - 2 = 8
        
        // Vérification que les articles du panier ont bien été supprimés
        verify(cartItemRepository, times(1)).deleteAll(mockCart.getItems());
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void checkout_ShouldThrowStockInsuffisantException_WhenStockIsLow() {
        // Arrange
        OrderRequestDTO request = new OrderRequestDTO();
        request.setPaymentMethod("CASH_ON_DELIVERY");

        CartItem cartItem = new CartItem();
        cartItem.setProduct(mockProduct);
        cartItem.setQuantity(15); // 15 demandés alors qu'il n'y a que 10 en stock

        mockCart = new Cart();
        mockCart.setId(UUID.randomUUID());
        mockCart.setUser(mockUser);
        mockCart.setItems(new ArrayList<>());
        mockCart.getItems().add(cartItem);

        // Mock avec mockUser.getId()
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(productRepository.findByIdWithPessimisticLock(mockProduct.getId())).thenReturn(Optional.of(mockProduct));

        // Act & Assert
        assertThrows(StockInsuffisantException.class, () -> {
            orderService.checkout(request); // Utilisation de checkout()
        });

        // Le stock ne doit pas avoir bougé
        assertEquals(10, mockProduct.getStockQuantity());
// On vérifie que la commande a été sauvegardée exactement 1 fois (pour le statut PENDING initial)
// mais pas plus (la transaction s'arrête suite à l'exception)
verify(orderRepository, times(1)).save(any(Order.class));    }
}