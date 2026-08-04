package com.cinema.booking.payment;

import com.cinema.booking.booking.Booking;
import com.cinema.booking.booking.BookingRepository;
import com.cinema.booking.booking.BookingStatus;
import com.cinema.booking.common.exception.BookingConflictException;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.common.util.TicketCodeGenerator;
import com.cinema.booking.payment.dto.PaymentRequest;
import com.cinema.booking.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    // Gia lap gateway thanh toan: khong goi ra ngoai, luon SUCCESS ngay -
    // muc tieu la co du luong PENDING -> PAID + sinh ve/QR, chua phai tich
    // hop payment that (de danh Giai doan 3).
    @Transactional
    public PaymentResponse pay(PaymentRequest request) {
        Payment existing = paymentRepository.findByIdempotencyKey(request.idempotencyKey()).orElse(null);
        if (existing != null) {
            if (!existing.getBooking().getId().equals(request.bookingId())) {
                throw new BookingConflictException(
                        "idempotencyKey nay da duoc dung cho booking khac (id=" + existing.getBooking().getId() + ")");
            }
            return PaymentMapper.toResponse(existing);
        }

        Booking booking = getBookingOrThrow(request.bookingId());
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingConflictException("Chi thanh toan duoc booking dang o trang thai PENDING");
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionRef("FAKE-" + UUID.randomUUID());
        payment.setIdempotencyKey(request.idempotencyKey());
        payment.setCreatedAt(OffsetDateTime.now());
        Payment saved = paymentRepository.save(payment);

        booking.setStatus(BookingStatus.PAID);
        booking.setTicketCode(TicketCodeGenerator.generate());

        return PaymentMapper.toResponse(saved);
    }

    private Booking getBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay booking voi id=" + id));
    }
}
