package com.cinema.booking.movie;

import com.cinema.booking.actor.Actor;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Entity lien ket rieng (khong dung @ManyToMany) vi bang movie_actors mang
// them du lieu (role_name) tren chinh quan he Movie-Actor, khong the bieu
// dien bang @ManyToMany thuan tuy.
@Entity
@Table(name = "movie_actors")
@Getter
@Setter
@NoArgsConstructor
public class MovieCast {

    @EmbeddedId
    private MovieCastId id = new MovieCastId();

    @ManyToOne
    @MapsId("movieId")
    private Movie movie;

    @ManyToOne
    @MapsId("actorId")
    private Actor actor;

    @Column(name = "role_name")
    private String roleName;
}
