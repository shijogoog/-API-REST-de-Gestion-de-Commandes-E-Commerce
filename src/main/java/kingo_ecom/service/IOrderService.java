package kingo_ecom.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kingo_ecom.dto.order.OrderRequestDTO;
import kingo_ecom.dto.order.OrderResponseDTO;

public interface IOrderService {
    OrderResponseDTO checkout(OrderRequestDTO request);
    Page<OrderResponseDTO> getOrderHistory(Pageable pageable);
    OrderResponseDTO getOrderById(UUID id);
}