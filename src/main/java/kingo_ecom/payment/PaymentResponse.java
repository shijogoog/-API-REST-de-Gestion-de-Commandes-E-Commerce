package kingo_ecom.payment;

import lombok.Data;

@Data
public class PaymentResponse {
    private boolean success;
    private String transactionId;
    private String failureReason;
}