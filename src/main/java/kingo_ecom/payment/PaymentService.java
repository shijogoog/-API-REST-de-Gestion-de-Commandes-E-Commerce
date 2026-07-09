package kingo_ecom.payment;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
}
