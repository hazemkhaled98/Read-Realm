package com.readrealm.payment.gateway.stripe;


import com.readrealm.payment.gateway.WebhookPaymentGateway;
import com.readrealm.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/stripe")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookPaymentGateway stripePaymentGateway;
    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebHook(
            @Valid @RequestBody String webhookPayload,
            @RequestHeader("Stripe-Signature") String stripeSignature
    ) {
        stripePaymentGateway.handleWebhook(paymentService, webhookPayload, stripeSignature);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
