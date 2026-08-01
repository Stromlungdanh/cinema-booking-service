package com.cinema.booking.cinema;

import com.cinema.booking.brand.Brand;
import com.cinema.booking.brand.BrandRepository;
import com.cinema.booking.cinema.dto.CinemaRequest;
import com.cinema.booking.cinema.dto.CinemaResponse;
import com.cinema.booking.common.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private CinemaService cinemaService;

    private Brand brand(long id, String name) {
        Brand brand = new Brand();
        brand.setId(id);
        brand.setName(name);
        return brand;
    }

    private CinemaRequest sampleRequest() {
        return new CinemaRequest(1L, "BHD Star Bitexco", "Quan 1", "Ho Chi Minh");
    }

    @Test
    void create_savesEntityBuiltFromRequest() {
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand(1L, "BHD Star")));
        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(invocation -> {
            Cinema cinema = invocation.getArgument(0);
            cinema.setId(1L);
            return cinema;
        });

        CinemaResponse response = cinemaService.create(sampleRequest());

        ArgumentCaptor<Cinema> captor = ArgumentCaptor.forClass(Cinema.class);
        verify(cinemaRepository).save(captor.capture());
        assertEquals("BHD Star Bitexco", captor.getValue().getName());
        assertEquals(1L, response.id());
        assertEquals("BHD Star", response.brandName());
    }

    @Test
    void create_throwsWhenBrandNotFound() {
        when(brandRepository.findById(99L)).thenReturn(Optional.empty());

        CinemaRequest request = new CinemaRequest(99L, "Ten rap", null, null);

        assertThrows(ResourceNotFoundException.class, () -> cinemaService.create(request));
    }

    @Test
    void findById_throwsResourceNotFoundWhenMissing() {
        when(cinemaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cinemaService.findById(99L));
    }

    @Test
    void update_appliesNewValuesToExistingEntity() {
        Cinema existing = new Cinema();
        existing.setId(5L);
        existing.setBrand(brand(1L, "BHD Star"));
        existing.setName("Ten cu");
        when(cinemaRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand(1L, "BHD Star")));

        CinemaResponse response = cinemaService.update(5L, sampleRequest());

        assertEquals("BHD Star Bitexco", response.name());
        assertEquals(5L, response.id());
    }

    @Test
    void delete_removesExistingEntity() {
        Cinema existing = new Cinema();
        existing.setId(7L);
        when(cinemaRepository.findById(7L)).thenReturn(Optional.of(existing));

        cinemaService.delete(7L);

        verify(cinemaRepository).delete(existing);
    }

    @Test
    void delete_throwsResourceNotFoundWhenMissing() {
        when(cinemaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cinemaService.delete(404L));
    }

    @Test
    void findByBrand_returnsCinemasOfBrand() {
        Cinema cinema = new Cinema();
        cinema.setId(1L);
        cinema.setBrand(brand(1L, "BHD Star"));
        cinema.setName("BHD Star Bitexco");
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand(1L, "BHD Star")));
        when(cinemaRepository.findByBrandId(1L)).thenReturn(List.of(cinema));

        var response = cinemaService.findByBrand(1L);

        assertEquals(1, response.size());
        assertEquals("BHD Star Bitexco", response.get(0).name());
    }

    @Test
    void findByBrand_throwsWhenBrandNotFound() {
        when(brandRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cinemaService.findByBrand(99L));
    }
}
