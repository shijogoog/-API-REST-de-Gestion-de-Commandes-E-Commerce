package kingo_ecom.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentRequest {
    private UUID orderId;
    private BigDecimal amount;
    private String paymentMethod;
}