package com.cinema.booking.booking;

import com.cinema.booking.booking.dto.BookingRequest;
import com.cinema.booking.booking.dto.BookingResponse;
import com.cinema.booking.booking.dto.TicketResponse;
import com.cinema.booking.common.exception.BookingConflictException;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.seat.Seat;
import com.cinema.booking.seat.SeatRepository;
import com.cinema.booking.showtime.Showtime;
import com.cinema.booking.showtime.ShowtimeRepository;
import com.cinema.booking.user.User;
import com.cinema.booking.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(BookingStatus.PENDING, BookingStatus.PAID);

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public BookingResponse create(Long userId, BookingRequest request) {
        User user = getUserOrThrow(userId);
        Showtime showtime = getShowtimeOrThrow(request.showtimeId());
        List<Seat> seats = resolveSeats(showtime, request.seatIds());

        List<Long> bookedSeatIds = bookingSeatRepository.findBookedSeatIds(
                showtime.getId(), ACTIVE_STATUSES, request.seatIds());
        if (!bookedSeatIds.isEmpty()) {
            throw new BookingConflictException("Ghe da duoc dat: " + bookedSeatIds);
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(OffsetDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        for (Seat seat : seats) {
            BigDecimal price = BookingMapper.priceForSeat(showtime, seat);
            total = total.add(price);

            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(booking);
            bookingSeat.setSeat(seat);
            bookingSeat.setPrice(price);
            booking.getSeats().add(bookingSeat);
        }
        booking.setTotalPrice(total);

        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingResponse findById(Long id, Long currentUserId) {
        Booking booking = getBookingOrThrow(id);
        requireOwner(booking, currentUserId);
        return BookingMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Khong tim thay user voi id=" + userId);
        }
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Transactional
    public BookingResponse cancel(Long id, Long currentUserId) {
        Booking booking = getBookingOrThrow(id);
        requireOwner(booking, currentUserId);
        return BookingMapper.toResponse(transitionToCancelled(booking));
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long id, Long currentUserId) {
        Booking booking = getBookingOrThrow(id);
        requireOwner(booking, currentUserId);
        if (booking.getStatus() != BookingStatus.PAID || booking.getTicketCode() == null) {
            throw new BookingConflictException("Booking chua thanh toan, chua co ve");
        }
        return BookingMapper.toTicketResponse(booking);
    }

    // --- Admin (khong ownership check - xem/huy duoc booking cua moi user) ---

    @Transactional(readOnly = true)
    public List<BookingResponse> findAllForAdmin(BookingStatus status, Long userId, Long showtimeId) {
        return bookingRepository.findAllFiltered(status, userId, showtimeId).stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse findByIdForAdmin(Long id) {
        return BookingMapper.toResponse(getBookingOrThrow(id));
    }

    @Transactional
    public BookingResponse cancelForAdmin(Long id) {
        return BookingMapper.toResponse(transitionToCancelled(getBookingOrThrow(id)));
    }

    private Booking transitionToCancelled(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingConflictException("Chi huy duoc booking dang o trang thai PENDING");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return booking;
    }

    // Validate moi seatId ton tai va thuoc dung phong chieu cua showtime,
    // giu nguyen thu tu request de tinh gia/tao BookingSeat khop voi seatIds.
    private List<Seat> resolveSeats(Showtime showtime, List<Long> seatIds) {
        List<Seat> found = seatRepository.findAllById(seatIds);
        Map<Long, Seat> byId = found.stream().collect(Collectors.toMap(Seat::getId, Function.identity()));
        Long roomId = showtime.getRoom().getId();
        return seatIds.stream()
                .map(seatId -> {
                    Seat seat = byId.get(seatId);
                    if (seat == null || !seat.getRoom().getId().equals(roomId)) {
                        throw new ResourceNotFoundException(
                                "Ghe id=" + seatId + " khong thuoc phong chieu cua suat chieu nay");
                    }
                    return seat;
                })
                .toList();
    }

    private Booking getBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay booking voi id=" + id));
    }

    // 404 (khong phai 403) khi khong phai chu booking - khong lo cho nguoi
    // goi biet booking do co ton tai hay khong.
    private void requireOwner(Booking booking, Long currentUserId) {
        if (!booking.getUser().getId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Khong tim thay booking voi id=" + booking.getId());
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user voi id=" + userId));
    }

    private Showtime getShowtimeOrThrow(Long showtimeId) {
        return showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay suat chieu voi id=" + showtimeId));
    }
}
