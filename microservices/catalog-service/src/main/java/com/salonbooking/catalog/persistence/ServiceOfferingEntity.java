package com.salonbooking.catalog.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "service_offerings", indexes = {
        @Index(name = "service_salon_idx", columnList = "salonId")
})
public class ServiceOfferingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(nullable = false)
    private long salonId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active = true;

    public ServiceOfferingEntity() {
    }

    public ServiceOfferingEntity(long salonId, String name, String description,
                                  int durationMinutes, BigDecimal price, boolean active) {
        this.salonId = salonId;
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getSalonId() {
        return salonId;
    }

    public void setSalonId(long salonId) {
        this.salonId = salonId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
