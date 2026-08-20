package com.salonbooking.api.salon;

import java.time.LocalTime;

/**
 * API DTO za salon lepote. Ovo je "ugovor" (contract) izmedju salon-service-a
 * i svih ostalih mikroservisa/klijenata koji citaju podatke o salonu
 * (npr. booking-service, gateway).
 */
public class Salon {

    private long salonId;
    private String name;
    private String address;
    private String city;
    private String phoneNumber;
    private String description;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private long ownerId;

    // Adresa/instanca mikroservisa koja je odgovorila - korisno za demonstraciju
    // load-balancinga izmedju vise instanci istog servisa (isti obrazac kao u knjizi).
    private String serviceAddress;

    public Salon() {
    }

    public Salon(long salonId, String name, String address, String city, String phoneNumber,
                 String description, LocalTime openingTime, LocalTime closingTime, long ownerId,
                 String serviceAddress) {
        this.salonId = salonId;
        this.name = name;
        this.address = address;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.description = description;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.ownerId = ownerId;
        this.serviceAddress = serviceAddress;
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

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}
