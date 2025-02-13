package com.nimbleways.springboilerplate.entities;

import lombok.*;

import java.time.LocalDate;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lead_time")
    private Integer leadTime;

    @Column(name = "available")
    private Integer available;

    @Column(name = "type")
    private ProductType type;

    @Column(name = "name")
    private String name;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "season_start_date")
    private LocalDate seasonStartDate;

    @Column(name = "season_end_date")
    private LocalDate seasonEndDate;

    public void reduceAvailabilityBy1() {
        this.available--;
    }

    public boolean isAvailable() {
        return available > 0;
    }

    public boolean hasLeadTime() {
        return this.leadTime > 0;
    }

    public boolean isInSeason() {
        return LocalDate.now().isAfter(this.seasonStartDate) &&
                LocalDate.now().isBefore(this.seasonEndDate);
    }

    public boolean isStillValid() {
        return this.getExpiryDate().isAfter(LocalDate.now());
    }

    public boolean willBeOutOfSeason() {
        return LocalDate.now().plusDays(leadTime).isAfter(seasonEndDate);
    }

    public boolean isSeasonNotStarted() {
        return seasonStartDate.isAfter(LocalDate.now());
    }
}
