package com.cinema.booking.seattype;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.seattype.dto.SeatTypeRequest;
import com.cinema.booking.seattype.dto.SeatTypeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatTypeService {

    private final SeatTypeRepository seatTypeRepository;

    @Transactional
    public SeatTypeResponse create(SeatTypeRequest request) {
        SeatType seatType = SeatTypeMapper.toEntity(request);
        return SeatTypeMapper.toResponse(seatTypeRepository.save(seatType));
    }

    @Transactional(readOnly = true)
    public List<SeatTypeResponse> findAll() {
        return seatTypeRepository.findAll().stream()
                .map(SeatTypeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SeatTypeResponse findById(Long id) {
        return SeatTypeMapper.toResponse(getSeatTypeOrThrow(id));
    }

    @Transactional
    public SeatTypeResponse update(Long id, SeatTypeRequest request) {
        SeatType seatType = getSeatTypeOrThrow(id);
        SeatTypeMapper.applyRequest(seatType, request);
        return SeatTypeMapper.toResponse(seatType);
    }

    @Transactional
    public void delete(Long id) {
        seatTypeRepository.delete(getSeatTypeOrThrow(id));
    }

    private SeatType getSeatTypeOrThrow(Long id) {
        return seatTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay loai ghe voi id=" + id));
    }
}
