package kingo_ecom.payment;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class DummyPaymentServiceImpl implements PaymentService {

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        // Simulation de latence réseau (ex: attente de réponse de l'opérateur Mobile
        // Money)
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        PaymentResponse response = new PaymentResponse();
        response.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Logique déterministe pour la simulation :
        // Si le montant est supérieur à 5000, on simule un échec (fonds insuffisants)
        if (request.getAmount().compareTo(new BigDecimal("5000")) > 0) {
            response.setSuccess(false);
            response.setFailureReason("Fonds insuffisants sur le compte Mobile Money");
        }
        // Le paiement à la livraison est toujours "validé" à la commande (statut
        // PENDING maintenu jusqu'à livraison)
        else if ("CASH_ON_DELIVERY".equals(request.getPaymentMethod())) {
            response.setSuccess(true);
        } else {
            response.setSuccess(true);
        }

        return response;
    }
}