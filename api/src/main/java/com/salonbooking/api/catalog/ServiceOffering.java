package com.salonbooking.api.catalog;

import java.math.BigDecimal;

/**
 * Usluga koju salon nudi (npr. "Zensko sisanje", "Manikir sa trajnim lakom").
 *
 * durationMinutes je kljucan podatak za zakazivanje: booking-service ga koristi
 * da izracuna kraj termina na osnovu pocetka koji je klijent izabrao.
 */
public class ServiceOffering {

    private long serviceId;
    private long salonId;
    private String name;
    private String description;
    private int durationMinutes;
    private BigDecimal price;
    private boolean active;

    private String serviceAddress;

    public ServiceOffering() {
    }

    public ServiceOffering(long serviceId, long salonId, String name, String description,
                            int durationMinutes, BigDecimal price, boolean active, String serviceAddress) {
        this.serviceId = serviceId;
        this.salonId = salonId;
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.active = active;
        this.serviceAddress = serviceAddress;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
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

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}
