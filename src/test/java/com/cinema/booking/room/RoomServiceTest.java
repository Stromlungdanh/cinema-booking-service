package com.cinema.booking.room;

import com.cinema.booking.cinema.Cinema;
import com.cinema.booking.cinema.CinemaRepository;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.room.dto.RoomRequest;
import com.cinema.booking.room.dto.RoomResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private CinemaRepository cinemaRepository;

    @InjectMocks
    private RoomService roomService;

    private Cinema cinema(long id, String name) {
        Cinema cinema = new Cinema();
        cinema.setId(id);
        cinema.setName(name);
        return cinema;
    }

    private RoomRequest sampleRequest() {
        return new RoomRequest(1L, "Phong 1", "IMAX");
    }

    @Test
    void create_savesEntityBuiltFromRequest() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema(1L, "BHD Star Bitexco")));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room room = invocation.getArgument(0);
            room.setId(1L);
            return room;
        });

        RoomResponse response = roomService.create(sampleRequest());

        ArgumentCaptor<Room> captor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(captor.capture());
        assertEquals("Phong 1", captor.getValue().getName());
        assertEquals("IMAX", captor.getValue().getRoomType());
        assertEquals(1L, response.id());
        assertEquals("BHD Star Bitexco", response.cinemaName());
    }

    @Test
    void create_throwsWhenCinemaNotFound() {
        when(cinemaRepository.findById(99L)).thenReturn(Optional.empty());

        RoomRequest request = new RoomRequest(99L, "Phong 1", "2D");

        assertThrows(ResourceNotFoundException.class, () -> roomService.create(request));
    }

    @Test
    void findById_throwsResourceNotFoundWhenMissing() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roomService.findById(99L));
    }

    @Test
    void update_appliesNewValuesToExistingEntity() {
        Room existing = new Room();
        existing.setId(5L);
        existing.setCinema(cinema(1L, "BHD Star Bitexco"));
        existing.setName("Ten cu");
        when(roomRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema(1L, "BHD Star Bitexco")));

        RoomResponse response = roomService.update(5L, sampleRequest());

        assertEquals("Phong 1", response.name());
        assertEquals(5L, response.id());
    }

    @Test
    void delete_removesExistingEntity() {
        Room existing = new Room();
        existing.setId(7L);
        when(roomRepository.findById(7L)).thenReturn(Optional.of(existing));

        roomService.delete(7L);

        verify(roomRepository).delete(existing);
    }

    @Test
    void delete_throwsResourceNotFoundWhenMissing() {
        when(roomRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roomService.delete(404L));
    }
}
