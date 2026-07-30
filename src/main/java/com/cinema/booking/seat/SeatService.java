package com.cinema.booking.seat;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.room.Room;
import com.cinema.booking.room.RoomRepository;
import com.cinema.booking.seat.dto.SeatLayoutRequest;
import com.cinema.booking.seat.dto.SeatResponse;
import com.cinema.booking.seat.dto.SeatRowRequest;
import com.cinema.booking.seattype.SeatType;
import com.cinema.booking.seattype.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final SeatTypeRepository seatTypeRepository;

    // Ghi de toan bo so do ghe cua 1 phong: xoa het ghe cu, sinh ghe moi theo
    // tung hang trong request. An toan vi Giai doan 1 chua co Showtime/Booking
    // nao tham chieu seat_id cu the.
    @Transactional
    public List<SeatResponse> generateLayout(Long roomId, SeatLayoutRequest request) {
        Room room = getRoomOrThrow(roomId);
        Map<Long, SeatType> seatTypesById = resolveSeatTypes(request.rows());

        seatRepository.deleteByRoomId(roomId);
        seatRepository.flush();

        List<Seat> seats = new ArrayList<>();
        for (SeatRowRequest row : request.rows()) {
            SeatType seatType = seatTypesById.get(row.seatTypeId());
            for (int col = 1; col <= row.columnCount(); col++) {
                seats.add(SeatMapper.toEntity(room, seatType, row.rowLabel(), col));
            }
        }

        return seatRepository.saveAll(seats).stream()
                .map(SeatMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> findByRoom(Long roomId) {
        getRoomOrThrow(roomId);
        return seatRepository.findByRoomIdOrderByRowLabelAscColNumberAsc(roomId).stream()
                .map(SeatMapper::toResponse)
                .toList();
    }

    private Room getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phong voi id=" + roomId));
    }

    private Map<Long, SeatType> resolveSeatTypes(List<SeatRowRequest> rows) {
        List<Long> seatTypeIds = rows.stream().map(SeatRowRequest::seatTypeId).toList();
        Map<Long, SeatType> found = seatTypeRepository.findAllById(seatTypeIds).stream()
                .collect(Collectors.toMap(SeatType::getId, Function.identity()));
        if (found.size() != new HashSet<>(seatTypeIds).size()) {
            throw new ResourceNotFoundException("Khong tim thay 1 hoac nhieu loai ghe voi id da cho");
        }
        return found;
    }
}
