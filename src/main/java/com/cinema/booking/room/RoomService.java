package com.cinema.booking.room;

import com.cinema.booking.cinema.Cinema;
import com.cinema.booking.cinema.CinemaRepository;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.room.dto.RoomRequest;
import com.cinema.booking.room.dto.RoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final CinemaRepository cinemaRepository;

    @Transactional
    public RoomResponse create(RoomRequest request) {
        Cinema cinema = getCinemaOrThrow(request.cinemaId());
        Room room = RoomMapper.toEntity(cinema, request);
        return RoomMapper.toResponse(roomRepository.save(room));
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> findAll() {
        return roomRepository.findAll().stream()
                .map(RoomMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse findById(Long id) {
        return RoomMapper.toResponse(getRoomOrThrow(id));
    }

    @Transactional
    public RoomResponse update(Long id, RoomRequest request) {
        Room room = getRoomOrThrow(id);
        Cinema cinema = getCinemaOrThrow(request.cinemaId());
        RoomMapper.applyRequest(room, cinema, request);
        return RoomMapper.toResponse(room);
    }

    @Transactional
    public void delete(Long id) {
        roomRepository.delete(getRoomOrThrow(id));
    }

    private Room getRoomOrThrow(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phong voi id=" + id));
    }

    private Cinema getCinemaOrThrow(Long cinemaId) {
        return cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay rap voi id=" + cinemaId));
    }
}
