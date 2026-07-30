package com.cinema.booking.actor;

import com.cinema.booking.actor.dto.ActorRequest;
import com.cinema.booking.actor.dto.ActorResponse;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;

    @Transactional
    public ActorResponse create(ActorRequest request) {
        Actor actor = ActorMapper.toEntity(request);
        return ActorMapper.toResponse(actorRepository.save(actor));
    }

    @Transactional(readOnly = true)
    public List<ActorResponse> findAll() {
        return actorRepository.findAll().stream()
                .map(ActorMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ActorResponse findById(Long id) {
        return ActorMapper.toResponse(getActorOrThrow(id));
    }

    @Transactional
    public ActorResponse update(Long id, ActorRequest request) {
        Actor actor = getActorOrThrow(id);
        ActorMapper.applyRequest(actor, request);
        return ActorMapper.toResponse(actor);
    }

    @Transactional
    public void delete(Long id) {
        actorRepository.delete(getActorOrThrow(id));
    }

    private Actor getActorOrThrow(Long id) {
        return actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dien vien voi id=" + id));
    }
}
