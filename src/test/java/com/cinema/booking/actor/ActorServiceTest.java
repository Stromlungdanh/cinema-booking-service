package com.cinema.booking.actor;

import com.cinema.booking.actor.dto.ActorRequest;
import com.cinema.booking.actor.dto.ActorResponse;
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
class ActorServiceTest {

    @Mock
    private ActorRepository actorRepository;

    @InjectMocks
    private ActorService actorService;

    private ActorRequest sampleRequest() {
        return new ActorRequest("Leonardo DiCaprio", "http://avatar.jpg");
    }

    @Test
    void create_savesEntityBuiltFromRequest() {
        when(actorRepository.save(any(Actor.class))).thenAnswer(invocation -> {
            Actor actor = invocation.getArgument(0);
            actor.setId(1L);
            return actor;
        });

        ActorResponse response = actorService.create(sampleRequest());

        ArgumentCaptor<Actor> captor = ArgumentCaptor.forClass(Actor.class);
        verify(actorRepository).save(captor.capture());
        assertEquals("Leonardo DiCaprio", captor.getValue().getName());
        assertEquals(1L, response.id());
    }

    @Test
    void findById_throwsResourceNotFoundWhenMissing() {
        when(actorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> actorService.findById(99L));
    }

    @Test
    void update_appliesNewValuesToExistingEntity() {
        Actor existing = new Actor();
        existing.setId(5L);
        existing.setName("Ten cu");
        when(actorRepository.findById(5L)).thenReturn(Optional.of(existing));

        ActorResponse response = actorService.update(5L, sampleRequest());

        assertEquals("Leonardo DiCaprio", response.name());
        assertEquals(5L, response.id());
    }

    @Test
    void delete_removesExistingEntity() {
        Actor existing = new Actor();
        existing.setId(7L);
        when(actorRepository.findById(7L)).thenReturn(Optional.of(existing));

        actorService.delete(7L);

        verify(actorRepository).delete(existing);
    }

    @Test
    void delete_throwsResourceNotFoundWhenMissing() {
        when(actorRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> actorService.delete(404L));
    }
}
