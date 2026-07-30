package com.cinema.booking.seattype;

import com.cinema.booking.seattype.dto.SeatTypeRequest;
import com.cinema.booking.seattype.dto.SeatTypeResponse;

public final class SeatTypeMapper {

    private SeatTypeMapper() {
    }

    public static SeatType toEntity(SeatTypeRequest request) {
        SeatType seatType = new SeatType();
        applyRequest(seatType, request);
        return seatType;
    }

    public static void applyRequest(SeatType seatType, SeatTypeRequest request) {
        seatType.setName(request.name());
        seatType.setPriceMultiplier(request.priceMultiplier());
    }

    public static SeatTypeResponse toResponse(SeatType seatType) {
        return new SeatTypeResponse(seatType.getId(), seatType.getName(), seatType.getPriceMultiplier());
    }
}
