package com.salonbooking.salon.persistence;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "salons", indexes = {@Index(name = "salon_city_idx", columnList = "city")})
public class SalonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    private String phoneNumber;

    @Column(length = 2000)
    private String description;

    private LocalTime openingTime;
    private LocalTime closingTime;

    @Column(nullable = false)
    private long ownerId;

    public SalonEntity() {
    }

    public SalonEntity(String name, String address, String city, String phoneNumber, String description,
                        LocalTime openingTime, LocalTime closingTime, long ownerId) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.description = description;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.ownerId = ownerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }
}
