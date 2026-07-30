package com.cinema.booking.seat;

import com.cinema.booking.room.Room;
import com.cinema.booking.seattype.SeatType;
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
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "row_label", nullable = false, length = 5)
    private String rowLabel;

    @Column(name = "col_number", nullable = false)
    private Integer colNumber;

    @ManyToOne
    @JoinColumn(name = "seat_type_id", nullable = false)
    private SeatType seatType;
}
