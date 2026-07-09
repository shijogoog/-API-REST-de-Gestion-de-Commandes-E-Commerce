package kingo_ecom.controller;

import kingo_ecom.dto.order.OrderRequestDTO;
import kingo_ecom.dto.order.OrderResponseDTO;
import kingo_ecom.service.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> checkout(@Valid @RequestBody OrderRequestDTO request) {
        return new ResponseEntity<>(orderService.checkout(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getOrderHistory(Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrderHistory(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}