package com.cinema.booking.brand;

import com.cinema.booking.brand.dto.BrandRequest;
import com.cinema.booking.brand.dto.BrandResponse;
import com.cinema.booking.common.exception.ResourceNotFoundException;
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
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    private BrandRequest sampleRequest() {
        return new BrandRequest("BHD Star", "http://logo.jpg");
    }

    @Test
    void create_savesEntityBuiltFromRequest() {
        when(brandRepository.save(any(Brand.class))).thenAnswer(invocation -> {
            Brand brand = invocation.getArgument(0);
            brand.setId(1L);
            return brand;
        });

        BrandResponse response = brandService.create(sampleRequest());

        ArgumentCaptor<Brand> captor = ArgumentCaptor.forClass(Brand.class);
        verify(brandRepository).save(captor.capture());
        assertEquals("BHD Star", captor.getValue().getName());
        assertEquals(1L, response.id());
    }

    @Test
    void findById_throwsResourceNotFoundWhenMissing() {
        when(brandRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> brandService.findById(99L));
    }

    @Test
    void update_appliesNewValuesToExistingEntity() {
        Brand existing = new Brand();
        existing.setId(5L);
        existing.setName("Ten cu");
        when(brandRepository.findById(5L)).thenReturn(Optional.of(existing));

        BrandResponse response = brandService.update(5L, sampleRequest());

        assertEquals("BHD Star", response.name());
        assertEquals(5L, response.id());
    }

    @Test
    void delete_removesExistingEntity() {
        Brand existing = new Brand();
        existing.setId(7L);
        when(brandRepository.findById(7L)).thenReturn(Optional.of(existing));

        brandService.delete(7L);

        verify(brandRepository).delete(existing);
    }

    @Test
    void delete_throwsResourceNotFoundWhenMissing() {
        when(brandRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> brandService.delete(404L));
    }
}
