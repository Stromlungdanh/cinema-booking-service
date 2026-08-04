package com.cinema.booking.payment;

import com.cinema.booking.booking.Booking;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, length = 30)
    private String method = "FAKE_GATEWAY";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    // Client tu sinh (VD: UUID) va gui lai nguyen ven neu goi lai API do
    // mang lag/bam nut 2 lan - PaymentService dung cot UNIQUE nay de tra ve
    // ket qua cu thay vi tao payment/thanh toan trung.
    @Column(name = "idempotency_key", nullable = false, length = 100, unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
