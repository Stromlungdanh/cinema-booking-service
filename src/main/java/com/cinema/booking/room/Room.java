package com.cinema.booking.room;

import com.cinema.booking.cinema.Cinema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @Column(nullable = false, length = 100)
    private String name;

    // String tu do, khong enum: schema khong co CHECK constraint tren cot nay
    // (khac movies.status), nghia la khong co bo gia tri co dinh o tang DB.
    @Column(name = "room_type", nullable = false, length = 20)
    private String roomType = "2D";
}
