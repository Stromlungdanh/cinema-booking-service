package com.cinema.booking.seattype;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.seattype.dto.SeatTypeRequest;
import com.cinema.booking.seattype.dto.SeatTypeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatTypeServiceTest {

    @Mock
    private SeatTypeRepository seatTypeRepository;

    @InjectMocks
    private SeatTypeService seatTypeService;

    private SeatTypeRequest sampleRequest() {
        return new SeatTypeRequest("VIP", new BigDecimal("1.50"));
    }

    @Test
    void create_savesEntityBuiltFromRequest() {
        when(seatTypeRepository.save(any(SeatType.class))).thenAnswer(invocation -> {
            SeatType seatType = invocation.getArgument(0);
            seatType.setId(1L);
            return seatType;
        });

        SeatTypeResponse response = seatTypeService.create(sampleRequest());

        ArgumentCaptor<SeatType> captor = ArgumentCaptor.forClass(SeatType.class);
        verify(seatTypeRepository).save(captor.capture());
        assertEquals("VIP", captor.getValue().getName());
        assertEquals(1L, response.id());
        assertEquals(new BigDecimal("1.50"), response.priceMultiplier());
    }

    @Test
    void findById_throwsResourceNotFoundWhenMissing() {
        when(seatTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> seatTypeService.findById(99L));
    }

    @Test
    void update_appliesNewValuesToExistingEntity() {
        SeatType existing = new SeatType();
        existing.setId(5L);
        existing.setName("Ten cu");
        when(seatTypeRepository.findById(5L)).thenReturn(Optional.of(existing));

        SeatTypeResponse response = seatTypeService.update(5L, sampleRequest());

        assertEquals("VIP", response.name());
        assertEquals(5L, response.id());
    }

    @Test
    void delete_removesExistingEntity() {
        SeatType existing = new SeatType();
        existing.setId(7L);
        when(seatTypeRepository.findById(7L)).thenReturn(Optional.of(existing));

        seatTypeService.delete(7L);

        verify(seatTypeRepository).delete(existing);
    }

    @Test
    void delete_throwsResourceNotFoundWhenMissing() {
        when(seatTypeRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> seatTypeService.delete(404L));
    }
}
