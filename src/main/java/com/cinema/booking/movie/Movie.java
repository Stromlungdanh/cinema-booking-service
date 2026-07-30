package com.cinema.booking.movie;

import com.cinema.booking.genre.Genre;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    private String language;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "trailer_url")
    private String trailerUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status = MovieStatus.COMING_SOON;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    // Quan he mot chieu Movie -> Genre, thuan tuy (movie_genres khong co cot rieng).
    @ManyToMany
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    // movie_actors mang them role_name nen dung entity lien ket rieng (MovieCast)
    // thay vi @ManyToMany. cascade + orphanRemoval de Service chi can clear()/addAll()
    // la Hibernate tu dong xoa dong cu, them dong moi.
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieCast> cast = new ArrayList<>();
}
