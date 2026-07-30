package com.cinema.booking.actor;

import com.cinema.booking.actor.dto.ActorRequest;
import com.cinema.booking.actor.dto.ActorResponse;

public final class ActorMapper {

    private ActorMapper() {
    }

    public static Actor toEntity(ActorRequest request) {
        Actor actor = new Actor();
        applyRequest(actor, request);
        return actor;
    }

    public static void applyRequest(Actor actor, ActorRequest request) {
        actor.setName(request.name());
        actor.setAvatarUrl(request.avatarUrl());
    }

    public static ActorResponse toResponse(Actor actor) {
        return new ActorResponse(actor.getId(), actor.getName(), actor.getAvatarUrl());
    }
}
