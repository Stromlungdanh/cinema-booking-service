package com.cinema.booking.payment;

import com.cinema.booking.payment.dto.PaymentRequest;
import com.cinema.booking.payment.dto.PaymentResponse;
import com.cinema.booking.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> pay(@AuthenticationPrincipal UserPrincipal principal,
                                                 @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.pay(principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
