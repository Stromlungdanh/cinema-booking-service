package com.cinema.booking.seat;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.room.Room;
import com.cinema.booking.room.RoomRepository;
import com.cinema.booking.seat.dto.SeatLayoutRequest;
import com.cinema.booking.seat.dto.SeatResponse;
import com.cinema.booking.seat.dto.SeatRowRequest;
import com.cinema.booking.seattype.SeatType;
import com.cinema.booking.seattype.SeatTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private SeatTypeRepository seatTypeRepository;

    @InjectMocks
    private SeatService seatService;

    private Room room(long id) {
        Room room = new Room();
        room.setId(id);
        room.setName("Phong 1");
        return room;
    }

    private SeatType seatType(long id, String name) {
        SeatType seatType = new SeatType();
        seatType.setId(id);
        seatType.setName(name);
        return seatType;
    }

    @Test
    void generateLayout_createsSeatsForEachRowAndColumn() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L)));
        when(seatTypeRepository.findAllById(List.of(10L))).thenReturn(List.of(seatType(10L, "Standard")));
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        SeatLayoutRequest request = new SeatLayoutRequest(List.of(new SeatRowRequest("A", 3, 10L)));

        List<SeatResponse> response = seatService.generateLayout(1L, request);

        verify(seatRepository).deleteByRoomId(1L);
        assertEquals(3, response.size());
        assertEquals("A", response.get(0).rowLabel());
        assertEquals(1, response.get(0).colNumber());
        assertEquals(3, response.get(2).colNumber());
        assertEquals("Standard", response.get(0).seatTypeName());
    }

    @Test
    void generateLayout_savesEntitiesWithCorrectRoomAndSeatType() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L)));
        when(seatTypeRepository.findAllById(List.of(10L))).thenReturn(List.of(seatType(10L, "Standard")));
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        SeatLayoutRequest request = new SeatLayoutRequest(List.of(new SeatRowRequest("A", 2, 10L)));

        seatService.generateLayout(1L, request);

        ArgumentCaptor<List<Seat>> captor = ArgumentCaptor.forClass(List.class);
        verify(seatRepository).saveAll(captor.capture());
        List<Seat> saved = captor.getValue();
        assertEquals(2, saved.size());
        assertEquals(1L, saved.get(0).getRoom().getId());
        assertEquals(10L, saved.get(0).getSeatType().getId());
    }

    @Test
    void generateLayout_throwsWhenRoomNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        SeatLayoutRequest request = new SeatLayoutRequest(List.of(new SeatRowRequest("A", 2, 10L)));

        assertThrows(ResourceNotFoundException.class, () -> seatService.generateLayout(99L, request));
    }

    @Test
    void generateLayout_throwsWhenSeatTypeNotFound() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L)));
        when(seatTypeRepository.findAllById(List.of(99L))).thenReturn(List.of());

        SeatLayoutRequest request = new SeatLayoutRequest(List.of(new SeatRowRequest("A", 2, 99L)));

        assertThrows(ResourceNotFoundException.class, () -> seatService.generateLayout(1L, request));
    }

    @Test
    void findByRoom_throwsWhenRoomNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> seatService.findByRoom(99L));
    }
}
