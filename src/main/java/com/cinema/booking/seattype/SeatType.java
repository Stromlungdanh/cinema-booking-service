package com.cinema.booking.seattype;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "seat_types")
@Getter
@Setter
@NoArgsConstructor
public class SeatType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "price_multiplier", nullable = false, precision = 4, scale = 2)
    private BigDecimal priceMultiplier = BigDecimal.ONE;
}
